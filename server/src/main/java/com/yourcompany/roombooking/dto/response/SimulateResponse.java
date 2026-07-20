package com.yourcompany.roombooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulateResponse {
    private String message;
    private String cardJson;
    private String commandType;
    private boolean success;
}
