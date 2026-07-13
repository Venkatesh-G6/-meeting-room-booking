package com.yourcompany.roombooking.service.impl;

import com.yourcompany.roombooking.audit.AuditService;
import com.yourcompany.roombooking.dto.request.AvailabilityRequest;
import com.yourcompany.roombooking.dto.request.CreateRoomRequest;
import com.yourcompany.roombooking.dto.request.UpdateRoomRequest;
import com.yourcompany.roombooking.dto.response.AvailabilityResponse;
import com.yourcompany.roombooking.dto.response.RoomResponse;
import com.yourcompany.roombooking.entity.Room;
import com.yourcompany.roombooking.enums.RoomType;
import com.yourcompany.roombooking.exception.BookingException;
import com.yourcompany.roombooking.exception.ResourceNotFoundException;
import com.yourcompany.roombooking.repository.BookingRepository;
import com.yourcompany.roombooking.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private RoomServiceImpl roomService;

    private UUID roomId;
    private Room room;

    @BeforeEach
    void setUp() {
        roomId = UUID.randomUUID();
        room = Room.builder()
                .id(roomId)
                .roomName("Conference Room A")
                .roomType(RoomType.MEETING)
                .capacity(10)
                .location("Floor 2")
                .active(true)
                .build();
    }

    @Test
    void createRoom_success() {
        CreateRoomRequest request = CreateRoomRequest.builder()
                .roomName("Conference Room A")
                .roomType(RoomType.MEETING)
                .capacity(10)
                .location("Floor 2")
                .build();
        when(roomRepository.existsByRoomName("Conference Room A")).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> {
            Room r = invocation.getArgument(0);
            r.setId(roomId);
            return r;
        });

        RoomResponse response = roomService.createRoom(request, "admin@company.com");

        assertThat(response.getId()).isEqualTo(roomId);
        assertThat(response.getRoomName()).isEqualTo("Conference Room A");
        verify(auditService, times(1)).log(eq("admin@company.com"), any(), eq("ROOM"), anyString(), any());
    }

    @Test
    void createRoom_duplicateName_throwsException() {
        CreateRoomRequest request = CreateRoomRequest.builder()
                .roomName("Conference Room A")
                .roomType(RoomType.MEETING)
                .capacity(10)
                .build();
        when(roomRepository.existsByRoomName("Conference Room A")).thenReturn(true);

        assertThatThrownBy(() -> roomService.createRoom(request, "admin@company.com"))
                .isInstanceOf(BookingException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void getRoomById_success() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        RoomResponse response = roomService.getRoomById(roomId);

        assertThat(response.getId()).isEqualTo(roomId);
        assertThat(response.getRoomName()).isEqualTo("Conference Room A");
    }

    @Test
    void getRoomById_notFound_throwsException() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getRoomById(roomId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateRoom_success() {
        UpdateRoomRequest request = UpdateRoomRequest.builder()
                .roomName("Conference Room B")
                .roomType(RoomType.TRAINING)
                .capacity(20)
                .location("Floor 3")
                .build();
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        RoomResponse response = roomService.updateRoom(roomId, request, "admin@company.com");

        assertThat(response.getRoomName()).isEqualTo("Conference Room B");
        assertThat(response.getCapacity()).isEqualTo(20);
        verify(auditService, times(1)).log(eq("admin@company.com"), any(), eq("ROOM"), anyString(), any());
    }

    @Test
    void updateRoom_notFound_throwsException() {
        UpdateRoomRequest request = UpdateRoomRequest.builder()
                .roomName("Conference Room B")
                .roomType(RoomType.TRAINING)
                .capacity(20)
                .build();
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.updateRoom(roomId, request, "admin@company.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void disableRoom_success() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        roomService.disableRoom(roomId, "admin@company.com");

        assertThat(room.getActive()).isFalse();
        verify(auditService, times(1)).log(eq("admin@company.com"), any(), eq("ROOM"), anyString(), any());
    }

    @Test
    void disableRoom_notFound_throwsException() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.disableRoom(roomId, "admin@company.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void checkAvailability_noBookedRooms_returnsAllActiveRoomsWithCapacity() {
        AvailabilityRequest request = AvailabilityRequest.builder()
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .minCapacity(5)
                .build();
        when(bookingRepository.findBookedRoomIds(any(), any())).thenReturn(List.of());
        when(roomRepository.findAllByActiveTrueAndCapacityGreaterThanEqual(5)).thenReturn(List.of(room));

        AvailabilityResponse response = roomService.checkAvailability(request);

        assertThat(response.getTotalAvailable()).isEqualTo(1);
        assertThat(response.getAvailableRooms().get(0).getId()).isEqualTo(roomId);
        verify(roomRepository, never()).findAvailableRooms(any(), any());
    }

    @Test
    void checkAvailability_withBookedRooms_excludesBookedRooms() {
        AvailabilityRequest request = AvailabilityRequest.builder()
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .minCapacity(5)
                .build();
        UUID bookedRoomId = UUID.randomUUID();
        when(bookingRepository.findBookedRoomIds(any(), any())).thenReturn(List.of(bookedRoomId));
        when(roomRepository.findAvailableRooms(5, List.of(bookedRoomId))).thenReturn(List.of(room));

        AvailabilityResponse response = roomService.checkAvailability(request);

        assertThat(response.getTotalAvailable()).isEqualTo(1);
        verify(roomRepository, never()).findAllByActiveTrueAndCapacityGreaterThanEqual(any());
    }

    @Test
    void checkAvailability_pastDate_throwsException() {
        AvailabilityRequest request = AvailabilityRequest.builder()
                .date(LocalDate.now().minusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .minCapacity(1)
                .build();

        assertThatThrownBy(() -> roomService.checkAvailability(request))
                .isInstanceOf(BookingException.class)
                .hasMessageContaining("past");
    }

    @Test
    void checkAvailability_startAfterEnd_throwsException() {
        AvailabilityRequest request = AvailabilityRequest.builder()
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(12, 0))
                .endTime(LocalTime.of(11, 0))
                .minCapacity(1)
                .build();

        assertThatThrownBy(() -> roomService.checkAvailability(request))
                .isInstanceOf(BookingException.class)
                .hasMessageContaining("Start time must be before end time");
    }
}
