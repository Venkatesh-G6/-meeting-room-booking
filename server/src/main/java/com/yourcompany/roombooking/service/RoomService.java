package com.yourcompany.roombooking.service;

import com.yourcompany.roombooking.dto.request.AvailabilityRequest;
import com.yourcompany.roombooking.dto.request.CreateRoomRequest;
import com.yourcompany.roombooking.dto.request.UpdateRoomRequest;
import com.yourcompany.roombooking.dto.response.AvailabilityResponse;
import com.yourcompany.roombooking.dto.response.PagedResponse;
import com.yourcompany.roombooking.dto.response.RoomResponse;

import java.util.List;
import java.util.UUID;

public interface RoomService {

    RoomResponse createRoom(CreateRoomRequest request, String actorEmail);

    RoomResponse getRoomById(UUID id);

    PagedResponse<RoomResponse> getAllRooms(int page, int size);

    RoomResponse updateRoom(UUID id, UpdateRoomRequest request, String actorEmail);

    void disableRoom(UUID id, String actorEmail);

    AvailabilityResponse checkAvailability(AvailabilityRequest request);
}
