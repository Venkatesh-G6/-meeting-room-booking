package com.yourcompany.roombooking.graph;

import com.yourcompany.roombooking.config.BotProperties;
import com.yourcompany.roombooking.dto.response.BookingResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

/*
 * Real Microsoft Graph implementation.
 * Active on local and prod profiles.
 *
 * Prerequisites to activate:
 * 1. Azure App Registration complete
 * 2. Graph API permissions granted:
 *    - Calendars.ReadWrite
 *    - User.Read.All
 *    - Mail.Send
 * 3. BOT_APP_ID and BOT_APP_PASSWORD set
 *
 * Implementation steps (Phase 8B):
 * Step 1: Add GraphServiceClient init
 * Step 2: Implement createCalendarEvent
 * Step 3: Implement cancelCalendarEvent
 * Step 4: Implement sendMeetingInvite
 * Step 5: Test with real Graph API
 */
@Slf4j
@Service
@Profile({"local", "prod"})
@RequiredArgsConstructor
public class GraphServiceImpl implements GraphService {

    private final BotProperties botProperties;

    private Object graphClient;
    private String accessToken;

    @PostConstruct
    void initializeGraphClient() {
        log.info("Initializing Microsoft Graph client");
        /*
         * TODO Phase 8B: Initialize real client
         *
         * ClientSecretCredential credential =
         *   new ClientSecretCredentialBuilder()
         *   .clientId(botProperties.getAppId())
         *   .clientSecret(botProperties.getAppPassword())
         *   .tenantId(azureTenantId)
         *   .build();
         *
         * graphClient = new GraphServiceClient(
         *   credential, scopes);
         */
        log.info("Graph client initialization pending Azure credentials");
    }

    @Override
    public String createCalendarEvent(BookingResponse booking) {
        log.info("Graph: Attempting to create calendar event for booking {}", booking.getId());
        throw new UnsupportedOperationException(
                "Graph API not yet configured. Add Azure credentials to activate.");
    }

    @Override
    public void cancelCalendarEvent(String eventId) {
        log.info("Graph: Attempting to cancel calendar event {}", eventId);
        throw new UnsupportedOperationException(
                "Graph API not yet configured. Add Azure credentials to activate.");
    }

    @Override
    public GraphUserProfile getUserProfile(String email) {
        log.info("Graph: Attempting to get user profile for {}", email);
        throw new UnsupportedOperationException(
                "Graph API not yet configured. Add Azure credentials to activate.");
    }

    @Override
    public void sendMeetingInvite(BookingResponse booking, List<String> attendeeEmails) {
        log.info("Graph: Attempting to send meeting invite for booking {} to {}", booking.getId(), attendeeEmails);
        throw new UnsupportedOperationException(
                "Graph API not yet configured. Add Azure credentials to activate.");
    }
}
