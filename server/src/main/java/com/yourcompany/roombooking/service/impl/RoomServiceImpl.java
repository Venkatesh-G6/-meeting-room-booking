package com.yourcompany.roombooking.service.impl;

import com.yourcompany.roombooking.audit.AuditMeta;
import com.yourcompany.roombooking.audit.AuditService;
import com.yourcompany.roombooking.dto.request.AvailabilityRequest;
import com.yourcompany.roombooking.dto.request.CreateRoomRequest;
import com.yourcompany.roombooking.dto.request.UpdateRoomRequest;
import com.yourcompany.roombooking.dto.response.AvailabilityResponse;
import com.yourcompany.roombooking.dto.response.PagedResponse;
import com.yourcompany.roombooking.dto.response.RoomResponse;
import com.yourcompany.roombooking.entity.Room;
import com.yourcompany.roombooking.enums.AuditAction;
import com.yourcompany.roombooking.exception.BookingException;
import com.yourcompany.roombooking.exception.ResourceNotFoundException;
import com.yourcompany.roombooking.repository.BookingRepository;
import com.yourcompany.roombooking.repository.RoomRepository;
import com.yourcompany.roombooking.service.RoomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final AuditService auditService;

    public RoomServiceImpl(RoomRepository roomRepository, BookingRepository bookingRepository, AuditService auditService) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.auditService = auditService;
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

        // TODO Phase 9: Replace "system" with
        // JWT extracted email from SecurityContext
        auditService.log(
                "system",
                AuditAction.ROOM_CREATED,
                "ROOM",
                savedRoom.getId().toString(),
                new AuditMeta.RoomMeta(
                        savedRoom.getId(),
                        savedRoom.getRoomName(),
                        savedRoom.getRoomType().name(),
                        savedRoom.getCapacity()
                )
        );

        return mapToResponse(savedRoom);
    }

    @Override
    public RoomResponse getRoomById(UUID id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        return mapToResponse(room);
    }

    @Override
    public PagedResponse<RoomResponse> getAllRooms(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Room> roomPage = roomRepository.findAllByActiveTrue(pageable);
        List<RoomResponse> roomResponses = roomPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return PagedResponse.of(roomPage, roomResponses);
    }

    @Override
    public RoomResponse updateRoom(UUID id, UpdateRoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        room.setRoomName(request.getRoomName());
        room.setRoomType(request.getRoomType());
        room.setCapacity(request.getCapacity());
        room.setLocation(request.getLocation());

        Room updatedRoom = roomRepository.save(room);

        // TODO Phase 9: Replace "system" with
        // JWT extracted email from SecurityContext
        auditService.log(
                "system",
                AuditAction.ROOM_UPDATED,
                "ROOM",
                updatedRoom.getId().toString(),
                new AuditMeta.RoomMeta(
                        updatedRoom.getId(),
                        updatedRoom.getRoomName(),
                        updatedRoom.getRoomType().name(),
                        updatedRoom.getCapacity()
                )
        );

        return mapToResponse(updatedRoom);
    }

    @Override
    public void disableRoom(UUID id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        room.setActive(false);
        roomRepository.save(room);

        // TODO Phase 9: Replace "system" with
        // JWT extracted email from SecurityContext
        auditService.log(
                "system",
                AuditAction.ROOM_DISABLED,
                "ROOM",
                room.getId().toString(),
                new AuditMeta.RoomMeta(
                        room.getId(),
                        room.getRoomName(),
                        room.getRoomType().name(),
                        room.getCapacity()
                )
        );
    }

    @Override
    public AvailabilityResponse checkAvailability(AvailabilityRequest request) {
        validateAvailabilityRequest(request);

        LocalDateTime startDateTime = request.toStartDateTime();
        LocalDateTime endDateTime = request.toEndDateTime();

        List<UUID> bookedRoomIds = bookingRepository.findBookedRoomIds(startDateTime, endDateTime);

        List<Room> rooms;
        if (bookedRoomIds.isEmpty()) {
            rooms = roomRepository.findAllByActiveTrueAndCapacityGreaterThanEqual(request.getMinCapacity());
        } else {
            rooms = roomRepository.findAvailableRooms(request.getMinCapacity(), bookedRoomIds);
        }

        List<RoomResponse> availableRooms = rooms.stream()
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
