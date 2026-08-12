package com.yourcompany.roombooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodayBookingsResponse {
    private Long roomId;
    private String roomName;
    private String location;
    private List<BookingResponse> bookings;
    private Boolean fullyAvailable;
}
