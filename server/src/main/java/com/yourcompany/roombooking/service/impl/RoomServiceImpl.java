package com.yourcompany.roombooking.service.impl;

import com.yourcompany.roombooking.dto.request.AvailabilityRequest;
import com.yourcompany.roombooking.dto.request.CreateRoomRequest;
import com.yourcompany.roombooking.dto.request.UpdateRoomRequest;
import com.yourcompany.roombooking.dto.response.AvailabilityResponse;
import com.yourcompany.roombooking.dto.response.RoomResponse;
import com.yourcompany.roombooking.entity.Room;
import com.yourcompany.roombooking.exception.BookingException;
import com.yourcompany.roombooking.exception.ResourceNotFoundException;
import com.yourcompany.roombooking.repository.BookingRepository;
import com.yourcompany.roombooking.repository.RoomRepository;
import com.yourcompany.roombooking.service.RoomService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public RoomServiceImpl(RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public RoomResponse createRoom(CreateRoomRequest request) {
        if (roomRepository.existsByRoomName(request.getRoomName())) {
            throw new BookingException("Room name already exists");
        }

        Room room = Room.builder()
                .roomName(request.getRoomName())
                .roomType(request.getRoomType())
                .capacity(request.getCapacity())
                .location(request.getLocation())
                .active(true)
                .build();

        Room savedRoom = roomRepository.save(room);
        return mapToResponse(savedRoom);
    }

    @Override
    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        return mapToResponse(room);
    }

    @Override
    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAllByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RoomResponse updateRoom(Long id, UpdateRoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        room.setRoomName(request.getRoomName());
        room.setRoomType(request.getRoomType());
        room.setCapacity(request.getCapacity());
        room.setLocation(request.getLocation());

        Room updatedRoom = roomRepository.save(room);
        return mapToResponse(updatedRoom);
    }

    @Override
    public void disableRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        room.setActive(false);
        roomRepository.save(room);
    }

    @Override
    public AvailabilityResponse checkAvailability(AvailabilityRequest request) {
        validateAvailabilityRequest(request);

        LocalDateTime startDateTime = request.toStartDateTime();
        LocalDateTime endDateTime = request.toEndDateTime();

        List<Long> bookedRoomIds = bookingRepository.findBookedRoomIds(startDateTime, endDateTime);
        if (bookedRoomIds.isEmpty()) {
            bookedRoomIds = null;
        }

        List<RoomResponse> availableRooms = roomRepository.findAvailableRooms(request.getMinCapacity(), bookedRoomIds).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return AvailabilityResponse.builder()
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .availableRooms(availableRooms)
                .totalAvailable(availableRooms.size())
                .build();
    }

    private void validateAvailabilityRequest(AvailabilityRequest request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BookingException("Start time must be before end time");
        }
        if (request.getDate().isBefore(LocalDate.now())) {
            throw new BookingException("Date cannot be in the past");
        }
        if (request.getMinCapacity() < 1) {
            throw new BookingException("Minimum capacity must be at least 1");
        }
    }

    private RoomResponse mapToResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .roomName(room.getRoomName())
                .roomType(room.getRoomType())
                .capacity(room.getCapacity())
                .location(room.getLocation())
                .active(room.getActive())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }
}
