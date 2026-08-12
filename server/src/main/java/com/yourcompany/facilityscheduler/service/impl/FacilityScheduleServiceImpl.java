package com.yourcompany.facilityscheduler.service.impl;

import com.yourcompany.facilityscheduler.dto.request.CreateFacilityScheduleRequest;
import com.yourcompany.facilityscheduler.dto.response.AvailabilityResponse;
import com.yourcompany.facilityscheduler.dto.response.FacilityScheduleResponse;
import com.yourcompany.facilityscheduler.dto.response.EmployeeResponse;
import com.yourcompany.facilityscheduler.dto.response.RoomResponse;
import com.yourcompany.facilityscheduler.dto.response.TodayFacilitySchedulesResponse;
import com.yourcompany.facilityscheduler.entity.FacilitySchedule;
import com.yourcompany.facilityscheduler.entity.Employee;
import com.yourcompany.facilityscheduler.entity.Room;
import com.yourcompany.facilityscheduler.enums.FacilityScheduleStatus;
import com.yourcompany.facilityscheduler.enums.RoomStatus;
import com.yourcompany.facilityscheduler.exception.FacilityScheduleException;
import com.yourcompany.facilityscheduler.exception.ResourceNotFoundException;
import com.yourcompany.facilityscheduler.repository.FacilityScheduleRepository;
import com.yourcompany.facilityscheduler.repository.EmployeeRepository;
import com.yourcompany.facilityscheduler.repository.RoomRepository;
import com.yourcompany.facilityscheduler.service.FacilityScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacilityScheduleServiceImpl implements FacilityScheduleService {

    private final FacilityScheduleRepository facilityScheduleRepository;
    private final EmployeeRepository employeeRepository;
    private final RoomRepository roomRepository;

    @Override
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAllByActiveTrue().stream()
                .map(this::mapToEmployeeResponse)
                .toList();
    }

    @Override
    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAllByStatus(RoomStatus.AVAILABLE).stream()
                .map(this::mapToRoomResponse)
                .toList();
    }

    @Override
    public AvailabilityResponse checkAvailability(Long roomId,
                                                   LocalDate date,
                                                   LocalTime startTime,
                                                   LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new FacilityScheduleException("Start time must be before end time");
        }

        if (date.isBefore(LocalDate.now())) {
            throw new FacilityScheduleException("Cannot schedule for a past date");
        }

        LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(date, endTime);

        List<FacilitySchedule> overlapping = facilityScheduleRepository.findOverlappingFacilitySchedules(
                roomId, FacilityScheduleStatus.CONFIRMED, startDateTime, endDateTime);

        if (overlapping.isEmpty()) {
            return AvailabilityResponse.builder()
                    .available(true)
                    .roomId(roomId)
                    .date(date)
                    .requestedStart(startTime)
                    .requestedEnd(endTime)
                    .message("Room is available")
                    .build();
        }

        FacilitySchedule conflicting = overlapping.get(0);
        LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);
        Optional<FacilitySchedule> nextSlot = facilityScheduleRepository.findNextAvailableSlot(
                roomId, FacilityScheduleStatus.CONFIRMED, conflicting.getEndTime(), endOfDay);

        LocalTime suggestedStart = null;
        LocalTime suggestedEnd = null;

        if (nextSlot.isPresent()) {
            suggestedStart = conflicting.getEndTime().toLocalTime();
            long durationMinutes = Duration.between(startTime, endTime).toMinutes();
            suggestedEnd = suggestedStart.plusMinutes(durationMinutes);
        }

        return AvailabilityResponse.builder()
                .available(false)
                .roomId(roomId)
                .roomName(conflicting.getRoom().getRoomName())
                .date(date)
                .requestedStart(startTime)
                .requestedEnd(endTime)
                .conflictingFacilitySchedule(mapToFacilityScheduleResponse(conflicting))
                .suggestedStartTime(suggestedStart)
                .suggestedEndTime(suggestedEnd)
                .message("Room is scheduled from "
                        + conflicting.getStartTime().toLocalTime()
                        + " to "
                        + conflicting.getEndTime().toLocalTime())
                .build();
    }

    @Override
    public FacilityScheduleResponse createFacilitySchedule(CreateFacilityScheduleRequest request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Room not found with id: " + request.getRoomId()));

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with id: " + request.getEmployeeId()));

        AvailabilityResponse availability = checkAvailability(
                request.getRoomId(), request.getDate(),
                request.getStartTime(), request.getEndTime());

        if (!availability.getAvailable()) {
            throw new FacilityScheduleException(availability.getMessage());
        }

        FacilitySchedule facilitySchedule = FacilitySchedule.builder()
                .room(room)
                .employee(employee)
                .title(request.getTitle())
                .startTime(LocalDateTime.of(request.getDate(), request.getStartTime()))
                .endTime(LocalDateTime.of(request.getDate(), request.getEndTime()))
                .status(FacilityScheduleStatus.CONFIRMED)
                .build();

        FacilitySchedule saved = facilityScheduleRepository.save(facilitySchedule);
        return mapToFacilityScheduleResponse(saved);
    }

    @Override
    public List<TodayFacilitySchedulesResponse> getTodayFacilitySchedules() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        List<FacilitySchedule> todayFacilitySchedules = facilityScheduleRepository.findTodayFacilitySchedules(startOfDay, endOfDay, FacilityScheduleStatus.CONFIRMED);

        Map<Long, List<FacilitySchedule>> facilitySchedulesByRoom = todayFacilitySchedules.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getRoom().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<TodayFacilitySchedulesResponse> result = new ArrayList<>();

        for (Map.Entry<Long, List<FacilitySchedule>> entry : facilitySchedulesByRoom.entrySet()) {
            List<FacilitySchedule> facilitySchedules = entry.getValue();
            Room room = facilitySchedules.get(0).getRoom();
            List<FacilityScheduleResponse> facilityScheduleResponses = facilitySchedules.stream()
                    .map(this::mapToFacilityScheduleResponse)
                    .toList();

            result.add(TodayFacilitySchedulesResponse.builder()
                    .roomId(room.getId())
                    .roomName(room.getRoomName())
                    .location(room.getLocation())
                    .facilitySchedules(facilityScheduleResponses)
                    .fullyAvailable(false)
                    .build());
        }

        List<Room> availableRooms = roomRepository.findAllByStatus(RoomStatus.AVAILABLE);
        for (Room room : availableRooms) {
            if (!facilitySchedulesByRoom.containsKey(room.getId())) {
                result.add(TodayFacilitySchedulesResponse.builder()
                        .roomId(room.getId())
                        .roomName(room.getRoomName())
                        .location(room.getLocation())
                        .facilitySchedules(List.of())
                        .fullyAvailable(true)
                        .build());
            }
        }

        result.sort((a, b) -> a.getRoomName().compareToIgnoreCase(b.getRoomName()));
        return result;
    }

    @Override
    public List<FacilityScheduleResponse> getRecentFacilitySchedules(int days) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
        return facilityScheduleRepository.findRecentFacilitySchedules(fromDate, FacilityScheduleStatus.CONFIRMED).stream()
                .map(this::mapToFacilityScheduleResponse)
                .toList();
    }

    @Override
    public void cancelFacilitySchedule(Long facilityScheduleId, Long employeeId) {
        FacilitySchedule facilitySchedule = facilityScheduleRepository.findById(facilityScheduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Facility schedule not found with id: " + facilityScheduleId));

        if (!facilitySchedule.getEmployee().getId().equals(employeeId)) {
            throw new FacilityScheduleException("You can only cancel your own facility schedules");
        }

        if (facilitySchedule.getStatus() == FacilityScheduleStatus.CANCELLED) {
            throw new FacilityScheduleException("Facility schedule is already cancelled");
        }

        facilitySchedule.setStatus(FacilityScheduleStatus.CANCELLED);
        facilityScheduleRepository.save(facilitySchedule);
    }

    private FacilityScheduleResponse mapToFacilityScheduleResponse(FacilitySchedule facilitySchedule) {
        return FacilityScheduleResponse.builder()
                .id(facilitySchedule.getId())
                .roomId(facilitySchedule.getRoom().getId())
                .roomName(facilitySchedule.getRoom().getRoomName())
                .employeeId(facilitySchedule.getEmployee().getId())
                .employeeName(facilitySchedule.getEmployee().getName())
                .employeeEmail(facilitySchedule.getEmployee().getEmail())
                .title(facilitySchedule.getTitle())
                .startTime(facilitySchedule.getStartTime())
                .endTime(facilitySchedule.getEndTime())
                .status(facilitySchedule.getStatus())
                .createdAt(facilitySchedule.getCreatedAt())
                .build();
    }

    private RoomResponse mapToRoomResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .roomName(room.getRoomName())
                .capacity(room.getCapacity())
                .location(room.getLocation())
                .status(room.getStatus())
                .build();
    }

    private EmployeeResponse mapToEmployeeResponse(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .email(employee.getEmail())
                .department(employee.getDepartment())
                .build();
    }
}
