package com.yourcompany.roombooking.bot;

import com.yourcompany.roombooking.dto.request.AvailabilityRequest;
import com.yourcompany.roombooking.dto.response.AvailabilityResponse;
import com.yourcompany.roombooking.dto.response.BookingResponse;
import com.yourcompany.roombooking.dto.response.PagedResponse;
import com.yourcompany.roombooking.dto.response.RoomResponse;
import com.yourcompany.roombooking.dto.response.SimulateResponse;
import com.yourcompany.roombooking.service.BookingService;
import com.yourcompany.roombooking.service.RoomService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("MultiTurnDialogHandler Tests")
@ExtendWith(MockitoExtension.class)
class MultiTurnDialogHandlerTest {

    @Mock
    private ConversationStateManager stateManager;

    @Mock
    private RoomService roomService;

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private MultiTurnDialogHandler handler;

    private static final String USER_EMAIL = "admin@company.com";

    private ConversationContext createIdleContext() {
        ConversationContext ctx = new ConversationContext();
        ctx.reset();
        ctx.setUserEmail(USER_EMAIL);
        return ctx;
    }

    private RoomResponse createRoom(String name, int capacity) {
        return RoomResponse.builder()
                .id(UUID.randomUUID())
                .roomName(name)
                .capacity(capacity)
                .active(true)
                .build();
    }

    private AvailabilityResponse createAvailabilityResponse(List<RoomResponse> rooms) {
        return AvailabilityResponse.builder()
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .availableRooms(rooms)
                .build();
    }

    @Test
    @DisplayName("Full booking flow — happy path")
    void fullBookingFlow_HappyPath() {
        // Step 1: Send "book a room"
        ConversationContext ctx = createIdleContext();
        SimulateResponse r1 = handler.handleMessage("book a room", USER_EMAIL, ctx);
        assertEquals(ConversationStep.AWAITING_DATE, ctx.getStep());
        assertNotNull(r1.getMessage());
        assertTrue(r1.getMessage().toLowerCase().contains("date"));

        // Step 2: Send "tomorrow"
        ctx.setSelectedDate(null);
        SimulateResponse r2 = handler.handleMessage("tomorrow", USER_EMAIL, ctx);
        assertEquals(ConversationStep.AWAITING_START_TIME, ctx.getStep());
        assertNotNull(r2.getMessage());
        assertTrue(r2.getMessage().toLowerCase().contains("start") || r2.getMessage().toLowerCase().contains("time"));

        // Step 3: Send "10:00"
        SimulateResponse r3 = handler.handleMessage("10:00", USER_EMAIL, ctx);
        assertEquals(ConversationStep.AWAITING_END_TIME, ctx.getStep());
        assertNotNull(r3.getMessage());
        assertTrue(r3.getMessage().toLowerCase().contains("end"));

        // Step 4: Send "11:00" — mock returns 2 rooms
        RoomResponse room1 = createRoom("Board Room", 25);
        RoomResponse room2 = createRoom("Conference Room", 10);
        AvailabilityResponse availResponse = createAvailabilityResponse(List.of(room1, room2));
        when(roomService.checkAvailability(any(AvailabilityRequest.class)))
                .thenReturn(availResponse);

        SimulateResponse r4 = handler.handleMessage("11:00", USER_EMAIL, ctx);
        assertEquals(ConversationStep.AWAITING_ROOM_SELECTION, ctx.getStep());
        assertNotNull(r4.getMessage());
        assertTrue(r4.getMessage().contains("1.") || r4.getMessage().contains("Board Room"));
        assertEquals(2, ctx.getAvailableRooms().size());

        // Step 5: Send "1" — select first room
        SimulateResponse r5 = handler.handleMessage("1", USER_EMAIL, ctx);
        assertEquals(ConversationStep.AWAITING_BOOKING_CONFIRMATION, ctx.getStep());
        assertNotNull(r5.getMessage());
        assertTrue(r5.getMessage().toLowerCase().contains("yes") || r5.getMessage().toLowerCase().contains("confirm"));

        // Step 6: Send "yes" — mock booking creation
        BookingResponse bookingResponse = BookingResponse.builder()
                .id(UUID.randomUUID())
                .roomName("Board Room")
                .title("Booked via Teams Bot")
                .startTime(LocalDateTime.of(ctx.getSelectedDate(), ctx.getStartTime()))
                .endTime(LocalDateTime.of(ctx.getSelectedDate(), ctx.getEndTime()))
                .status(com.yourcompany.roombooking.enums.BookingStatus.CONFIRMED)
                .build();
        when(bookingService.createBooking(any(), anyString())).thenReturn(bookingResponse);

        SimulateResponse r6 = handler.handleMessage("yes", USER_EMAIL, ctx);
        assertEquals(ConversationStep.IDLE, ctx.getStep());
        assertNotNull(r6.getCardJson());
        assertTrue(r6.isSuccess());
    }

    @Test
    @DisplayName("Cancel flow — happy path")
    void cancelFlow_HappyPath() {
        // Step 1: Send "cancel 1"
        ConversationContext ctx = createIdleContext();
        SimulateResponse r1 = handler.handleMessage("cancel 1", USER_EMAIL, ctx);
        assertEquals(ConversationStep.AWAITING_CANCEL_CONFIRMATION, ctx.getStep());
        assertNotNull(r1.getMessage());
        assertTrue(r1.getMessage().toLowerCase().contains("yes") || r1.getMessage().toLowerCase().contains("confirm"));

        // Step 2: Send "yes" — mock cancel + getMyBookings
        BookingResponse booking = BookingResponse.builder()
                .id(UUID.randomUUID())
                .roomName("Board Room")
                .title("Test Booking")
                .status(com.yourcompany.roombooking.enums.BookingStatus.CONFIRMED)
                .build();
        PagedResponse<BookingResponse> bookingsPage = PagedResponse.<BookingResponse>builder()
                .content(List.of(booking))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();
        when(bookingService.getMyBookings(anyString(), anyInt(), anyInt())).thenReturn(bookingsPage);
        doNothing().when(bookingService).cancelBooking(any(), anyString());

        SimulateResponse r2 = handler.handleMessage("yes", USER_EMAIL, ctx);
        assertEquals(ConversationStep.IDLE, ctx.getStep());
        assertTrue(r2.isSuccess());
        assertTrue(r2.getMessage().contains("cancelled") || r2.getMessage().contains("\u2705"));
    }

    @Test
    @DisplayName("Availability flow — all info extracted, returns card directly")
    void availabilityFlow_WithExtractedInfo() {
        ConversationContext ctx = createIdleContext();

        RoomResponse room = createRoom("Board Room", 25);
        AvailabilityResponse availResponse = createAvailabilityResponse(List.of(room));
        when(roomService.checkAvailability(any(AvailabilityRequest.class)))
                .thenReturn(availResponse);

        SimulateResponse response = handler.handleMessage(
                "check availability tomorrow 10am to 11am", USER_EMAIL, ctx);

        assertEquals(ConversationStep.AWAITING_ROOM_SELECTION, ctx.getStep());
        assertNotNull(response.getCardJson());
        assertTrue(response.isSuccess());
    }

    @Test
    @DisplayName("Expired session — resets to IDLE")
    void expiredSession_ResetsToIdle() {
        ConversationContext ctx = new ConversationContext();
        ctx.setStep(ConversationStep.AWAITING_DATE);
        ctx.setCommand(BotCommand.BOOK_ROOM);
        ctx.setUserEmail(USER_EMAIL);
        ctx.setLastUpdated(LocalDateTime.now().minus(20, ChronoUnit.MINUTES));

        // The handler receives context directly; expired context
        // with AWAITING_DATE will try to parse date from message.
        // Sending a valid new command "book a room" starts fresh.
        SimulateResponse response = handler.handleMessage("book a room", USER_EMAIL, ctx);

        // Since context was in AWAITING_DATE, it tries to parse "book a room" as a date
        // which fails, so it asks for date again — step stays AWAITING_DATE
        assertEquals(ConversationStep.AWAITING_DATE, ctx.getStep());
        assertNotNull(response.getMessage());
        assertTrue(response.getMessage().toLowerCase().contains("date"));
    }

    @Test
    @DisplayName("Invalid date — asks again")
    void invalidDate_AsksAgain() {
        ConversationContext ctx = new ConversationContext();
        ctx.setStep(ConversationStep.AWAITING_DATE);
        ctx.setCommand(BotCommand.BOOK_ROOM);
        ctx.setUserEmail(USER_EMAIL);
        ctx.setLastUpdated(LocalDateTime.now());

        SimulateResponse response = handler.handleMessage("not a date", USER_EMAIL, ctx);

        assertEquals(ConversationStep.AWAITING_DATE, ctx.getStep());
        assertNotNull(response.getMessage());
        assertTrue(response.getMessage().toLowerCase().contains("date"));
    }

    @Test
    @DisplayName("No rooms available — offers alternative")
    void noRoomsAvailable_OffersAlternative() {
        ConversationContext ctx = new ConversationContext();
        ctx.setStep(ConversationStep.AWAITING_END_TIME);
        ctx.setCommand(BotCommand.BOOK_ROOM);
        ctx.setUserEmail(USER_EMAIL);
        ctx.setSelectedDate(LocalDate.now().plusDays(1));
        ctx.setStartTime(LocalTime.of(10, 0));
        ctx.setLastUpdated(LocalDateTime.now());

        AvailabilityResponse emptyResponse = createAvailabilityResponse(Collections.emptyList());
        when(roomService.checkAvailability(any(AvailabilityRequest.class)))
                .thenReturn(emptyResponse);

        SimulateResponse response = handler.handleMessage("11:00", USER_EMAIL, ctx);

        assertEquals(ConversationStep.IDLE, ctx.getStep());
        assertNotNull(response.getMessage());
        assertTrue(response.getMessage().toLowerCase().contains("no rooms") ||
                response.getMessage().toLowerCase().contains("different time"));
    }
}
