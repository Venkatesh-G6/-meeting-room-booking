package com.yourcompany.roombooking.bot;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
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

    private static final Pattern TIME_KEYWORD_PATTERN = Pattern.compile(
            "(start|from|at|end|to|until|between)\\s+(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern TIME_GENERIC_PATTERN = Pattern.compile(
            "\\b(\\d{1,2}):(\\d{2})\\s*(am|pm)?\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern HOUR_PATTERN = Pattern.compile(
            "\\b(\\d{1,2})\\s*(am|pm)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern HOUR_MINUTE_24H_PATTERN = Pattern.compile(
            "\\b([01]?\\d|2[0-3])([0-5]\\d)\\b");

    private static final Pattern DATE_SLASH_PATTERN = Pattern.compile(
            "\\b(\\d{1,2})/(\\d{1,2})/(\\d{4})\\b");

    private static final Pattern DATE_DASH_PATTERN = Pattern.compile(
            "\\b(\\d{1,2})-(\\d{1,2})-(\\d{4})\\b");

    private static final Pattern DATE_DOT_PATTERN = Pattern.compile(
            "\\b(\\d{1,2})\\.(\\d{1,2})\\.(\\d{4})\\b");

    private static final Pattern BOOKING_ID_PATTERN = Pattern.compile(
            "(?:cancel|cancel\\s+booking|#)\\s*#?\\s*(\\d+)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern ANY_NUMBER_PATTERN = Pattern.compile("\\b(\\d+)\\b");

    public static ParsedCommand parse(String text) {
        ParsedCommand cmd = new ParsedCommand();
        cmd.rawText = text;

        if (text == null || text.trim().isEmpty()) {
            cmd.command = BotCommand.UNKNOWN;
            return cmd;
        }

        String normalized = text.toLowerCase().trim();
        cmd.command = BotCommand.UNKNOWN;

        // RESET / CANCEL CONVERSATION — highest priority
        if (normalized.equals("reset") || normalized.equals("start over") || normalized.equals("restart")
                || normalized.equals("cancel") || normalized.equals("abort") || normalized.equals("exit")
                || normalized.equals("quit") || normalized.equals("stop") || normalized.equals("nevermind")
                || normalized.equals("never mind") || normalized.equals("forget it")) {
            cmd.command = BotCommand.RESET;
            return cmd;
        }

        // GREETING — only pure greetings, not "hi, book a room"
        if (isGreeting(normalized)) {
            cmd.command = BotCommand.GREETING;
            return cmd;
        }

        // HELP
        if (normalized.equals("help") || normalized.equals("?") || normalized.equals("commands")
                || normalized.equals("what can you do") || normalized.startsWith("help me")) {
            cmd.command = BotCommand.HELP;
            return cmd;
        }

        // LIST ROOMS
        if ((normalized.contains("list") || normalized.contains("show") || normalized.contains("all"))
                && normalized.contains("room") && !normalized.contains("booking") && !normalized.contains("availability")) {
            cmd.command = BotCommand.LIST_ROOMS;
            return cmd;
        }

        // CANCEL BOOKING — must check before MY_BOOKINGS since "cancel my booking" has both
        if (normalized.contains("cancel") && (normalized.contains("booking") || hasNumber(normalized))) {
            cmd.command = BotCommand.CANCEL_BOOKING;
            cmd.bookingId = extractBookingId(normalized);
            return cmd;
        }

        // MY BOOKINGS
        if (normalized.contains("my booking") || normalized.contains("my bookings")
                || normalized.contains("my meetings") || normalized.contains("my meeting")
                || normalized.contains("show booking") || normalized.contains("show my booking")
                || normalized.contains("list booking") || normalized.contains("view booking")) {
            cmd.command = BotCommand.MY_BOOKINGS;
            return cmd;
        }

        // CHECK AVAILABILITY — must contain "avail" or "free room" or "open room"
        if (normalized.contains("availability") || normalized.contains("available")
                || normalized.contains("free room") || normalized.contains("open room")
                || (normalized.contains("check") && normalized.contains("room"))) {
            cmd.command = BotCommand.CHECK_AVAILABILITY;
            cmd.date = extractDate(normalized);
            cmd.startTime = extractTime(normalized, "start", "from");
            cmd.endTime = extractTime(normalized, "end", "to", "until");
            return cmd;
        }

        // BOOK ROOM
        if (normalized.contains("book") || normalized.contains("reserve") || normalized.contains("schedule")) {
            cmd.command = BotCommand.BOOK_ROOM;
            cmd.roomName = extractRoomName(normalized);
            cmd.date = extractDate(normalized);
            cmd.startTime = extractTime(normalized, "start", "from", "at");
            cmd.endTime = extractTime(normalized, "end", "to", "until");
            return cmd;
        }

        return cmd;
    }

    private static boolean isGreeting(String text) {
        String trimmed = text.replaceAll("[!,.]+$", "").trim();
        return trimmed.equals("hi") || trimmed.equals("hello") || trimmed.equals("hey")
                || trimmed.equals("hii") || trimmed.equals("hiii") || trimmed.equals("yo")
                || trimmed.equals("sup") || trimmed.equals("greetings")
                || trimmed.equals("good morning") || trimmed.equals("good afternoon")
                || trimmed.equals("good evening") || trimmed.equals("hi there")
                || trimmed.equals("hello there");
    }

    private static boolean hasNumber(String text) {
        return ANY_NUMBER_PATTERN.matcher(text).find();
    }

    private static LocalDate extractDate(String text) {
        if (text.contains("today")) {
            return LocalDate.now();
        }
        if (text.contains("tomorrow") || text.contains("tmrw") || text.contains("tmrw")) {
            return LocalDate.now().plusDays(1);
        }
        if (text.contains("day after tomorrow") || text.contains("day after")) {
            return LocalDate.now().plusDays(2);
        }
        if (text.contains("next week")) {
            return LocalDate.now().plusWeeks(1);
        }
        if (text.contains("next month")) {
            return LocalDate.now().plusMonths(1);
        }

        // Day-of-week names: "this Monday", "next Friday", "Monday", "friday"
        LocalDate dowDate = extractDayOfWeek(text);
        if (dowDate != null) {
            return dowDate;
        }

        // DD/MM/YYYY
        Matcher slashMatcher = DATE_SLASH_PATTERN.matcher(text);
        if (slashMatcher.find()) {
            return buildDate(slashMatcher.group(1), slashMatcher.group(2), slashMatcher.group(3));
        }

        // DD-MM-YYYY
        Matcher dashMatcher = DATE_DASH_PATTERN.matcher(text);
        if (dashMatcher.find()) {
            return buildDate(dashMatcher.group(1), dashMatcher.group(2), dashMatcher.group(3));
        }

        // DD.MM.YYYY
        Matcher dotMatcher = DATE_DOT_PATTERN.matcher(text);
        if (dotMatcher.find()) {
            return buildDate(dotMatcher.group(1), dotMatcher.group(2), dotMatcher.group(3));
        }

        return null;
    }

    private static LocalDate extractDayOfWeek(String text) {
        String[] dayNames = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
        String[] dayAbbrevs = {"mon", "tue", "wed", "thu", "fri", "sat", "sun"};
        DayOfWeek[] dows = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY};

        for (int i = 0; i < dayNames.length; i++) {
            if (text.contains(dayNames[i]) || text.matches(".*\\b" + dayAbbrevs[i] + "\\b.*")) {
                boolean isNext = text.contains("next");
                LocalDate today = LocalDate.now();
                LocalDate target = today.with(TemporalAdjusters.nextOrSame(dows[i]));
                if (isNext && target.equals(today)) {
                    target = today.with(TemporalAdjusters.next(dows[i]));
                }
                if (!isNext && target.isBefore(today)) {
                    target = today.with(TemporalAdjusters.next(dows[i]));
                }
                return target;
            }
        }
        return null;
    }

    private static LocalDate buildDate(String dayStr, String monthStr, String yearStr) {
        try {
            int day = Integer.parseInt(dayStr);
            int month = Integer.parseInt(monthStr);
            int year = Integer.parseInt(yearStr);
            if (month < 1 || month > 12 || day < 1 || day > 31) {
                return null;
            }
            return LocalDate.of(year, month, day);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LocalTime extractTime(String text, String... keywords) {
        // First try keyword-based extraction: "from 10am", "to 12pm", "start 10:00"
        for (String keyword : keywords) {
            Pattern keywordPattern = Pattern.compile(
                    keyword + "\\s+(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?",
                    Pattern.CASE_INSENSITIVE);
            Matcher matcher = keywordPattern.matcher(text);
            if (matcher.find()) {
                return parseTimeFromGroups(matcher);
            }
        }

        // Try generic HH:MM format
        Matcher timeMatcher = TIME_GENERIC_PATTERN.matcher(text);
        if (timeMatcher.find()) {
            return parseTimeFromGroups(timeMatcher);
        }

        // Try "10am", "2pm" format
        Matcher hourMatcher = HOUR_PATTERN.matcher(text);
        if (hourMatcher.find()) {
            int hour = Integer.parseInt(hourMatcher.group(1));
            String ampm = hourMatcher.group(2).toLowerCase();
            if (ampm.equals("pm") && hour != 12) hour += 12;
            else if (ampm.equals("am") && hour == 12) hour = 0;
            return LocalTime.of(hour, 0);
        }

        // Try 24-hour HHMM format (e.g. "1400", "0930") — only if no am/pm context
        if (!text.contains("am") && !text.contains("pm")) {
            Matcher h24Matcher = HOUR_MINUTE_24H_PATTERN.matcher(text);
            if (h24Matcher.find()) {
                int hour = Integer.parseInt(h24Matcher.group(1));
                int minute = Integer.parseInt(h24Matcher.group(2));
                return LocalTime.of(hour, minute);
            }
        }

        return null;
    }

    private static LocalTime parseTimeFromGroups(Matcher matcher) {
        int hour = Integer.parseInt(matcher.group(1));
        int minute = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
        if (matcher.group(3) != null) {
            String ampm = matcher.group(3).toLowerCase();
            if (ampm.equals("pm") && hour != 12) hour += 12;
            else if (ampm.equals("am") && hour == 12) hour = 0;
        }
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            return null;
        }
        return LocalTime.of(hour, minute);
    }

    private static String extractRoomName(String text) {
        // Remove the command prefix
        String cleaned = text.replaceFirst("(?i)^(book|reserve|schedule)\\s+", "").trim();

        // Try to extract room name up to date/time keywords
        // Matches: "conference room a", "meeting room b", "boardroom", "room 5"
        Pattern roomPattern = Pattern.compile(
                "([a-z0-9\\s-]+?)\\s+(?:on|for|at|from|today|tomorrow|this|next|\\d{1,2}[:/\\s]|$)",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = roomPattern.matcher(cleaned);
        if (matcher.find()) {
            String name = matcher.group(1).trim();
            if (!name.isEmpty()) {
                return name;
            }
        }

        // If the word "room" appears, capture up to and including it plus optional suffix
        Pattern roomWordPattern = Pattern.compile(
                "([a-z0-9\\s-]*?\\broom\\b(?:\\s+[a-z0-9-]+)?)",
                Pattern.CASE_INSENSITIVE);
        Matcher roomWordMatcher = roomWordPattern.matcher(cleaned);
        if (roomWordMatcher.find()) {
            String name = roomWordMatcher.group(1).trim();
            if (!name.isEmpty()) {
                return name;
            }
        }

        // Single word room name (e.g. "boardroom", "conference")
        if (!cleaned.isEmpty() && !cleaned.matches(".*\\d{1,2}[:/].*")) {
            String[] words = cleaned.split("\\s+");
            if (words.length == 1) {
                return words[0];
            }
        }

        return null;
    }

    private static Long extractBookingId(String text) {
        // "cancel booking 5", "cancel #5", "cancel 5", "#5"
        Matcher idMatcher = BOOKING_ID_PATTERN.matcher(text);
        if (idMatcher.find()) {
            try {
                return Long.parseLong(idMatcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // Fallback: any number in the text
        Matcher numMatcher = ANY_NUMBER_PATTERN.matcher(text);
        if (numMatcher.find()) {
            try {
                return Long.parseLong(numMatcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }
}
