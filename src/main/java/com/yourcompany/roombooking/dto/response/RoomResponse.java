package com.yourcompany.roombooking.dto.response;

import com.yourcompany.roombooking.enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {

    private Long id;
    private String roomName;
    private RoomType roomType;
    private Integer capacity;
    private String location;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
