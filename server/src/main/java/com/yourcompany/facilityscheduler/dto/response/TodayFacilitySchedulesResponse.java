package com.yourcompany.facilityscheduler.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodayFacilitySchedulesResponse {
    private Long roomId;
    private String roomName;
    private String location;
    private List<FacilityScheduleResponse> facilitySchedules;
    private Boolean fullyAvailable;
}
