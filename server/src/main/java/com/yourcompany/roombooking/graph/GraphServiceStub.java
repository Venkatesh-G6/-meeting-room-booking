package com.yourcompany.roombooking.graph;

import com.yourcompany.roombooking.dto.response.BookingResponse;
import com.yourcompany.roombooking.repository.BookingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * STUB implementation for development.
 * Logs all actions without real API calls.
 * Replaced by GraphServiceImpl in prod.
 *
 * TODO Phase 8B: Create GraphServiceImpl
 * with @Profile("local","prod")
 * using Microsoft Graph SDK.
 */
@Slf4j
@Service
@Profile("dev")
public class GraphServiceStub implements GraphService {

    private final BookingRepository bookingRepository;

    public GraphServiceStub(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public String createCalendarEvent(BookingResponse booking) {
        log.info("STUB: Creating calendar event for booking {} in room {}", booking.getId(), booking.getRoomName());
        String eventId = "stub-event-" + booking.getId() + "-" + System.currentTimeMillis();
        log.info("STUB: Created calendar event with ID: {}", eventId);
        return eventId;
    }

    @Override
    public void cancelCalendarEvent(String eventId) {
        log.info("STUB: Cancelling calendar event {}", eventId);
        if (eventId.contains("stub")) {
            log.info("STUB: Event cancelled successfully");
        } else {
            log.info("STUB: Event not found {}", eventId);
        }
    }

    @Override
    public GraphUserProfile getUserProfile(String email) {
        log.info("STUB: Getting user profile for {}", email);
        return new GraphUserProfile(
                "stub-user-" + email.hashCode(),
                email,
                email.split("@")[0],
                "Employee",
                "Engineering"
        );
    }

    @Override
    public void sendMeetingInvite(BookingResponse booking, List<String> attendeeEmails) {
        log.info("STUB: Sending meeting invite for booking {} to {}", booking.getId(), attendeeEmails);
        for (String attendeeEmail : attendeeEmails) {
            log.info("STUB: Sending invite to: {}", attendeeEmail);
        }
    }
}
