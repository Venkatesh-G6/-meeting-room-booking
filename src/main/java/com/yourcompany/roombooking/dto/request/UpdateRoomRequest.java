package com.yourcompany.roombooking.dto.request;

import com.yourcompany.roombooking.enums.RoomType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRoomRequest {

    @NotBlank
    private String roomName;

    @NotNull
    private RoomType roomType;

    @NotNull
    @Min(1)
    private Integer capacity;

    private String location;
}
