package com.yourcompany.roombooking.service;

import com.yourcompany.roombooking.dto.request.AvailabilityRequest;
import com.yourcompany.roombooking.dto.request.CreateRoomRequest;
import com.yourcompany.roombooking.dto.request.UpdateRoomRequest;
import com.yourcompany.roombooking.dto.response.AvailabilityResponse;
import com.yourcompany.roombooking.dto.response.RoomResponse;

import java.util.List;

public interface RoomService {

    RoomResponse createRoom(CreateRoomRequest request);

    RoomResponse getRoomById(Long id);

    List<RoomResponse> getAllRooms();

    RoomResponse updateRoom(Long id, UpdateRoomRequest request);

    void disableRoom(Long id);

    AvailabilityResponse checkAvailability(AvailabilityRequest request);
}
