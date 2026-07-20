package com.yourcompany.roombooking.graph;

import com.yourcompany.roombooking.dto.response.BookingResponse;
import com.yourcompany.roombooking.enums.BookingStatus;
import com.yourcompany.roombooking.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
@ActiveProfiles("dev")
class GraphServiceStubTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private GraphServiceStub graphService;

    private BookingResponse buildBookingResponse() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        return BookingResponse.builder()
                .id(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .roomId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .roomName("Meeting Room A")
                .bookedBy("user@test.com")
                .title("Team Standup")
                .attendeeCount(5)
                .startTime(tomorrow.atTime(10, 0))
                .endTime(tomorrow.atTime(11, 0))
                .status(BookingStatus.CONFIRMED)
                .build();
    }

    @Test
    void createCalendarEvent_ReturnsEventId() {
        String result = graphService.createCalendarEvent(buildBookingResponse());

        assertNotNull(result);
        assertTrue(result.startsWith("stub-event-"));
    }

    @Test
    void createCalendarEvent_LogsCorrectInfo() {
        BookingResponse booking = buildBookingResponse();

        String result = assertDoesNotThrow(() -> graphService.createCalendarEvent(booking));

        assertTrue(result.contains(booking.getId().toString()));
    }

    @Test
    void cancelCalendarEvent_StubEvent_Succeeds() {
        assertDoesNotThrow(() -> graphService.cancelCalendarEvent("stub-event-123"));
    }

    @Test
    void getUserProfile_ReturnsProfile() {
        GraphUserProfile profile = graphService.getUserProfile("test@company.com");

        assertNotNull(profile);
        assertEquals("test@company.com", profile.email());
        assertEquals("test", profile.displayName());
    }

    @Test
    void sendMeetingInvite_NoException() {
        assertDoesNotThrow(() -> graphService.sendMeetingInvite(
                buildBookingResponse(),
                List.of("user1@test.com", "user2@test.com")
        ));
    }
}
