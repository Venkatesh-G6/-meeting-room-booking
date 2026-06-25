package com.yourcompany.roombooking.service;

import com.yourcompany.roombooking.dto.request.CreateBookingRequest;
import com.yourcompany.roombooking.dto.response.BookingResponse;

import java.util.List;

public interface BookingService {

    BookingResponse createBooking(CreateBookingRequest request, String bookedBy);

    BookingResponse getBookingById(Long id);

    List<BookingResponse> getMyBookings(String bookedBy);

    List<BookingResponse> getAllBookings();

    void cancelBooking(Long id, String requestedBy);
}
