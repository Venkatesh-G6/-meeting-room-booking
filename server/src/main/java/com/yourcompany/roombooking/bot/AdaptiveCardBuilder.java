package com.yourcompany.roombooking.bot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yourcompany.roombooking.dto.response.BookingResponse;
import com.yourcompany.roombooking.dto.response.RoomResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdaptiveCardBuilder {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public static String buildAvailabilityCard(List<RoomResponse> rooms, String date, String startTime, String endTime) {
        ObjectNode card = mapper.createObjectNode();
        card.put("type", "AdaptiveCard");
        card.put("version", "1.4");

        ArrayNode body = card.putArray("body");

        ObjectNode title = body.addObject();
        title.put("type", "TextBlock");
        title.put("text", "Available Rooms");
        title.put("size", "Large");
        title.put("weight", "Bolder");

        ObjectNode subtitle = body.addObject();
        subtitle.put("type", "TextBlock");
        subtitle.put("text", date + " | " + startTime + " - " + endTime);
        subtitle.put("color", "Accent");

        if (rooms != null) {
            for (RoomResponse room : rooms) {
                ObjectNode columnSet = body.addObject();
                columnSet.put("type", "ColumnSet");

                ArrayNode columns = columnSet.putArray("columns");

                ObjectNode infoCol = columns.addObject();
                infoCol.put("type", "Column");
                infoCol.put("width", "stretch");
                ArrayNode infoItems = infoCol.putArray("items");
                ObjectNode roomNameBlock = infoItems.addObject();
                roomNameBlock.put("type", "TextBlock");
                roomNameBlock.put("text", room.getRoomName());
                roomNameBlock.put("weight", "Bolder");
                ObjectNode capacityBlock = infoItems.addObject();
                capacityBlock.put("type", "TextBlock");
                capacityBlock.put("text", "Capacity: " + (room.getCapacity() != null ? room.getCapacity() : "N/A"));
                capacityBlock.put("isSubtle", true);
                if (room.getLocation() != null) {
                    ObjectNode locBlock = infoItems.addObject();
                    locBlock.put("type", "TextBlock");
                    locBlock.put("text", "Location: " + room.getLocation());
                    locBlock.put("isSubtle", true);
                }

                ObjectNode buttonCol = columns.addObject();
                buttonCol.put("type", "Column");
                buttonCol.put("width", "auto");
                ArrayNode buttonItems = buttonCol.putArray("items");
                ObjectNode bookButton = buttonItems.addObject();
                bookButton.put("type", "ActionSet");
                ArrayNode actions = bookButton.putArray("actions");
                ObjectNode action = actions.addObject();
                action.put("type", "Action.Submit");
                action.put("title", "Book");
                action.put("data", room.getRoomName());
            }
        }

        if (rooms == null || rooms.isEmpty()) {
            ObjectNode empty = body.addObject();
            empty.put("type", "TextBlock");
            empty.put("text", "No rooms available for the selected time.");
            empty.put("color", "Warning");
        }

        card.set("actions", mapper.createArrayNode());

        return toJson(card);
    }

    public static String buildBookingConfirmCard(BookingResponse booking) {
        ObjectNode card = mapper.createObjectNode();
        card.put("type", "AdaptiveCard");
        card.put("version", "1.4");

        ArrayNode body = card.putArray("body");

        ObjectNode title = body.addObject();
        title.put("type", "TextBlock");
        title.put("text", "\u2705 Booking Confirmed");
        title.put("size", "Large");
        title.put("weight", "Bolder");

        addFactBody(body, "Room", booking.getRoomName());
        addFactBody(body, "Date", booking.getStartTime() != null ? booking.getStartTime().format(DATE_FMT) : "N/A");
        addFactBody(body, "Start", booking.getStartTime() != null ? booking.getStartTime().format(TIME_FMT) : "N/A");
        addFactBody(body, "End", booking.getEndTime() != null ? booking.getEndTime().format(TIME_FMT) : "N/A");
        addFactBody(body, "Booking ID", booking.getId() != null ? booking.getId().toString() : "N/A");

        ArrayNode actions = card.putArray("actions");
        ObjectNode cancelAction = actions.addObject();
        cancelAction.put("type", "Action.Submit");
        cancelAction.put("title", "Cancel Booking");
        ObjectNode cancelData = cancelAction.putObject("data");
        cancelData.put("action", "cancel");
        cancelData.put("bookingId", booking.getId() != null ? booking.getId().toString() : "");

        return toJson(card);
    }

    public static String buildMyBookingsCard(List<BookingResponse> bookings) {
        ObjectNode card = mapper.createObjectNode();
        card.put("type", "AdaptiveCard");
        card.put("version", "1.4");

        ArrayNode body = card.putArray("body");

        ObjectNode title = body.addObject();
        title.put("type", "TextBlock");
        title.put("text", "Your Upcoming Bookings");
        title.put("size", "Large");
        title.put("weight", "Bolder");

        if (bookings != null) {
            for (BookingResponse booking : bookings) {
                ObjectNode columnSet = body.addObject();
                columnSet.put("type", "ColumnSet");

                ArrayNode columns = columnSet.putArray("columns");

                ObjectNode infoCol = columns.addObject();
                infoCol.put("type", "Column");
                infoCol.put("width", "stretch");
                ArrayNode infoItems = infoCol.putArray("items");

                ObjectNode roomBlock = infoItems.addObject();
                roomBlock.put("type", "TextBlock");
                roomBlock.put("text", booking.getRoomName() != null ? booking.getRoomName() : "Unknown Room");
                roomBlock.put("weight", "Bolder");

                ObjectNode dateBlock = infoItems.addObject();
                dateBlock.put("type", "TextBlock");
                dateBlock.put("text", booking.getStartTime() != null ? booking.getStartTime().format(DATE_FMT) : "N/A");
                dateBlock.put("isSubtle", true);

                ObjectNode timeBlock = infoItems.addObject();
                timeBlock.put("type", "TextBlock");
                timeBlock.put("text", (booking.getStartTime() != null ? booking.getStartTime().format(TIME_FMT) : "?") +
                        " - " + (booking.getEndTime() != null ? booking.getEndTime().format(TIME_FMT) : "?"));
                timeBlock.put("isSubtle", true);

                ObjectNode buttonCol = columns.addObject();
                buttonCol.put("type", "Column");
                buttonCol.put("width", "auto");
                ArrayNode buttonItems = buttonCol.putArray("items");
                ObjectNode actionSet = buttonItems.addObject();
                actionSet.put("type", "ActionSet");
                ArrayNode actions = actionSet.putArray("actions");
                ObjectNode cancelAction = actions.addObject();
                cancelAction.put("type", "Action.Submit");
                cancelAction.put("title", "Cancel");
                ObjectNode cancelData = cancelAction.putObject("data");
                cancelData.put("action", "cancel");
                cancelData.put("bookingId", booking.getId() != null ? booking.getId().toString() : "");
            }
        }

        if (bookings == null || bookings.isEmpty()) {
            ObjectNode empty = body.addObject();
            empty.put("type", "TextBlock");
            empty.put("text", "You have no upcoming bookings.");
            empty.put("color", "Warning");
        }

        card.set("actions", mapper.createArrayNode());

        return toJson(card);
    }

    public static String buildErrorCard(String message) {
        ObjectNode card = mapper.createObjectNode();
        card.put("type", "AdaptiveCard");
        card.put("version", "1.4");

        ArrayNode body = card.putArray("body");

        ObjectNode errorBlock = body.addObject();
        errorBlock.put("type", "TextBlock");
        errorBlock.put("text", "\u274C " + (message != null ? message : "An error occurred"));
        errorBlock.put("color", "Attention");
        errorBlock.put("size", "Medium");
        errorBlock.put("wrap", true);

        card.set("actions", mapper.createArrayNode());

        return toJson(card);
    }

    public static String buildHelpCard() {
        ObjectNode card = mapper.createObjectNode();
        card.put("type", "AdaptiveCard");
        card.put("version", "1.4");

        ArrayNode body = card.putArray("body");

        ObjectNode title = body.addObject();
        title.put("type", "TextBlock");
        title.put("text", "Room Booking Bot - Commands");
        title.put("size", "Large");
        title.put("weight", "Bolder");

        ObjectNode intro = body.addObject();
        intro.put("type", "TextBlock");
        intro.put("text", "Here's what I can help you with:");
        intro.put("wrap", true);
        intro.put("spacing", "Small");

        String[] commands = {
                "\u2022 **check availability** [date] [time] - Find free rooms\n  e.g. \"check availability tomorrow from 10am to 11am\"",
                "\u2022 **book** [room name] [date] [time] - Book a room\n  e.g. \"book conference room A tomorrow from 10am to 11am\"",
                "\u2022 **my bookings** - View your upcoming bookings",
                "\u2022 **cancel** [booking id] - Cancel a booking\n  e.g. \"cancel 3\"",
                "\u2022 **list rooms** - Show all available rooms",
                "\u2022 **reset** - Cancel current operation and start over",
                "\u2022 **help** - Show this help message"
        };

        for (String cmd : commands) {
            ObjectNode cmdBlock = body.addObject();
            cmdBlock.put("type", "TextBlock");
            cmdBlock.put("text", cmd);
            cmdBlock.put("wrap", true);
            cmdBlock.put("spacing", "Small");
        }

        ObjectNode tipBlock = body.addObject();
        tipBlock.put("type", "TextBlock");
        tipBlock.put("text", "\uD83D\uDCA1 **Date formats:** today, tomorrow, Monday, next Friday, DD/MM/YYYY\n**Time formats:** 10:00 AM, 2:30 PM, 1400, 0930");
        tipBlock.put("wrap", true);
        tipBlock.put("spacing", "Medium");
        tipBlock.put("isSubtle", true);

        card.set("actions", mapper.createArrayNode());

        return toJson(card);
    }

    public static String buildRoomListCard(List<RoomResponse> rooms) {
        ObjectNode card = mapper.createObjectNode();
        card.put("type", "AdaptiveCard");
        card.put("version", "1.4");

        ArrayNode body = card.putArray("body");

        ObjectNode title = body.addObject();
        title.put("type", "TextBlock");
        title.put("text", "All Rooms");
        title.put("size", "Large");
        title.put("weight", "Bolder");

        if (rooms != null) {
            for (RoomResponse room : rooms) {
                ObjectNode columnSet = body.addObject();
                columnSet.put("type", "ColumnSet");

                ArrayNode columns = columnSet.putArray("columns");

                ObjectNode infoCol = columns.addObject();
                infoCol.put("type", "Column");
                infoCol.put("width", "stretch");
                ArrayNode infoItems = infoCol.putArray("items");

                ObjectNode roomNameBlock = infoItems.addObject();
                roomNameBlock.put("type", "TextBlock");
                roomNameBlock.put("text", room.getRoomName());
                roomNameBlock.put("weight", "Bolder");

                ObjectNode capacityBlock = infoItems.addObject();
                capacityBlock.put("type", "TextBlock");
                capacityBlock.put("text", "Capacity: " + (room.getCapacity() != null ? room.getCapacity() : "N/A"));
                capacityBlock.put("isSubtle", true);

                if (room.getLocation() != null) {
                    ObjectNode locBlock = infoItems.addObject();
                    locBlock.put("type", "TextBlock");
                    locBlock.put("text", "Location: " + room.getLocation());
                    locBlock.put("isSubtle", true);
                }
            }
        }

        if (rooms == null || rooms.isEmpty()) {
            ObjectNode empty = body.addObject();
            empty.put("type", "TextBlock");
            empty.put("text", "No rooms found.");
            empty.put("color", "Warning");
        }

        card.set("actions", mapper.createArrayNode());

        return toJson(card);
    }

    private static void addFactBody(ArrayNode body, String label, String value) {
        ObjectNode factBlock = body.addObject();
        factBlock.put("type", "TextBlock");
        factBlock.put("text", "**" + label + ":** " + (value != null ? value : "N/A"));
        factBlock.put("wrap", true);
        factBlock.put("spacing", "Small");
    }

    private static String toJson(ObjectNode card) {
        try {
            return mapper.writeValueAsString(card);
        } catch (Exception e) {
            return "{\"type\":\"AdaptiveCard\",\"version\":\"1.4\",\"body\":[{\"type\":\"TextBlock\",\"text\":\"Error generating card\",\"color\":\"Attention\"}]}";
        }
    }
}
