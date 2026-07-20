package com.yourcompany.roombooking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarEventRequest {
    private Long bookingId;
    private String subject;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private String organizerEmail;
    private List<String> attendeeEmails;
    @Builder.Default
    private Boolean isOnlineMeeting = false;
}
