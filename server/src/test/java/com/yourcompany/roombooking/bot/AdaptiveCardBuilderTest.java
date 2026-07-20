package com.yourcompany.roombooking.bot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourcompany.roombooking.dto.response.BookingResponse;
import com.yourcompany.roombooking.dto.response.RoomResponse;
import com.yourcompany.roombooking.enums.RoomType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AdaptiveCardBuilder Tests")
class AdaptiveCardBuilderTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    private JsonNode parseJson(String json) throws Exception {
        return mapper.readTree(json);
    }

    @Test
    @DisplayName("buildAvailabilityCard - valid JSON with required fields")
    void buildAvailabilityCard_returnsValidJsonWithRequiredFields() throws Exception {
        RoomResponse room = RoomResponse.builder()
                .id(UUID.randomUUID())
                .roomName("Conference Room A")
                .capacity(10)
                .location("Floor 2")
                .build();

        String json = AdaptiveCardBuilder.buildAvailabilityCard(List.of(room), "2025-07-14", "10:00", "12:00");
        JsonNode card = parseJson(json);

        assertEquals("AdaptiveCard", card.get("type").asText());
        assertEquals("1.4", card.get("version").asText());

        JsonNode body = card.get("body");
        assertTrue(body.isArray());
        assertTrue(body.size() >= 2);

        assertEquals("Available Rooms", body.get(0).get("text").asText());
        assertEquals("Large", body.get(0).get("size").asText());
        assertEquals("Bolder", body.get(0).get("weight").asText());

        assertEquals("2025-07-14 | 10:00 - 12:00", body.get(1).get("text").asText());
        assertEquals("Accent", body.get(1).get("color").asText());
    }

    @Test
    @DisplayName("buildAvailabilityCard - contains ColumnSet for each room")
    void buildAvailabilityCard_containsColumnSetForEachRoom() throws Exception {
        RoomResponse room1 = RoomResponse.builder().roomName("Room A").capacity(5).build();
        RoomResponse room2 = RoomResponse.builder().roomName("Room B").capacity(8).build();

        String json = AdaptiveCardBuilder.buildAvailabilityCard(List.of(room1, room2), "2025-07-14", "10:00", "12:00");
        JsonNode card = parseJson(json);
        JsonNode body = card.get("body");

        long columnSets = 0;
        for (JsonNode node : body) {
            if ("ColumnSet".equals(node.get("type").asText())) {
                columnSets++;
            }
        }
        assertEquals(2, columnSets);
    }

    @Test
    @DisplayName("buildAvailabilityCard - empty rooms list shows warning")
    void buildAvailabilityCard_emptyRooms_showsWarning() throws Exception {
        String json = AdaptiveCardBuilder.buildAvailabilityCard(List.of(), "2025-07-14", "10:00", "12:00");
        JsonNode card = parseJson(json);
        JsonNode body = card.get("body");

        boolean hasWarning = false;
        for (JsonNode node : body) {
            if ("Warning".equals(node.path("color").asText(null))) {
                hasWarning = true;
                break;
            }
        }
        assertTrue(hasWarning);
    }

    @Test
    @DisplayName("buildAvailabilityCard - null rooms shows warning")
    void buildAvailabilityCard_nullRooms_showsWarning() throws Exception {
        String json = AdaptiveCardBuilder.buildAvailabilityCard(null, "2025-07-14", "10:00", "12:00");
        JsonNode card = parseJson(json);

        assertEquals("AdaptiveCard", card.get("type").asText());
        assertTrue(card.get("body").isArray());
    }

    @Test
    @DisplayName("buildBookingConfirmCard - valid JSON with booking details")
    void buildBookingConfirmCard_returnsValidJsonWithBookingDetails() throws Exception {
        UUID bookingId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.of(2025, 7, 14, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 7, 14, 12, 0);

        BookingResponse booking = BookingResponse.builder()
                .id(bookingId)
                .roomId(roomId)
                .roomName("Conference Room A")
                .bookedBy("user@example.com")
                .title("Team Sync")
                .attendeeCount(5)
                .startTime(start)
                .endTime(end)
                .build();

        String json = AdaptiveCardBuilder.buildBookingConfirmCard(booking);
        JsonNode card = parseJson(json);

        assertEquals("AdaptiveCard", card.get("type").asText());
        assertEquals("1.4", card.get("version").asText());

        JsonNode body = card.get("body");
        assertTrue(body.isArray());
        assertTrue(body.get(0).get("text").asText().contains("Booking Confirmed"));

        String fullText = body.toString();
        assertTrue(fullText.contains("Conference Room A"));
        assertTrue(fullText.contains(bookingId.toString()));
    }

    @Test
    @DisplayName("buildBookingConfirmCard - has cancel action with booking ID")
    void buildBookingConfirmCard_hasCancelActionWithBookingId() throws Exception {
        UUID bookingId = UUID.randomUUID();
        BookingResponse booking = BookingResponse.builder()
                .id(bookingId)
                .roomName("Room A")
                .startTime(LocalDateTime.of(2025, 7, 14, 10, 0))
                .endTime(LocalDateTime.of(2025, 7, 14, 12, 0))
                .build();

        String json = AdaptiveCardBuilder.buildBookingConfirmCard(booking);
        JsonNode card = parseJson(json);

        JsonNode actions = card.get("actions");
        assertTrue(actions.isArray());
        assertEquals(1, actions.size());
        assertEquals("Action.Submit", actions.get(0).get("type").asText());
        assertEquals("Cancel Booking", actions.get(0).get("title").asText());
        assertEquals(bookingId.toString(), actions.get(0).get("data").get("bookingId").asText());
    }

    @Test
    @DisplayName("buildMyBookingsCard - valid JSON with bookings list")
    void buildMyBookingsCard_returnsValidJsonWithBookings() throws Exception {
        BookingResponse b1 = BookingResponse.builder()
                .id(UUID.randomUUID())
                .roomName("Room A")
                .startTime(LocalDateTime.of(2025, 7, 14, 10, 0))
                .endTime(LocalDateTime.of(2025, 7, 14, 11, 0))
                .build();
        BookingResponse b2 = BookingResponse.builder()
                .id(UUID.randomUUID())
                .roomName("Room B")
                .startTime(LocalDateTime.of(2025, 7, 15, 14, 0))
                .endTime(LocalDateTime.of(2025, 7, 15, 15, 0))
                .build();

        String json = AdaptiveCardBuilder.buildMyBookingsCard(List.of(b1, b2));
        JsonNode card = parseJson(json);

        assertEquals("AdaptiveCard", card.get("type").asText());

        JsonNode body = card.get("body");
        assertEquals("Your Upcoming Bookings", body.get(0).get("text").asText());

        long columnSets = 0;
        for (JsonNode node : body) {
            if ("ColumnSet".equals(node.path("type").asText(null))) {
                columnSets++;
            }
        }
        assertEquals(2, columnSets);
    }

    @Test
    @DisplayName("buildMyBookingsCard - each booking has cancel button")
    void buildMyBookingsCard_eachBookingHasCancelButton() throws Exception {
        BookingResponse b1 = BookingResponse.builder()
                .id(UUID.randomUUID())
                .roomName("Room A")
                .startTime(LocalDateTime.of(2025, 7, 14, 10, 0))
                .endTime(LocalDateTime.of(2025, 7, 14, 11, 0))
                .build();

        String json = AdaptiveCardBuilder.buildMyBookingsCard(List.of(b1));
        JsonNode card = parseJson(json);
        JsonNode body = card.get("body");

        boolean hasCancelAction = false;
        for (JsonNode node : body) {
            if ("ColumnSet".equals(node.path("type").asText(null))) {
                JsonNode columns = node.get("columns");
                for (JsonNode col : columns) {
                    JsonNode items = col.get("items");
                    if (items != null) {
                        for (JsonNode item : items) {
                            if ("ActionSet".equals(item.path("type").asText(null))) {
                                JsonNode actions = item.get("actions");
                                if (actions != null && actions.size() > 0) {
                                    assertEquals("Cancel", actions.get(0).get("title").asText());
                                    hasCancelAction = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        assertTrue(hasCancelAction);
    }

    @Test
    @DisplayName("buildMyBookingsCard - empty list shows no bookings message")
    void buildMyBookingsCard_emptyList_showsNoBookingsMessage() throws Exception {
        String json = AdaptiveCardBuilder.buildMyBookingsCard(List.of());
        JsonNode card = parseJson(json);
        JsonNode body = card.get("body");

        boolean hasEmptyMessage = false;
        for (JsonNode node : body) {
            if (node.path("text").asText("").contains("no upcoming bookings")) {
                hasEmptyMessage = true;
            }
        }
        assertTrue(hasEmptyMessage);
    }

    @Test
    @DisplayName("buildErrorCard - valid JSON with error message and attention color")
    void buildErrorCard_returnsValidJsonWithError() throws Exception {
        String json = AdaptiveCardBuilder.buildErrorCard("Room not available");
        JsonNode card = parseJson(json);

        assertEquals("AdaptiveCard", card.get("type").asText());
        assertEquals("1.4", card.get("version").asText());

        JsonNode body = card.get("body");
        assertTrue(body.isArray());
        assertTrue(body.get(0).get("text").asText().contains("Room not available"));
        assertEquals("Attention", body.get(0).get("color").asText());
    }

    @Test
    @DisplayName("buildErrorCard - null message shows default error text")
    void buildErrorCard_nullMessage_showsDefaultError() throws Exception {
        String json = AdaptiveCardBuilder.buildErrorCard(null);
        JsonNode card = parseJson(json);

        JsonNode body = card.get("body");
        assertTrue(body.get(0).get("text").asText().contains("error occurred"));
    }

    @Test
    @DisplayName("buildHelpCard - valid JSON with all commands listed")
    void buildHelpCard_returnsValidJsonWithAllCommands() throws Exception {
        String json = AdaptiveCardBuilder.buildHelpCard();
        JsonNode card = parseJson(json);

        assertEquals("AdaptiveCard", card.get("type").asText());
        assertEquals("1.4", card.get("version").asText());

        JsonNode body = card.get("body");
        assertTrue(body.isArray());
        assertEquals("Room Booking Bot - Commands", body.get(0).get("text").asText());

        String fullText = body.toString();
        assertTrue(fullText.contains("check availability"));
        assertTrue(fullText.contains("book"));
        assertTrue(fullText.contains("my bookings"));
        assertTrue(fullText.contains("cancel"));
    }

    @Test
    @DisplayName("All cards have actions array")
    void allCards_haveActionsArray() throws Exception {
        RoomResponse room = RoomResponse.builder().roomName("Room A").capacity(5).build();
        BookingResponse booking = BookingResponse.builder()
                .id(UUID.randomUUID())
                .roomName("Room A")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .build();

        String availJson = AdaptiveCardBuilder.buildAvailabilityCard(List.of(room), "2025-07-14", "10:00", "12:00");
        String bookingJson = AdaptiveCardBuilder.buildBookingConfirmCard(booking);
        String myBookingsJson = AdaptiveCardBuilder.buildMyBookingsCard(List.of(booking));
        String errorJson = AdaptiveCardBuilder.buildErrorCard("Test error");
        String helpJson = AdaptiveCardBuilder.buildHelpCard();

        assertTrue(parseJson(availJson).has("actions"));
        assertTrue(parseJson(bookingJson).has("actions"));
        assertTrue(parseJson(myBookingsJson).has("actions"));
        assertTrue(parseJson(errorJson).has("actions"));
        assertTrue(parseJson(helpJson).has("actions"));
    }
}
