package com.yourcompany.roombooking.service.impl;

import com.yourcompany.roombooking.audit.AuditMeta;
import com.yourcompany.roombooking.audit.AuditService;
import com.yourcompany.roombooking.dto.request.CreateBookingRequest;
import com.yourcompany.roombooking.dto.response.BookingResponse;
import com.yourcompany.roombooking.dto.response.PagedResponse;
import com.yourcompany.roombooking.entity.Booking;
import com.yourcompany.roombooking.entity.Room;
import com.yourcompany.roombooking.enums.AuditAction;
import com.yourcompany.roombooking.enums.BookingStatus;
import com.yourcompany.roombooking.exception.BookingException;
import com.yourcompany.roombooking.exception.ResourceNotFoundException;
import com.yourcompany.roombooking.graph.GraphService;
import com.yourcompany.roombooking.repository.BookingRepository;
import com.yourcompany.roombooking.repository.RoomRepository;
import com.yourcompany.roombooking.service.BookingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final AuditService auditService;
    private final GraphService graphService;

    public BookingServiceImpl(BookingRepository bookingRepository, RoomRepository roomRepository, AuditService auditService, GraphService graphService) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.auditService = auditService;
        this.graphService = graphService;
    }

    @Override
    @Transactional
    // Pessimistic lock ensures no two transactions can book the same room at the same time
    public BookingResponse createBooking(CreateBookingRequest request, String bookedBy) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (!room.getActive()) {
            throw new BookingException("Room is not available for booking");
        }

        if (request.getAttendeeCount() == null) {
            request.setAttendeeCount(1);
        }

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            request.setTitle("Meeting - " + room.getRoomName());
        }

        validateTimeRange(request.getStartTime(), request.getEndTime());
        validateFutureBooking(request.getStartTime());
        validateRoomCapacity(room, request.getAttendeeCount());
        validateDuplicateBooking(room.getId(), bookedBy, request.getStartTime(), request.getEndTime());
        validateRoomAvailability(room.getId(), request.getStartTime(), request.getEndTime());

        Booking booking = Booking.builder()
                .room(room)
                .bookedBy(bookedBy)
                .title(request.getTitle())
                .attendeeCount(request.getAttendeeCount())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(BookingStatus.CONFIRMED)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        auditService.log(
                bookedBy,
                AuditAction.BOOKING_CREATED,
                "BOOKING",
                savedBooking.getId().toString(),
                new AuditMeta.BookingMeta(
                        savedBooking.getId(),
                        savedBooking.getRoom().getId(),
                        savedBooking.getRoom().getRoomName(),
                        savedBooking.getBookedBy(),
                        savedBooking.getStartTime().toString(),
                        savedBooking.getEndTime().toString()
                )
        );

        BookingResponse response = mapToResponse(savedBooking);

        try {
            String eventId = graphService.createCalendarEvent(response);
            if (eventId != null) {
                savedBooking.setGraphEventId(eventId);
                bookingRepository.save(savedBooking);
                response.setGraphEventId(eventId);
                log.info("Graph event created: {}", eventId);
            }
        } catch (Exception e) {
            log.warn("Graph calendar sync failed for booking {}: {}", savedBooking.getId(), e.getMessage());
            // Do NOT fail the booking
            // Graph sync failure is non-critical
        }

        return response;
    }

    @Override
    public BookingResponse getBookingById(UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        return mapToResponse(booking);
    }

    @Override
    public PagedResponse<BookingResponse> getMyBookings(String bookedBy, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> bookingPage = bookingRepository.findAllByBookedByOrderByStartTimeDesc(bookedBy, pageable);
        List<BookingResponse> bookingResponses = bookingPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return PagedResponse.of(bookingPage, bookingResponses);
    }

    @Override
    public PagedResponse<BookingResponse> getAllBookings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Booking> bookingPage = bookingRepository.findAll(pageable);
        List<BookingResponse> bookingResponses = bookingPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return PagedResponse.of(bookingPage, bookingResponses);
    }

    @Override
    public void cancelBooking(UUID id, String requestedBy) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingException("Booking is already cancelled");
        }

        if (booking.getStartTime().isBefore(LocalDateTime.now()) && booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new BookingException("Cannot cancel a booking that has already started");
        }

        if (!booking.getBookedBy().equals(requestedBy) && !isCurrentUserAdmin()) {
            throw new BookingException("You are not authorized to cancel this booking");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        auditService.log(
                requestedBy,
                AuditAction.BOOKING_CANCELLED,
                "BOOKING",
                booking.getId().toString(),
                new AuditMeta.BookingMeta(
                        booking.getId(),
                        booking.getRoom().getId(),
                        booking.getRoom().getRoomName(),
                        booking.getBookedBy(),
                        booking.getStartTime().toString(),
                        booking.getEndTime().toString()
                )
        );

        try {
            if (booking.getGraphEventId() != null) {
                graphService.cancelCalendarEvent(booking.getGraphEventId());
                log.info("Graph event cancelled: {}", booking.getGraphEventId());
            }
        } catch (Exception e) {
            log.warn("Graph cancellation failed for booking {}: {}", booking.getId(), e.getMessage());
            // Do NOT fail the cancellation
        }
    }

    /*
     * TODO Phase 9 — Teams Integration:
     * Replace hardcoded admin check with
     * proper role verification from JWT.
     * SecurityContextHolder will have
     * ROLE_ADMIN from Entra ID token.
     */
    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new BookingException("Start time must be before end time");
        }
    }

    private void validateFutureBooking(LocalDateTime startTime) {
        if (startTime.isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new BookingException("Booking cannot be made in the past");
        }
    }

    private void validateRoomCapacity(Room room, Integer attendeeCount) {
        if (attendeeCount > room.getCapacity()) {
            throw new BookingException("Attendee count exceeds room capacity");
        }
    }

    private void validateRoomAvailability(UUID roomId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                roomId, startTime, endTime
        );
        if (!overlappingBookings.isEmpty()) {
            throw new BookingException("Room is already booked for the selected time slot");
        }
    }

    private void validateDuplicateBooking(UUID roomId, String bookedBy, LocalDateTime startTime, LocalDateTime endTime) {
        bookingRepository.findDuplicateBooking(roomId, bookedBy, startTime, endTime)
                .ifPresent(booking -> {
                    throw new BookingException("You already have a booking for this room at the selected time");
                });
    }

    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .roomId(booking.getRoom().getId())
                .roomName(booking.getRoom().getRoomName())
                .bookedBy(booking.getBookedBy())
                .title(booking.getTitle())
                .attendeeCount(booking.getAttendeeCount())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus())
                .graphEventId(booking.getGraphEventId())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
