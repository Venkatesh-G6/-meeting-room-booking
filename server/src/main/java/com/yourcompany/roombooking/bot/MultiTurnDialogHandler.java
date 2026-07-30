package com.yourcompany.roombooking.bot;

import com.yourcompany.roombooking.dto.request.AvailabilityRequest;
import com.yourcompany.roombooking.dto.request.CreateBookingRequest;
import com.yourcompany.roombooking.dto.response.AvailabilityResponse;
import com.yourcompany.roombooking.dto.response.BookingResponse;
import com.yourcompany.roombooking.dto.response.PagedResponse;
import com.yourcompany.roombooking.dto.response.RoomResponse;
import com.yourcompany.roombooking.dto.response.SimulateResponse;
import com.yourcompany.roombooking.service.BookingService;
import com.yourcompany.roombooking.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MultiTurnDialogHandler {

    private static final Logger log = LoggerFactory.getLogger(MultiTurnDialogHandler.class);

    private final ConversationStateManager stateManager;
    private final RoomService roomService;
    private final BookingService bookingService;

    public MultiTurnDialogHandler(ConversationStateManager stateManager,
                                  RoomService roomService,
                                  BookingService bookingService) {
        this.stateManager = stateManager;
        this.roomService = roomService;
        this.bookingService = bookingService;
    }

    public SimulateResponse handleMessage(String userMessage, String userEmail, ConversationContext context) {
        if (context == null) {
            context = stateManager.getContext(userEmail);
        }
        context.setUserEmail(userEmail);

        String normalized = userMessage.toLowerCase().trim();

        try {
            // RESET works at any conversation step
            if (normalized.equals("reset") || normalized.equals("start over") || normalized.equals("restart")
                    || normalized.equals("cancel") || normalized.equals("abort") || normalized.equals("exit")
                    || normalized.equals("quit") || normalized.equals("stop") || normalized.equals("nevermind")
                    || normalized.equals("never mind") || normalized.equals("forget it")) {
                context.reset();
                stateManager.saveContext(userEmail, context);
                return textResponse("Session reset. How can I help you?", "RESET", true);
            }

            switch (context.getStep()) {
                case IDLE:
                    return handleIdle(userMessage, normalized, userEmail, context);
                case AWAITING_DATE:
                    return handleAwaitingDate(normalized, context);
                case AWAITING_START_TIME:
                    return handleAwaitingStartTime(normalized, context);
                case AWAITING_END_TIME:
                    return handleAwaitingEndTime(normalized, context);
                case AWAITING_ROOM_SELECTION:
                    return handleAwaitingRoomSelection(normalized, context);
                case AWAITING_BOOKING_CONFIRMATION:
                    return handleAwaitingBookingConfirmation(normalized, userEmail, context);
                case AWAITING_CANCEL_CONFIRMATION:
                    return handleAwaitingCancelConfirmation(normalized, userEmail, context);
                default:
                    context.reset();
                    return textResponse("Session reset. How can I help you?", "UNKNOWN", false);
            }
        } catch (Exception e) {
            log.error("Error in multi-turn dialog: {}", e.getMessage(), e);
            context.reset();
            return SimulateResponse.builder()
                    .cardJson(AdaptiveCardBuilder.buildErrorCard("Something went wrong: " + e.getMessage()))
                    .commandType("ERROR")
                    .success(false)
                    .build();
        }
    }

    private SimulateResponse handleIdle(String userMessage, String normalized, String userEmail, ConversationContext context) {
        ParsedCommand cmd = ParsedCommand.parse(userMessage);

        switch (cmd.getCommand()) {
            case CHECK_AVAILABILITY:
                if (cmd.getDate() != null && cmd.getStartTime() != null && cmd.getEndTime() != null) {
                    if (cmd.getDate().isBefore(LocalDate.now())) {
                        return textResponse("You cannot check availability for a past date. Please try a future date.",
                                "CHECK_AVAILABILITY", false);
                    }
                    return executeAvailability(cmd.getDate(), cmd.getStartTime(), cmd.getEndTime(), context);
                } else {
                    context.setStep(ConversationStep.AWAITING_DATE);
                    context.setCommand(BotCommand.CHECK_AVAILABILITY);
                    if (cmd.getStartTime() != null) context.setStartTime(cmd.getStartTime());
                    if (cmd.getEndTime() != null) context.setEndTime(cmd.getEndTime());
                    stateManager.saveContext(userEmail, context);
                    return textResponse("What date would you like to check? (e.g. tomorrow, 25/07/2025)",
                            "CHECK_AVAILABILITY", false);
                }

            case BOOK_ROOM:
                if (cmd.getDate() != null && cmd.getStartTime() != null && cmd.getEndTime() != null && cmd.getRoomName() != null) {
                    if (cmd.getDate().isBefore(LocalDate.now())) {
                        return textResponse("You cannot book a room for a past date. Please try a future date.",
                                "BOOK_ROOM", false);
                    }
                    context.setSelectedDate(cmd.getDate());
                    context.setStartTime(cmd.getStartTime());
                    context.setEndTime(cmd.getEndTime());
                    context.setSelectedRoomName(cmd.getRoomName());
                    return showBookingConfirmation(context);
                } else if (cmd.getDate() == null) {
                    context.setStep(ConversationStep.AWAITING_DATE);
                    context.setCommand(BotCommand.BOOK_ROOM);
                    if (cmd.getStartTime() != null) context.setStartTime(cmd.getStartTime());
                    if (cmd.getEndTime() != null) context.setEndTime(cmd.getEndTime());
                    if (cmd.getRoomName() != null) context.setSelectedRoomName(cmd.getRoomName());
                    stateManager.saveContext(userEmail, context);
                    return textResponse("Let me help you book a room. What date do you need it for?",
                            "BOOK_ROOM", false);
                } else {
                    context.setStep(ConversationStep.AWAITING_DATE);
                    context.setCommand(BotCommand.BOOK_ROOM);
                    context.setSelectedDate(cmd.getDate());
                    if (cmd.getStartTime() != null) context.setStartTime(cmd.getStartTime());
                    if (cmd.getEndTime() != null) context.setEndTime(cmd.getEndTime());
                    if (cmd.getRoomName() != null) context.setSelectedRoomName(cmd.getRoomName());
                    stateManager.saveContext(userEmail, context);
                    return textResponse("What date would you like to book? (e.g. tomorrow)",
                            "BOOK_ROOM", false);
                }

            case CANCEL_BOOKING:
                if (cmd.getBookingId() != null) {
                    context.setPendingBookingId(cmd.getBookingId());
                    context.setStep(ConversationStep.AWAITING_CANCEL_CONFIRMATION);
                    stateManager.saveContext(userEmail, context);
                    return textResponse("Are you sure you want to cancel booking #" + cmd.getBookingId() +
                            "? Reply YES to confirm or NO to cancel.", "CANCEL_BOOKING", false);
                } else {
                    return textResponse("Please provide the booking ID to cancel.\nType: cancel [booking-id]\nUse 'my bookings' to see your IDs.",
                            "CANCEL_BOOKING", false);
                }

            case MY_BOOKINGS:
                return processMyBookings(userEmail);

            case HELP:
                return SimulateResponse.builder()
                        .cardJson(AdaptiveCardBuilder.buildHelpCard())
                        .commandType("HELP")
                        .success(true)
                        .build();

            case GREETING:
                return textResponse("Hello! I'm the Room Booking Bot. Type 'help' to see what I can do.",
                        "GREETING", true);

            case LIST_ROOMS:
                return processListRooms();

            case RESET:
                context.reset();
                stateManager.saveContext(userEmail, context);
                return textResponse("Session reset. How can I help you?", "RESET", true);

            case UNKNOWN:
            default:
                return textResponse("I didn't understand that. Type 'help' to see what I can do.",
                        "UNKNOWN", false);
        }
    }

    private SimulateResponse handleAwaitingDate(String normalized, ConversationContext context) {
        LocalDate date = tryParseDate(normalized);
        if (date == null) {
            return textResponse("I didn't understand that date. Please try: tomorrow, today, or DD/MM/YYYY format.",
                    context.getCommand() != null ? context.getCommand().name() : "UNKNOWN", false);
        }

        if (date.isBefore(LocalDate.now())) {
            return textResponse("You cannot use a past date. Please try a future date (e.g. tomorrow, "
                    + LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ").",
                    context.getCommand() != null ? context.getCommand().name() : "UNKNOWN", false);
        }

        context.setSelectedDate(date);

        if (context.getStartTime() != null) {
            context.setStep(ConversationStep.AWAITING_END_TIME);
            stateManager.saveContext(context.getUserEmail(), context);
            return textResponse("What time should it end?", context.getCommand().name(), false);
        } else {
            context.setStep(ConversationStep.AWAITING_START_TIME);
            stateManager.saveContext(context.getUserEmail(), context);
            return textResponse("What time should the meeting start? (e.g. 10:00 AM)",
                    context.getCommand().name(), false);
        }
    }

    private SimulateResponse handleAwaitingStartTime(String normalized, ConversationContext context) {
        LocalTime time = tryParseTime(normalized);
        if (time == null) {
            return textResponse("Please provide time in HH:MM format (e.g. 10:00 or 2:30 PM)",
                    context.getCommand().name(), false);
        }

        context.setStartTime(time);
        context.setStep(ConversationStep.AWAITING_END_TIME);
        stateManager.saveContext(context.getUserEmail(), context);
        return textResponse("What time should it end?", context.getCommand().name(), false);
    }

    private SimulateResponse handleAwaitingEndTime(String normalized, ConversationContext context) {
        LocalTime time = tryParseTime(normalized);
        if (time == null) {
            return textResponse("Please provide time in HH:MM format (e.g. 10:00 or 2:30 PM)",
                    context.getCommand().name(), false);
        }

        if (context.getStartTime() != null && !time.isAfter(context.getStartTime())) {
            return textResponse("End time must be after start time (" +
                    context.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
                    "). Please try again.", context.getCommand().name(), false);
        }

        context.setEndTime(time);

        if (context.getCommand() == BotCommand.CHECK_AVAILABILITY) {
            return executeAvailability(context.getSelectedDate(), context.getStartTime(),
                    context.getEndTime(), context);
        }

        if (context.getCommand() == BotCommand.BOOK_ROOM) {
            AvailabilityResponse response = checkAvailability(context);
            List<RoomResponse> rooms = response.getAvailableRooms() != null
                    ? response.getAvailableRooms() : Collections.emptyList();
            context.setAvailableRooms(rooms);

            if (rooms.isEmpty()) {
                context.reset();
                stateManager.saveContext(context.getUserEmail(), context);
                return textResponse("No rooms available for that time slot. Would you like to check a different time?",
                        "BOOK_ROOM", false);
            }

            context.setStep(ConversationStep.AWAITING_ROOM_SELECTION);
            stateManager.saveContext(context.getUserEmail(), context);

            StringBuilder sb = new StringBuilder("Which room would you like? Reply with the room number.\n\n");
            for (int i = 0; i < rooms.size(); i++) {
                RoomResponse room = rooms.get(i);
                sb.append(i + 1).append(". ").append(room.getRoomName());
                if (room.getCapacity() != null) {
                    sb.append(" (capacity: ").append(room.getCapacity()).append(")");
                }
                sb.append("\n");
            }
            return textResponse(sb.toString().trim(), "BOOK_ROOM", false);
        }

        context.reset();
        return textResponse("Session reset. How can I help you?", "UNKNOWN", false);
    }

    private SimulateResponse handleAwaitingRoomSelection(String normalized, ConversationContext context) {
        Integer selection = tryParseNumber(normalized);
        List<RoomResponse> rooms = context.getAvailableRooms();

        if (selection == null || selection < 1 || selection > (rooms != null ? rooms.size() : 0)) {
            int total = rooms != null ? rooms.size() : 0;
            return textResponse("Please reply with a number between 1 and " + total,
                    context.getCommand().name(), false);
        }

        RoomResponse selectedRoom = rooms.get(selection - 1);
        context.setSelectedRoomId((long) selection);
        context.setSelectedRoomName(selectedRoom.getRoomName());
        context.setStep(ConversationStep.AWAITING_BOOKING_CONFIRMATION);
        stateManager.saveContext(context.getUserEmail(), context);

        return textResponse("Book " + selectedRoom.getRoomName() + " on " +
                context.getSelectedDate().format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")) +
                " from " + context.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
                " to " + context.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
                "?\nReply YES to confirm or NO to cancel.",
                context.getCommand().name(), false);
    }

    private SimulateResponse handleAwaitingBookingConfirmation(String normalized, String userEmail, ConversationContext context) {
        if (normalized.contains("yes") || normalized.contains("confirm")) {
            List<RoomResponse> rooms = context.getAvailableRooms();
            if (rooms == null || rooms.isEmpty()) {
                context.reset();
                return textResponse("No room selected. Please start again.", "BOOK_ROOM", false);
            }

            int roomIndex = context.getSelectedRoomId() != null ? context.getSelectedRoomId().intValue() - 1 : -1;
            if (roomIndex < 0 || roomIndex >= rooms.size()) {
                context.reset();
                return textResponse("Invalid room selection. Please start again.", "BOOK_ROOM", false);
            }

            RoomResponse room = rooms.get(roomIndex);
            LocalDateTime startDateTime = LocalDateTime.of(context.getSelectedDate(), context.getStartTime());
            LocalDateTime endDateTime = LocalDateTime.of(context.getSelectedDate(), context.getEndTime());

            CreateBookingRequest bookingRequest = CreateBookingRequest.builder()
                    .roomId(room.getId())
                    .title("Booked via Teams Bot")
                    .attendeeCount(1)
                    .startTime(startDateTime)
                    .endTime(endDateTime)
                    .build();

            BookingResponse booking = bookingService.createBooking(bookingRequest, userEmail);
            context.reset();
            stateManager.saveContext(userEmail, context);

            return SimulateResponse.builder()
                    .cardJson(AdaptiveCardBuilder.buildBookingConfirmCard(booking))
                    .commandType("BOOK_ROOM")
                    .success(true)
                    .build();
        }

        if (normalized.contains("no") || normalized.contains("cancel")) {
            context.reset();
            stateManager.saveContext(userEmail, context);
            return textResponse("Booking cancelled. How else can I help you?", "BOOK_ROOM", false);
        }

        return textResponse("Please reply YES to confirm or NO to cancel.",
                context.getCommand().name(), false);
    }

    private SimulateResponse handleAwaitingCancelConfirmation(String normalized, String userEmail, ConversationContext context) {
        if (normalized.contains("yes")) {
            try {
                int index = context.getPendingBookingId() != null ? context.getPendingBookingId().intValue() : -1;
                PagedResponse<BookingResponse> bookingsPage = bookingService.getMyBookings(userEmail, 0, 10);
                List<BookingResponse> bookings = bookingsPage.getContent() != null
                        ? bookingsPage.getContent() : Collections.emptyList();

                if (index < 1 || index > bookings.size()) {
                    context.reset();
                    return textResponse("Invalid booking number: " + index + ". You have " + bookings.size() + " bookings.",
                            "CANCEL_BOOKING", false);
                }

                BookingResponse booking = bookings.get(index - 1);
                bookingService.cancelBooking(booking.getId(), userEmail);
                context.reset();
                stateManager.saveContext(userEmail, context);
                return textResponse("\u2705 Booking #" + index + " (" + booking.getTitle() + ") has been cancelled.",
                        "CANCEL_BOOKING", true);
            } catch (Exception e) {
                context.reset();
                return SimulateResponse.builder()
                        .cardJson(AdaptiveCardBuilder.buildErrorCard("Failed to cancel booking: " + e.getMessage()))
                        .commandType("CANCEL_BOOKING")
                        .success(false)
                        .build();
            }
        }

        if (normalized.contains("no")) {
            context.reset();
            stateManager.saveContext(userEmail, context);
            return textResponse("Cancellation aborted. Your booking is still active.", "CANCEL_BOOKING", false);
        }

        return textResponse("Please reply YES to confirm cancellation or NO to abort.",
                "CANCEL_BOOKING", false);
    }

    private SimulateResponse executeAvailability(LocalDate date, LocalTime startTime, LocalTime endTime,
                                                  ConversationContext context) {
        AvailabilityResponse response = checkAvailability(date, startTime, endTime);
        List<RoomResponse> rooms = response.getAvailableRooms() != null
                ? response.getAvailableRooms() : Collections.emptyList();

        context.setAvailableRooms(rooms);
        context.setSelectedDate(date);
        context.setStartTime(startTime);
        context.setEndTime(endTime);
        context.setStep(ConversationStep.AWAITING_ROOM_SELECTION);
        stateManager.saveContext(context.getUserEmail(), context);

        String card = AdaptiveCardBuilder.buildAvailabilityCard(rooms, date.toString(),
                startTime.toString(), endTime.toString());

        return SimulateResponse.builder()
                .cardJson(card)
                .commandType("CHECK_AVAILABILITY")
                .success(true)
                .build();
    }

    private SimulateResponse processListRooms() {
        PagedResponse<RoomResponse> roomsPage = roomService.getAllRooms(0, 50);
        List<RoomResponse> rooms = roomsPage.getContent() != null
                ? roomsPage.getContent() : Collections.emptyList();
        String card = AdaptiveCardBuilder.buildRoomListCard(rooms);
        return SimulateResponse.builder()
                .cardJson(card)
                .commandType("LIST_ROOMS")
                .success(true)
                .build();
    }

    private SimulateResponse showBookingConfirmation(ConversationContext context) {
        context.setStep(ConversationStep.AWAITING_BOOKING_CONFIRMATION);
        stateManager.saveContext(context.getUserEmail(), context);

        return textResponse("Book " + context.getSelectedRoomName() + " on " +
                context.getSelectedDate().format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")) +
                " from " + context.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
                " to " + context.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")) +
                "?\nReply YES to confirm or NO to cancel.",
                "BOOK_ROOM", false);
    }

    private AvailabilityResponse checkAvailability(ConversationContext context) {
        return checkAvailability(context.getSelectedDate(), context.getStartTime(), context.getEndTime());
    }

    private AvailabilityResponse checkAvailability(LocalDate date, LocalTime startTime, LocalTime endTime) {
        AvailabilityRequest request = AvailabilityRequest.builder()
                .date(date)
                .startTime(startTime)
                .endTime(endTime)
                .minCapacity(1)
                .build();
        return roomService.checkAvailability(request);
    }

    private SimulateResponse processMyBookings(String userEmail) {
        PagedResponse<BookingResponse> bookingsPage = bookingService.getMyBookings(userEmail, 0, 5);
        List<BookingResponse> bookings = bookingsPage.getContent() != null
                ? bookingsPage.getContent() : Collections.emptyList();

        String card = AdaptiveCardBuilder.buildMyBookingsCard(bookings);
        return SimulateResponse.builder()
                .cardJson(card)
                .commandType("MY_BOOKINGS")
                .success(true)
                .build();
    }

    private LocalDate tryParseDate(String text) {
        if (text.contains("today")) {
            return LocalDate.now();
        }
        if (text.contains("tomorrow") || text.contains("tmrw")) {
            return LocalDate.now().plusDays(1);
        }
        if (text.contains("day after tomorrow") || text.contains("day after")) {
            return LocalDate.now().plusDays(2);
        }
        if (text.contains("next week")) {
            return LocalDate.now().plusWeeks(1);
        }

        // Day-of-week names
        String[] dayNames = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
        String[] dayAbbrevs = {"mon", "tue", "wed", "thu", "fri", "sat", "sun"};
        java.time.DayOfWeek[] dows = {java.time.DayOfWeek.MONDAY, java.time.DayOfWeek.TUESDAY,
                java.time.DayOfWeek.WEDNESDAY, java.time.DayOfWeek.THURSDAY,
                java.time.DayOfWeek.FRIDAY, java.time.DayOfWeek.SATURDAY, java.time.DayOfWeek.SUNDAY};
        for (int i = 0; i < dayNames.length; i++) {
            if (text.contains(dayNames[i]) || text.matches(".*\\b" + dayAbbrevs[i] + "\\b.*")) {
                boolean isNext = text.contains("next");
                LocalDate today = LocalDate.now();
                LocalDate target = today.with(java.time.temporal.TemporalAdjusters.nextOrSame(dows[i]));
                if (isNext && target.equals(today)) {
                    target = today.with(java.time.temporal.TemporalAdjusters.next(dows[i]));
                }
                if (!isNext && target.isBefore(today)) {
                    target = today.with(java.time.temporal.TemporalAdjusters.next(dows[i]));
                }
                return target;
            }
        }

        Pattern datePattern = Pattern.compile("\\b(\\d{1,2})/(\\d{1,2})/(\\d{4})\\b");
        Matcher matcher = datePattern.matcher(text);
        if (matcher.find()) {
            int day = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            int year = Integer.parseInt(matcher.group(3));
            if (month < 1 || month > 12 || day < 1 || day > 31) return null;
            return LocalDate.of(year, month, day);
        }
        Pattern datePatternDash = Pattern.compile("\\b(\\d{1,2})-(\\d{1,2})-(\\d{4})\\b");
        Matcher dashMatcher = datePatternDash.matcher(text);
        if (dashMatcher.find()) {
            int day = Integer.parseInt(dashMatcher.group(1));
            int month = Integer.parseInt(dashMatcher.group(2));
            int year = Integer.parseInt(dashMatcher.group(3));
            if (month < 1 || month > 12 || day < 1 || day > 31) return null;
            return LocalDate.of(year, month, day);
        }
        Pattern datePatternDot = Pattern.compile("\\b(\\d{1,2})\\.(\\d{1,2})\\.(\\d{4})\\b");
        Matcher dotMatcher = datePatternDot.matcher(text);
        if (dotMatcher.find()) {
            int day = Integer.parseInt(dotMatcher.group(1));
            int month = Integer.parseInt(dotMatcher.group(2));
            int year = Integer.parseInt(dotMatcher.group(3));
            if (month < 1 || month > 12 || day < 1 || day > 31) return null;
            return LocalDate.of(year, month, day);
        }
        return null;
    }

    private LocalTime tryParseTime(String text) {
        // HH:MM format with optional am/pm
        Pattern timePattern = Pattern.compile("\\b(\\d{1,2}):(\\d{2})\\s*(am|pm)?\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = timePattern.matcher(text);
        if (matcher.find()) {
            int hour = Integer.parseInt(matcher.group(1));
            int minute = Integer.parseInt(matcher.group(2));
            if (matcher.group(3) != null) {
                String ampm = matcher.group(3).toLowerCase();
                if (ampm.equals("pm") && hour != 12) hour += 12;
                else if (ampm.equals("am") && hour == 12) hour = 0;
            }
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) return null;
            return LocalTime.of(hour, minute);
        }
        // "10am", "2pm" format
        Pattern hourPattern = Pattern.compile("\\b(\\d{1,2})\\s*(am|pm)\\b", Pattern.CASE_INSENSITIVE);
        Matcher hourMatcher = hourPattern.matcher(text);
        if (hourMatcher.find()) {
            int hour = Integer.parseInt(hourMatcher.group(1));
            String ampm = hourMatcher.group(2).toLowerCase();
            if (ampm.equals("pm") && hour != 12) hour += 12;
            else if (ampm.equals("am") && hour == 12) hour = 0;
            return LocalTime.of(hour, 0);
        }
        // 24-hour HHMM format (e.g. "1400", "0930") — only if no am/pm
        if (!text.contains("am") && !text.contains("pm")) {
            Pattern h24Pattern = Pattern.compile("\\b([01]?\\d|2[0-3])([0-5]\\d)\\b");
            Matcher h24Matcher = h24Pattern.matcher(text);
            if (h24Matcher.find()) {
                int hour = Integer.parseInt(h24Matcher.group(1));
                int minute = Integer.parseInt(h24Matcher.group(2));
                return LocalTime.of(hour, minute);
            }
        }
        return null;
    }

    private Integer tryParseNumber(String text) {
        Pattern numberPattern = Pattern.compile("\\b(\\d+)\\b");
        Matcher matcher = numberPattern.matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }

    private SimulateResponse textResponse(String message, String commandType, boolean success) {
        return SimulateResponse.builder()
                .message(message)
                .commandType(commandType)
                .success(success)
                .build();
    }
}
