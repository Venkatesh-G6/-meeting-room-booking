package com.yourcompany.roombooking.graph;

/*
 * Microsoft Graph API Service
 * Handles all Microsoft 365 integration.
 *
 * Phase 8A (now): Stub implementation
 * for development and testing.
 *
 * Phase 8B (after Azure access):
 * Real implementation using
 * Microsoft Graph SDK.
 * Requires:
 * - Azure App Registration
 * - Graph API permissions:
 *   Calendars.ReadWrite
 *   User.Read.All
 *   Mail.Send
 */
public interface GraphService {

    // Creates calendar event when room is booked
    String createCalendarEvent(
            com.yourcompany.roombooking.dto.response.BookingResponse booking);

    // Cancels calendar event when booking is cancelled
    void cancelCalendarEvent(
            String eventId);

    // Gets user profile from Entra ID
    GraphUserProfile getUserProfile(
            String email);

    // Sends meeting invite to attendees
    void sendMeetingInvite(
            com.yourcompany.roombooking.dto.response.BookingResponse booking,
            java.util.List<String> attendeeEmails);
}
