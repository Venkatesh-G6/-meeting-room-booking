package com.yourcompany.roombooking.service.impl;

import com.yourcompany.roombooking.dto.request.CreateBookingRequest;
import com.yourcompany.roombooking.dto.response.AvailabilityResponse;
import com.yourcompany.roombooking.dto.response.BookingResponse;
import com.yourcompany.roombooking.dto.response.EmployeeResponse;
import com.yourcompany.roombooking.dto.response.RoomResponse;
import com.yourcompany.roombooking.dto.response.TodayBookingsResponse;
import com.yourcompany.roombooking.entity.Booking;
import com.yourcompany.roombooking.entity.Employee;
import com.yourcompany.roombooking.entity.Room;
import com.yourcompany.roombooking.enums.BookingStatus;
import com.yourcompany.roombooking.enums.RoomStatus;
import com.yourcompany.roombooking.exception.BookingException;
import com.yourcompany.roombooking.exception.ResourceNotFoundException;
import com.yourcompany.roombooking.repository.BookingRepository;
import com.yourcompany.roombooking.repository.EmployeeRepository;
import com.yourcompany.roombooking.repository.RoomRepository;
import com.yourcompany.roombooking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final EmployeeRepository employeeRepository;
    private final RoomRepository roomRepository;

    @Override
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAllByActiveTrue().stream()
                .map(this::mapToEmployeeResponse)
                .toList();
    }

    @Override
    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAllByStatus(RoomStatus.AVAILABLE).stream()
                .map(this::mapToRoomResponse)
                .toList();
    }

    @Override
    public AvailabilityResponse checkAvailability(Long roomId,
                                                   LocalDate date,
                                                   LocalTime startTime,
                                                   LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new BookingException("Start time must be before end time");
        }

        if (date.isBefore(LocalDate.now())) {
            throw new BookingException("Cannot book for a past date");
        }

        LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(date, endTime);

        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                roomId, BookingStatus.CONFIRMED, startDateTime, endDateTime);

        if (overlapping.isEmpty()) {
            return AvailabilityResponse.builder()
                    .available(true)
                    .roomId(roomId)
                    .date(date)
                    .requestedStart(startTime)
                    .requestedEnd(endTime)
                    .message("Room is available")
                    .build();
        }

        Booking conflicting = overlapping.get(0);
        LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);
        Optional<Booking> nextSlot = bookingRepository.findNextAvailableSlot(
                roomId, BookingStatus.CONFIRMED, conflicting.getEndTime(), endOfDay);

        LocalTime suggestedStart = null;
        LocalTime suggestedEnd = null;

        if (nextSlot.isPresent()) {
            suggestedStart = conflicting.getEndTime().toLocalTime();
            long durationMinutes = Duration.between(startTime, endTime).toMinutes();
            suggestedEnd = suggestedStart.plusMinutes(durationMinutes);
        }

        return AvailabilityResponse.builder()
                .available(false)
                .roomId(roomId)
                .roomName(conflicting.getRoom().getRoomName())
                .date(date)
                .requestedStart(startTime)
                .requestedEnd(endTime)
                .conflictingBooking(mapToBookingResponse(conflicting))
                .suggestedStartTime(suggestedStart)
                .suggestedEndTime(suggestedEnd)
                .message("Room is booked from "
                        + conflicting.getStartTime().toLocalTime()
                        + " to "
                        + conflicting.getEndTime().toLocalTime())
                .build();
    }

    @Override
    public BookingResponse createBooking(CreateBookingRequest request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room not found with id: " + request.getRoomId()));

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + request.getEmployeeId()));

        AvailabilityResponse availability = checkAvailability(
                request.getRoomId(), request.getDate(),
                request.getStartTime(), request.getEndTime());

        if (!availability.getAvailable()) {
            throw new BookingException(availability.getMessage());
        }

        Booking booking = Booking.builder()
                .room(room)
                .employee(employee)
                .title(request.getTitle())
                .startTime(LocalDateTime.of(request.getDate(), request.getStartTime()))
                .endTime(LocalDateTime.of(request.getDate(), request.getEndTime()))
                .status(BookingStatus.CONFIRMED)
                .build();

        Booking saved = bookingRepository.save(booking);
        return mapToBookingResponse(saved);
    }

    @Override
    public List<TodayBookingsResponse> getTodayBookings() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        List<Booking> todayBookings = bookingRepository.findTodayBookings(startOfDay, endOfDay, BookingStatus.CONFIRMED);

        Map<Long, List<Booking>> bookingsByRoom = todayBookings.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getRoom().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<TodayBookingsResponse> result = new ArrayList<>();

        for (Map.Entry<Long, List<Booking>> entry : bookingsByRoom.entrySet()) {
            List<Booking> bookings = entry.getValue();
            Room room = bookings.get(0).getRoom();
            List<BookingResponse> bookingResponses = bookings.stream()
                    .map(this::mapToBookingResponse)
                    .toList();

            result.add(TodayBookingsResponse.builder()
                    .roomId(room.getId())
                    .roomName(room.getRoomName())
                    .location(room.getLocation())
                    .bookings(bookingResponses)
                    .fullyAvailable(false)
                    .build());
        }

        List<Room> availableRooms = roomRepository.findAllByStatus(RoomStatus.AVAILABLE);
        for (Room room : availableRooms) {
            if (!bookingsByRoom.containsKey(room.getId())) {
                result.add(TodayBookingsResponse.builder()
                        .roomId(room.getId())
                        .roomName(room.getRoomName())
                        .location(room.getLocation())
                        .bookings(List.of())
                        .fullyAvailable(true)
                        .build());
            }
        }

        result.sort((a, b) -> a.getRoomName().compareToIgnoreCase(b.getRoomName()));
        return result;
    }

    @Override
    public List<BookingResponse> getRecentBookings(int days) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
        return bookingRepository.findRecentBookings(fromDate, BookingStatus.CONFIRMED).stream()
                .map(this::mapToBookingResponse)
                .toList();
    }

    @Override
    public void cancelBooking(Long bookingId, Long employeeId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found with id: " + bookingId));

        if (!booking.getEmployee().getId().equals(employeeId)) {
            throw new BookingException("You can only cancel your own bookings");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingException("Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .roomId(booking.getRoom().getId())
                .roomName(booking.getRoom().getRoomName())
                .employeeId(booking.getEmployee().getId())
                .employeeName(booking.getEmployee().getName())
                .employeeEmail(booking.getEmployee().getEmail())
                .title(booking.getTitle())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }

    private RoomResponse mapToRoomResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .roomName(room.getRoomName())
                .capacity(room.getCapacity())
                .location(room.getLocation())
                .status(room.getStatus())
                .build();
    }

    private EmployeeResponse mapToEmployeeResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .email(employee.getEmail())
                .department(employee.getDepartment())
                .build();
    }
}
