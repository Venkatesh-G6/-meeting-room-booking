package com.yourcompany.roombooking.controller;

import com.yourcompany.roombooking.dto.request.AvailabilityRequest;
import com.yourcompany.roombooking.dto.request.CreateRoomRequest;
import com.yourcompany.roombooking.dto.request.UpdateRoomRequest;
import com.yourcompany.roombooking.dto.response.AvailabilityResponse;
import com.yourcompany.roombooking.dto.response.PagedResponse;
import com.yourcompany.roombooking.dto.response.RoomResponse;
import com.yourcompany.roombooking.service.RoomService;
import com.yourcompany.roombooking.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
@Tag(name = "Room Management")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @Operation(summary = "Check room availability")
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> getAvailableRooms(
            @Parameter(description = "Date in format yyyy-MM-dd", example = "2025-07-01")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @Parameter(description = "Start time in format HH:mm:ss", example = "10:00:00")
            @RequestParam @DateTimeFormat(pattern = "HH:mm:ss") LocalTime startTime,
            @Parameter(description = "End time in format HH:mm:ss", example = "11:00:00")
            @RequestParam @DateTimeFormat(pattern = "HH:mm:ss") LocalTime endTime,
            @Parameter(description = "Minimum room capacity required", example = "5")
            @RequestParam(defaultValue = "1") Integer minCapacity) {
        AvailabilityRequest request = AvailabilityRequest.builder()
                .date(date)
                .startTime(startTime)
                .endTime(endTime)
                .minCapacity(minCapacity)
                .build();
        AvailabilityResponse response = roomService.checkAvailability(request);
        return ResponseEntity.ok(ApiResponse.success("Available rooms fetched successfully", response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        RoomResponse response = roomService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Room created successfully", response));
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<RoomResponse>>> getAllRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<RoomResponse> rooms = roomService.getAllRooms(page, size);
        return ResponseEntity.ok(ApiResponse.success("Rooms fetched successfully", rooms));
    }

    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomById(@PathVariable UUID id) {
        RoomResponse room = roomService.getRoomById(id);
        return ResponseEntity.ok(ApiResponse.success("Room fetched successfully", room));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoomRequest request) {
        RoomResponse response = roomService.updateRoom(id, request);
        return ResponseEntity.ok(ApiResponse.success("Room updated successfully", response));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/disable")
    public ResponseEntity<ApiResponse<Void>> disableRoom(@PathVariable UUID id) {
        roomService.disableRoom(id);
        return ResponseEntity.ok(ApiResponse.success("Room disabled successfully", null));
    }
}
