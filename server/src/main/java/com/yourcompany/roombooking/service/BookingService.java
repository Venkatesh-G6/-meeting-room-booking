package com.yourcompany.roombooking.service;

import com.yourcompany.roombooking.dto.request.CreateBookingRequest;
import com.yourcompany.roombooking.dto.response.AvailabilityResponse;
import com.yourcompany.roombooking.dto.response.BookingResponse;
import com.yourcompany.roombooking.dto.response.EmployeeResponse;
import com.yourcompany.roombooking.dto.response.RoomResponse;
import com.yourcompany.roombooking.dto.response.TodayBookingsResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface BookingService {

    List<EmployeeResponse> getAllEmployees();

    List<RoomResponse> getAllRooms();

    AvailabilityResponse checkAvailability(Long roomId,
                                           LocalDate date,
                                           LocalTime startTime,
                                           LocalTime endTime);

    BookingResponse createBooking(CreateBookingRequest request);

    List<TodayBookingsResponse> getTodayBookings();

    List<BookingResponse> getRecentBookings(int days);

    void cancelBooking(Long bookingId, Long employeeId);
}
