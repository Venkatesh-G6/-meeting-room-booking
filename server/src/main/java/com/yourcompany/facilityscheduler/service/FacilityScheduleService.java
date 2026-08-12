package com.yourcompany.facilityscheduler.service;

import com.yourcompany.facilityscheduler.dto.request.CreateFacilityScheduleRequest;
import com.yourcompany.facilityscheduler.dto.response.AvailabilityResponse;
import com.yourcompany.facilityscheduler.dto.response.FacilityScheduleResponse;
import com.yourcompany.facilityscheduler.dto.response.EmployeeResponse;
import com.yourcompany.facilityscheduler.dto.response.RoomResponse;
import com.yourcompany.facilityscheduler.dto.response.TodayFacilitySchedulesResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface FacilityScheduleService {

    List<EmployeeResponse> getAllEmployees();

    List<RoomResponse> getAllRooms();

    AvailabilityResponse checkAvailability(Long roomId,
                                           LocalDate date,
                                           LocalTime startTime,
                                           LocalTime endTime);

    FacilityScheduleResponse createFacilitySchedule(CreateFacilityScheduleRequest request);

    List<TodayFacilitySchedulesResponse> getTodayFacilitySchedules();

    List<FacilityScheduleResponse> getRecentFacilitySchedules(int days);

    void cancelFacilitySchedule(Long facilityScheduleId, Long employeeId);
}
