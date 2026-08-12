package com.yourcompany.facilityscheduler.dto.response;

import com.yourcompany.facilityscheduler.enums.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {
    private Long id;
    private String roomName;
    private Integer capacity;
    private String location;
    private RoomStatus status;
}
