package com.yourcompany.facilityscheduler.controller;

import com.yourcompany.facilityscheduler.dto.request.CreateFacilityScheduleRequest;
import com.yourcompany.facilityscheduler.dto.response.AvailabilityResponse;
import com.yourcompany.facilityscheduler.dto.response.FacilityScheduleResponse;
import com.yourcompany.facilityscheduler.dto.response.EmployeeResponse;
import com.yourcompany.facilityscheduler.dto.response.RoomResponse;
import com.yourcompany.facilityscheduler.dto.response.TodayFacilitySchedulesResponse;
import com.yourcompany.facilityscheduler.service.FacilityScheduleService;
import com.yourcompany.facilityscheduler.util.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5175")
@RequiredArgsConstructor
@Tag(name = "Facility Scheduling")
public class FacilityScheduleController {

    private final FacilityScheduleService facilityScheduleService;

    @GetMapping("/employees")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getAllEmployees() {
        List<EmployeeResponse> employees = facilityScheduleService.getAllEmployees();
        return ResponseEntity.ok(
                ApiResponse.success("Employees fetched successfully", employees));
    }

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAllRooms() {
        List<RoomResponse> rooms = facilityScheduleService.getAllRooms();
        return ResponseEntity.ok(
                ApiResponse.success("Rooms fetched successfully", rooms));
    }

    @GetMapping("/facility-schedules/availability")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkAvailability(
            @RequestParam Long roomId,
            @RequestParam LocalDate date,
            @RequestParam LocalTime startTime,
            @RequestParam LocalTime endTime) {
        AvailabilityResponse response = facilityScheduleService.checkAvailability(roomId, date, startTime, endTime);
        return ResponseEntity.ok(
                ApiResponse.success("Availability checked", response));
    }

    @PostMapping("/facility-schedules")
    public ResponseEntity<ApiResponse<FacilityScheduleResponse>> createFacilitySchedule(
            @Valid @RequestBody CreateFacilityScheduleRequest request) {
        FacilityScheduleResponse response = facilityScheduleService.createFacilitySchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Facility scheduled successfully", response));
    }

    @GetMapping("/facility-schedules/today")
    public ResponseEntity<ApiResponse<List<TodayFacilitySchedulesResponse>>> getTodayFacilitySchedules() {
        List<TodayFacilitySchedulesResponse> response = facilityScheduleService.getTodayFacilitySchedules();
        return ResponseEntity.ok(
                ApiResponse.success("Today's schedules fetched", response));
    }

    @GetMapping("/facility-schedules/recent")
    public ResponseEntity<ApiResponse<List<FacilityScheduleResponse>>> getRecentFacilitySchedules(
            @RequestParam(defaultValue = "5") int days) {
        List<FacilityScheduleResponse> response = facilityScheduleService.getRecentFacilitySchedules(days);
        return ResponseEntity.ok(
                ApiResponse.success("Recent schedules fetched", response));
    }

    @DeleteMapping("/facility-schedules/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelFacilitySchedule(
            @PathVariable Long id,
            @RequestParam Long employeeId) {
        facilityScheduleService.cancelFacilitySchedule(id, employeeId);
        return ResponseEntity.ok(
                ApiResponse.success("Facility schedule cancelled successfully", null));
    }
}
