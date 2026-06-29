package com.yourcompany.roombooking.service;

import com.yourcompany.roombooking.dto.request.CreateBookingRequest;
import com.yourcompany.roombooking.dto.response.BookingResponse;

import java.util.List;
import java.util.UUID;

public interface BookingService {

    BookingResponse createBooking(CreateBookingRequest request, String bookedBy);

    BookingResponse getBookingById(UUID id);

    List<BookingResponse> getMyBookings(String bookedBy);

    List<BookingResponse> getAllBookings();

    void cancelBooking(UUID id, String requestedBy);
}
