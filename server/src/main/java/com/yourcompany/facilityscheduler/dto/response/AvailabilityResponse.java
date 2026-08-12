package com.yourcompany.facilityscheduler.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityResponse {
    private Boolean available;
    private Long roomId;
    private String roomName;
    private LocalDate date;
    private LocalTime requestedStart;
    private LocalTime requestedEnd;
    private FacilityScheduleResponse conflictingFacilitySchedule;
    private LocalTime suggestedStartTime;
    private LocalTime suggestedEndTime;
    private String message;
}
