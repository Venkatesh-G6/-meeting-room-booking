package com.yourcompany.roombooking.service;

import com.yourcompany.roombooking.dto.request.CreateBookingRequest;
import com.yourcompany.roombooking.dto.response.BookingResponse;
import com.yourcompany.roombooking.dto.response.PagedResponse;

import java.util.UUID;

public interface BookingService {

    BookingResponse createBooking(CreateBookingRequest request, String bookedBy);

    BookingResponse getBookingById(UUID id);

    PagedResponse<BookingResponse> getMyBookings(String bookedBy, int page, int size);

    PagedResponse<BookingResponse> getAllBookings(int page, int size);

    void cancelBooking(UUID id, String requestedBy);
}
