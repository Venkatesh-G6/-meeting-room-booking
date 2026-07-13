package com.yourcompany.roombooking.service.impl;

import com.yourcompany.roombooking.audit.AuditService;
import com.yourcompany.roombooking.dto.request.CreateBookingRequest;
import com.yourcompany.roombooking.dto.response.BookingResponse;
import com.yourcompany.roombooking.entity.Booking;
import com.yourcompany.roombooking.entity.Room;
import com.yourcompany.roombooking.enums.BookingStatus;
import com.yourcompany.roombooking.enums.RoomType;
import com.yourcompany.roombooking.exception.BookingException;
import com.yourcompany.roombooking.exception.ResourceNotFoundException;
import com.yourcompany.roombooking.repository.BookingRepository;
import com.yourcompany.roombooking.repository.RoomRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private Room activeRoom;
    private UUID roomId;
    private UUID bookingId;

    @BeforeEach
    void setUp() {
        roomId = UUID.randomUUID();
        bookingId = UUID.randomUUID();
        activeRoom = Room.builder()
                .id(roomId)
                .roomName("Test Room")
                .roomType(RoomType.MEETING)
                .capacity(10)
                .location("Floor 1")
                .active(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private CreateBookingRequest validRequest() {
        return CreateBookingRequest.builder()
                .roomId(roomId)
                .title("Team Sync")
                .attendeeCount(5)
                .startTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0))
                .endTime(LocalDateTime.now().plusDays(1).withHour(11).withMinute(0))
                .build();
    }

    @Test
    void createBooking_success() {
        CreateBookingRequest request = validRequest();
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(activeRoom));
        when(bookingRepository.findDuplicateBooking(any(), anyString(), any(), any())).thenReturn(Optional.empty());
        when(bookingRepository.findOverlappingBookings(any(), any(), any())).thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(bookingId);
            return b;
        });

        BookingResponse response = bookingService.createBooking(request, "alice@company.com");

        assertThat(response.getId()).isEqualTo(bookingId);
        assertThat(response.getBookedBy()).isEqualTo("alice@company.com");
        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(auditService, times(1)).log(eq("alice@company.com"), any(), eq("BOOKING"), anyString(), any());
    }

    @Test
    void createBooking_roomNotFound_throwsException() {
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(validRequest(), "alice@company.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createBooking_inactiveRoom_throwsException() {
        activeRoom.setActive(false);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(activeRoom));

        assertThatThrownBy(() -> bookingService.createBooking(validRequest(), "alice@company.com"))
                .isInstanceOf(BookingException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void createBooking_attendeeCountExceedsCapacity_throwsException() {
        CreateBookingRequest request = validRequest();
        request.setAttendeeCount(50);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(activeRoom));

        assertThatThrownBy(() -> bookingService.createBooking(request, "alice@company.com"))
                .isInstanceOf(BookingException.class)
                .hasMessageContaining("exceeds room capacity");
    }

    @Test
    void createBooking_pastStartTime_throwsException() {
        CreateBookingRequest request = validRequest();
        request.setStartTime(LocalDateTime.now().minusDays(1));
        request.setEndTime(LocalDateTime.now().minusDays(1).plusHours(1));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(activeRoom));

        assertThatThrownBy(() -> bookingService.createBooking(request, "alice@company.com"))
                .isInstanceOf(BookingException.class)
                .hasMessageContaining("cannot be made in the past");
    }

    @Test
    void createBooking_startAfterEnd_throwsException() {
        CreateBookingRequest request = validRequest();
        request.setStartTime(LocalDateTime.now().plusDays(1).withHour(12));
        request.setEndTime(LocalDateTime.now().plusDays(1).withHour(11));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(activeRoom));

        assertThatThrownBy(() -> bookingService.createBooking(request, "alice@company.com"))
                .isInstanceOf(BookingException.class)
                .hasMessageContaining("Start time must be before end time");
    }

    @Test
    void createBooking_overlappingBooking_throwsException() {
        CreateBookingRequest request = validRequest();
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(activeRoom));
        when(bookingRepository.findDuplicateBooking(any(), anyString(), any(), any())).thenReturn(Optional.empty());
        when(bookingRepository.findOverlappingBookings(any(), any(), any()))
                .thenReturn(List.of(new Booking()));

        assertThatThrownBy(() -> bookingService.createBooking(request, "alice@company.com"))
                .isInstanceOf(BookingException.class)
                .hasMessageContaining("already booked");
    }

    @Test
    void cancelBooking_ownerCancels_success() {
        Booking booking = Booking.builder()
                .id(bookingId)
                .room(activeRoom)
                .bookedBy("alice@company.com")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .status(BookingStatus.CONFIRMED)
                .build();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        bookingService.cancelBooking(bookingId, "alice@company.com");

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(auditService, times(1)).log(eq("alice@company.com"), any(), eq("BOOKING"), anyString(), any());
    }

    @Test
    void cancelBooking_notOwnerNotAdmin_throwsException() {
        Booking booking = Booking.builder()
                .id(bookingId)
                .room(activeRoom)
                .bookedBy("alice@company.com")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .status(BookingStatus.CONFIRMED)
                .build();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId, "bob@company.com"))
                .isInstanceOf(BookingException.class)
                .hasMessageContaining("not authorized");
    }

    @Test
    void cancelBooking_adminCancelsOthersBooking_success() {
        Booking booking = Booking.builder()
                .id(bookingId)
                .room(activeRoom)
                .bookedBy("alice@company.com")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .status(BookingStatus.CONFIRMED)
                .build();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin@company.com", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
        );

        bookingService.cancelBooking(bookingId, "admin@company.com");

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void cancelBooking_alreadyCancelled_throwsException() {
        Booking booking = Booking.builder()
                .id(bookingId)
                .room(activeRoom)
                .bookedBy("alice@company.com")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .status(BookingStatus.CANCELLED)
                .build();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId, "alice@company.com"))
                .isInstanceOf(BookingException.class)
                .hasMessageContaining("already cancelled");
    }

    @Test
    void cancelBooking_notFound_throwsException() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId, "alice@company.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getBookingById_success() {
        Booking booking = Booking.builder()
                .id(bookingId)
                .room(activeRoom)
                .bookedBy("alice@company.com")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .status(BookingStatus.CONFIRMED)
                .build();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingResponse response = bookingService.getBookingById(bookingId);

        assertThat(response.getId()).isEqualTo(bookingId);
    }

    @Test
    void getBookingById_notFound_throwsException() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById(bookingId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
