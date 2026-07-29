package com.yourcompany.roombooking.controller;

import com.yourcompany.roombooking.bot.RoomBookingBotHandler;
import com.yourcompany.roombooking.dto.request.SimulateRequest;
import com.yourcompany.roombooking.dto.response.SimulateResponse;
import com.yourcompany.roombooking.util.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BotSimulatorControllerTest {

    @Mock
    private RoomBookingBotHandler botHandler;

    @InjectMocks
    private BotSimulatorController botSimulatorController;

    @Test
    void simulate_returnsSuccessResponse() {
        SimulateResponse mockResponse = SimulateResponse.builder()
                .message("Available rooms: Conference Room A")
                .commandType("CHECK_AVAILABILITY")
                .success(true)
                .build();
        when(botHandler.processMessage(anyString(), anyString())).thenReturn(mockResponse);

        SimulateRequest request = SimulateRequest.builder()
                .text("check availability")
                .userEmail("admin@company.com")
                .build();

        ResponseEntity<ApiResponse<SimulateResponse>> result = botSimulatorController.simulate(request);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().isSuccess()).isTrue();
        assertThat(result.getBody().getData().getMessage()).isEqualTo("Available rooms: Conference Room A");
        assertThat(result.getBody().getData().getCommandType()).isEqualTo("CHECK_AVAILABILITY");
        assertThat(result.getBody().getData().isSuccess()).isTrue();
    }

    @Test
    void simulate_withHelpCommand_returnsHelpResponse() {
        SimulateResponse mockResponse = SimulateResponse.builder()
                .message("Here are the available commands...")
                .cardJson("{\"type\":\"AdaptiveCard\"}")
                .commandType("HELP")
                .success(true)
                .build();
        when(botHandler.processMessage("help", "user@company.com")).thenReturn(mockResponse);

        SimulateRequest request = SimulateRequest.builder()
                .text("help")
                .userEmail("user@company.com")
                .build();

        ResponseEntity<ApiResponse<SimulateResponse>> result = botSimulatorController.simulate(request);

        assertThat(result.getBody().getData().getCommandType()).isEqualTo("HELP");
        assertThat(result.getBody().getData().getCardJson()).isNotNull();
    }

    @Test
    void simulate_withUnknownCommand_returnsFailureResponse() {
        SimulateResponse mockResponse = SimulateResponse.builder()
                .message("Unknown command. Type 'help' for available commands.")
                .commandType("UNKNOWN")
                .success(false)
                .build();
        when(botHandler.processMessage(anyString(), anyString())).thenReturn(mockResponse);

        SimulateRequest request = SimulateRequest.builder()
                .text("xyz")
                .userEmail("user@company.com")
                .build();

        ResponseEntity<ApiResponse<SimulateResponse>> result = botSimulatorController.simulate(request);

        assertThat(result.getBody().getData().isSuccess()).isFalse();
        assertThat(result.getBody().getData().getCommandType()).isEqualTo("UNKNOWN");
    }

    @Test
    void simulate_delegatesCorrectTextAndEmail() {
        SimulateResponse mockResponse = SimulateResponse.builder()
                .message("ok")
                .success(true)
                .build();
        when(botHandler.processMessage("book room", "admin@company.com")).thenReturn(mockResponse);

        SimulateRequest request = SimulateRequest.builder()
                .text("book room")
                .userEmail("admin@company.com")
                .build();

        botSimulatorController.simulate(request);

        org.mockito.Mockito.verify(botHandler).processMessage("book room", "admin@company.com");
    }
}
