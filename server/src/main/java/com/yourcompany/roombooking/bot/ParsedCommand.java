package com.yourcompany.roombooking.bot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParsedCommand {

    private BotCommand command;
    private String roomName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long bookingId;
    private String rawText;

    public BotCommand getCommand() { return command; }
    public void setCommand(BotCommand command) { this.command = command; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }

    public static ParsedCommand parse(String text) {
        ParsedCommand cmd = new ParsedCommand();
        cmd.rawText = text;

        if (text == null || text.trim().isEmpty()) {
            cmd.command = BotCommand.UNKNOWN;
            return cmd;
        }

        String normalized = text.toLowerCase().trim();
        cmd.command = BotCommand.UNKNOWN;

        if (normalized.contains("availability") || normalized.contains("available") || normalized.contains("check")) {
            cmd.command = BotCommand.CHECK_AVAILABILITY;
            cmd.date = extractDate(normalized);
            cmd.startTime = extractTime(normalized, "start", "from");
            cmd.endTime = extractTime(normalized, "end", "to", "until");
        } else if (normalized.contains("cancel")) {
            cmd.command = BotCommand.CANCEL_BOOKING;
            cmd.bookingId = extractBookingId(normalized);
        } else if (normalized.contains("my booking") || normalized.contains("my meetings") || normalized.contains("show booking")) {
            cmd.command = BotCommand.MY_BOOKINGS;
        } else if (normalized.contains("book") || normalized.contains("reserve")) {
            cmd.command = BotCommand.BOOK_ROOM;
            cmd.roomName = extractRoomName(normalized);
            cmd.date = extractDate(normalized);
            cmd.startTime = extractTime(normalized, "start", "from", "at");
            cmd.endTime = extractTime(normalized, "end", "to", "until");
        } else if (normalized.contains("help") || normalized.contains("hi") || normalized.contains("hello")) {
            cmd.command = BotCommand.HELP;
        }

        return cmd;
    }

    private static LocalDate extractDate(String text) {
        if (text.contains("today")) {
            return LocalDate.now();
        }
        if (text.contains("tomorrow")) {
            return LocalDate.now().plusDays(1);
        }

        Pattern datePattern = Pattern.compile("\\b(\\d{1,2})/(\\d{1,2})/(\\d{4})\\b");
        Matcher matcher = datePattern.matcher(text);
        if (matcher.find()) {
            int day = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            int year = Integer.parseInt(matcher.group(3));
            return LocalDate.of(year, month, day);
        }

        Pattern datePatternDash = Pattern.compile("\\b(\\d{1,2})-(\\d{1,2})-(\\d{4})\\b");
        Matcher dashMatcher = datePatternDash.matcher(text);
        if (dashMatcher.find()) {
            int day = Integer.parseInt(dashMatcher.group(1));
            int month = Integer.parseInt(dashMatcher.group(2));
            int year = Integer.parseInt(dashMatcher.group(3));
            return LocalDate.of(year, month, day);
        }

        return null;
    }

    private static LocalTime extractTime(String text, String... keywords) {
        for (String keyword : keywords) {
            Pattern keywordPattern = Pattern.compile(keyword + "\\s+(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?", Pattern.CASE_INSENSITIVE);
            Matcher matcher = keywordPattern.matcher(text);
            if (matcher.find()) {
                return parseTimeFromGroups(matcher);
            }
        }

        Pattern timePattern = Pattern.compile("\\b(\\d{1,2}):(\\d{2})\\s*(am|pm)?\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = timePattern.matcher(text);
        if (matcher.find()) {
            return parseTimeFromGroups(matcher);
        }

        Pattern hourPattern = Pattern.compile("\\b(\\d{1,2})\\s*(am|pm)\\b", Pattern.CASE_INSENSITIVE);
        Matcher hourMatcher = hourPattern.matcher(text);
        if (hourMatcher.find()) {
            int hour = Integer.parseInt(hourMatcher.group(1));
            String ampm = hourMatcher.group(2).toLowerCase();
            if (ampm.equals("pm") && hour != 12) {
                hour += 12;
            } else if (ampm.equals("am") && hour == 12) {
                hour = 0;
            }
            return LocalTime.of(hour, 0);
        }

        return null;
    }

    private static LocalTime parseTimeFromGroups(Matcher matcher) {
        int hour = Integer.parseInt(matcher.group(1));
        int minute = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
        if (matcher.group(3) != null) {
            String ampm = matcher.group(3).toLowerCase();
            if (ampm.equals("pm") && hour != 12) {
                hour += 12;
            } else if (ampm.equals("am") && hour == 12) {
                hour = 0;
            }
        }
        return LocalTime.of(hour, minute);
    }

    private static String extractRoomName(String text) {
        // Remove the "book" or "reserve" prefix
        String cleaned = text.replaceFirst("(?i)^(book|reserve)\\s+", "").trim();
        // Extract everything up to date/time keywords
        Pattern roomPattern = Pattern.compile("([a-z0-9\\s-]+?)\\s+(?:on|for|at|from|today|tomorrow|\\d{1,2}[:/\\s]|$)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = roomPattern.matcher(cleaned);
        if (matcher.find()) {
            String name = matcher.group(1).trim();
            // Remove trailing "room" suffix and add it back cleanly
            if (name.toLowerCase().endsWith("room")) {
                return name;
            }
            // If the word "room" appears in the cleaned text, capture up to and including it
            Pattern roomWordPattern = Pattern.compile("([a-z0-9\\s-]*?room(?:\\s+[a-z0-9-]+)?)", Pattern.CASE_INSENSITIVE);
            Matcher roomWordMatcher = roomWordPattern.matcher(cleaned);
            if (roomWordMatcher.find()) {
                return roomWordMatcher.group(1).trim();
            }
            return name;
        }
        return null;
    }

    private static Long extractBookingId(String text) {
        Pattern idPattern = Pattern.compile("cancel\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = idPattern.matcher(text);
        if (matcher.find()) {
            return Long.parseLong(matcher.group(1));
        }

        Pattern numberPattern = Pattern.compile("\\b(\\d+)\\b");
        Matcher numMatcher = numberPattern.matcher(text);
        if (numMatcher.find()) {
            return Long.parseLong(numMatcher.group(1));
        }

        return null;
    }
}
