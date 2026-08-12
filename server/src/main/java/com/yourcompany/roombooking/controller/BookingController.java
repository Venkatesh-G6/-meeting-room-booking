package com.yourcompany.roombooking.controller;

import com.yourcompany.roombooking.dto.request.CreateBookingRequest;
import com.yourcompany.roombooking.dto.response.AvailabilityResponse;
import com.yourcompany.roombooking.dto.response.BookingResponse;
import com.yourcompany.roombooking.dto.response.EmployeeResponse;
import com.yourcompany.roombooking.dto.response.RoomResponse;
import com.yourcompany.roombooking.dto.response.TodayBookingsResponse;
import com.yourcompany.roombooking.service.BookingService;
import com.yourcompany.roombooking.util.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
@Tag(name = "Room Booking")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/employees")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getAllEmployees() {
        List<EmployeeResponse> employees = bookingService.getAllEmployees();
        return ResponseEntity.ok(
                ApiResponse.success("Employees fetched successfully", employees));
    }

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAllRooms() {
        List<RoomResponse> rooms = bookingService.getAllRooms();
        return ResponseEntity.ok(
                ApiResponse.success("Rooms fetched successfully", rooms));
    }

    @GetMapping("/bookings/availability")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkAvailability(
            @RequestParam Long roomId,
            @RequestParam LocalDate date,
            @RequestParam LocalTime startTime,
            @RequestParam LocalTime endTime) {
        AvailabilityResponse response = bookingService.checkAvailability(roomId, date, startTime, endTime);
        return ResponseEntity.ok(
                ApiResponse.success("Availability checked", response));
    }

    @PostMapping("/bookings")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody CreateBookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Room booked successfully", response));
    }

    @GetMapping("/bookings/today")
    public ResponseEntity<ApiResponse<List<TodayBookingsResponse>>> getTodayBookings() {
        List<TodayBookingsResponse> response = bookingService.getTodayBookings();
        return ResponseEntity.ok(
                ApiResponse.success("Today's bookings fetched", response));
    }

    @GetMapping("/bookings/recent")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getRecentBookings(
            @RequestParam(defaultValue = "5") int days) {
        List<BookingResponse> response = bookingService.getRecentBookings(days);
        return ResponseEntity.ok(
                ApiResponse.success("Recent bookings fetched", response));
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            @PathVariable Long id,
            @RequestParam Long employeeId) {
        bookingService.cancelBooking(id, employeeId);
        return ResponseEntity.ok(
                ApiResponse.success("Booking cancelled successfully", null));
    }
}
