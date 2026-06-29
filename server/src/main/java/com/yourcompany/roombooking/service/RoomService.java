package com.yourcompany.roombooking.service;

import com.yourcompany.roombooking.dto.request.AvailabilityRequest;
import com.yourcompany.roombooking.dto.request.CreateRoomRequest;
import com.yourcompany.roombooking.dto.request.UpdateRoomRequest;
import com.yourcompany.roombooking.dto.response.AvailabilityResponse;
import com.yourcompany.roombooking.dto.response.RoomResponse;

import java.util.List;
import java.util.UUID;

public interface RoomService {

    RoomResponse createRoom(CreateRoomRequest request);

    RoomResponse getRoomById(UUID id);

    List<RoomResponse> getAllRooms();

    RoomResponse updateRoom(UUID id, UpdateRoomRequest request);

    void disableRoom(UUID id);

    AvailabilityResponse checkAvailability(AvailabilityRequest request);
}
