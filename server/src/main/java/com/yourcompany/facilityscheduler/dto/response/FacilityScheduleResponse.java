package com.yourcompany.facilityscheduler.dto.response;

import com.yourcompany.facilityscheduler.enums.FacilityScheduleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityScheduleResponse {
    private Long id;
    private Long roomId;
    private String roomName;
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private FacilityScheduleStatus status;
    private LocalDateTime createdAt;
}
