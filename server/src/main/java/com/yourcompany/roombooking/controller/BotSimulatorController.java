package com.yourcompany.roombooking.controller;

import com.yourcompany.roombooking.bot.RoomBookingBotHandler;
import com.yourcompany.roombooking.dto.request.SimulateRequest;
import com.yourcompany.roombooking.dto.response.SimulateResponse;
import com.yourcompany.roombooking.util.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * Dev only endpoint for testing
 * bot logic without Microsoft Teams.
 * Disabled in prod profile.
 * Use /bot-simulator page in React UI.
 */
@RestController
@RequestMapping("/api/messages")
@Profile("dev")
@Tag(name = "Bot Simulator")
public class BotSimulatorController {

    private final RoomBookingBotHandler botHandler;

    public BotSimulatorController(RoomBookingBotHandler botHandler) {
        this.botHandler = botHandler;
    }

    @PostMapping("/simulate")
    public ResponseEntity<ApiResponse<SimulateResponse>> simulate(@RequestBody SimulateRequest request) {
        SimulateResponse response = botHandler.processMessage(request.getText(), request.getUserEmail());
        return ResponseEntity.ok(ApiResponse.success("Simulated successfully", response));
    }
}
