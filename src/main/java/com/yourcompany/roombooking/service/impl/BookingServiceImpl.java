package com.yourcompany.roombooking.service.impl;

import com.yourcompany.roombooking.dto.request.CreateBookingRequest;
import com.yourcompany.roombooking.dto.response.BookingResponse;
import com.yourcompany.roombooking.entity.Booking;
import com.yourcompany.roombooking.entity.Room;
import com.yourcompany.roombooking.enums.BookingStatus;
import com.yourcompany.roombooking.exception.BookingException;
import com.yourcompany.roombooking.exception.ResourceNotFoundException;
import com.yourcompany.roombooking.repository.BookingRepository;
import com.yourcompany.roombooking.repository.RoomRepository;
import com.yourcompany.roombooking.service.BookingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    public BookingServiceImpl(BookingRepository bookingRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (!room.getActive()) {
            throw new BookingException("Room is not available for booking");
        }

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BookingException("Start time must be before end time");
        }

        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BookingException("Booking cannot be made in the past");
        }

        if (request.getAttendeeCount() > room.getCapacity()) {
            throw new BookingException("Attendee count exceeds room capacity");
        }

        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                room.getId(), request.getStartTime(), request.getEndTime()
        );
        if (!overlappingBookings.isEmpty()) {
            throw new BookingException("Room is already booked for the selected time slot");
        }

        Booking booking = Booking.builder()
                .room(room)
                .bookedBy(request.getBookedBy())
                .title(request.getTitle())
                .attendeeCount(request.getAttendeeCount())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(BookingStatus.CONFIRMED)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        return mapToResponse(savedBooking);
    }

    @Override
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        return mapToResponse(booking);
    }

    @Override
    public List<BookingResponse> getMyBookings(String bookedBy) {
        return bookingRepository.findAllByBookedByOrderByStartTimeDesc(bookedBy).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void cancelBooking(Long id, String requestedBy) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingException("Booking is already cancelled");
        }

        if (!booking.getBookedBy().equals(requestedBy)) {
            throw new BookingException("You are not authorized to cancel this booking");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
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
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
