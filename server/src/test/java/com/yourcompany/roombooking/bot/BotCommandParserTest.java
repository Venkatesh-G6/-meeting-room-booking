package com.yourcompany.roombooking.bot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BotCommandParser Tests")
class BotCommandParserTest {

    @Test
    @DisplayName("CHECK_AVAILABILITY - with 'availability' keyword")
    void parse_availabilityKeyword_returnsCheckAvailability() {
        ParsedCommand cmd = ParsedCommand.parse("check availability for today");
        assertEquals(BotCommand.CHECK_AVAILABILITY, cmd.getCommand());
        assertEquals(LocalDate.now(), cmd.getDate());
    }

    @Test
    @DisplayName("CHECK_AVAILABILITY - with 'available' keyword and times")
    void parse_availableKeywordWithTimes_returnsCheckAvailabilityWithTimes() {
        ParsedCommand cmd = ParsedCommand.parse("available rooms from 10:00 to 12:00 tomorrow");
        assertEquals(BotCommand.CHECK_AVAILABILITY, cmd.getCommand());
        assertEquals(LocalDate.now().plusDays(1), cmd.getDate());
        assertEquals(LocalTime.of(10, 0), cmd.getStartTime());
        assertEquals(LocalTime.of(12, 0), cmd.getEndTime());
    }

    @Test
    @DisplayName("CHECK_AVAILABILITY - with 'check' + 'room' keyword and am/pm times")
    void parse_checkKeywordWithAmPm_returnsCheckAvailabilityWithTimes() {
        ParsedCommand cmd = ParsedCommand.parse("check room 10am to 2pm");
        assertEquals(BotCommand.CHECK_AVAILABILITY, cmd.getCommand());
        assertEquals(LocalTime.of(10, 0), cmd.getStartTime());
        assertEquals(LocalTime.of(14, 0), cmd.getEndTime());
    }

    @Test
    @DisplayName("CHECK_AVAILABILITY - with dd/MM/yyyy date")
    void parse_checkAvailabilityWithDatePattern_returnsCheckAvailabilityWithDate() {
        ParsedCommand cmd = ParsedCommand.parse("check availability on 25/12/2025");
        assertEquals(BotCommand.CHECK_AVAILABILITY, cmd.getCommand());
        assertEquals(LocalDate.of(2025, 12, 25), cmd.getDate());
    }

    @Test
    @DisplayName("BOOK_ROOM - with 'book' keyword and room name")
    void parse_bookKeyword_returnsBookRoomWithRoomName() {
        ParsedCommand cmd = ParsedCommand.parse("book Conference Room A for today from 2pm to 4pm");
        assertEquals(BotCommand.BOOK_ROOM, cmd.getCommand());
        assertNotNull(cmd.getRoomName());
        assertEquals(LocalDate.now(), cmd.getDate());
        assertEquals(LocalTime.of(14, 0), cmd.getStartTime());
        assertEquals(LocalTime.of(16, 0), cmd.getEndTime());
    }

    @Test
    @DisplayName("BOOK_ROOM - with 'reserve' keyword")
    void parse_reserveKeyword_returnsBookRoom() {
        ParsedCommand cmd = ParsedCommand.parse("reserve Boardroom on 15/08/2025 from 9:00 to 10:30");
        assertEquals(BotCommand.BOOK_ROOM, cmd.getCommand());
        assertEquals(LocalDate.of(2025, 8, 15), cmd.getDate());
        assertEquals(LocalTime.of(9, 0), cmd.getStartTime());
        assertEquals(LocalTime.of(10, 30), cmd.getEndTime());
    }

    @Test
    @DisplayName("MY_BOOKINGS - with 'my booking' keyword")
    void parse_myBookingKeyword_returnsMyBookings() {
        ParsedCommand cmd = ParsedCommand.parse("show my bookings");
        assertEquals(BotCommand.MY_BOOKINGS, cmd.getCommand());
    }

    @Test
    @DisplayName("MY_BOOKINGS - with 'my meetings' keyword")
    void parse_myMeetingsKeyword_returnsMyBookings() {
        ParsedCommand cmd = ParsedCommand.parse("my meetings");
        assertEquals(BotCommand.MY_BOOKINGS, cmd.getCommand());
    }

    @Test
    @DisplayName("MY_BOOKINGS - with 'show booking' keyword")
    void parse_showBookingKeyword_returnsMyBookings() {
        ParsedCommand cmd = ParsedCommand.parse("show booking");
        assertEquals(BotCommand.MY_BOOKINGS, cmd.getCommand());
    }

    @Test
    @DisplayName("CANCEL_BOOKING - with booking ID after 'cancel'")
    void parse_cancelWithId_returnsCancelBookingWithId() {
        ParsedCommand cmd = ParsedCommand.parse("cancel 123");
        assertEquals(BotCommand.CANCEL_BOOKING, cmd.getCommand());
        assertEquals(123L, cmd.getBookingId());
    }

    @Test
    @DisplayName("CANCEL_BOOKING - with larger booking ID")
    void parse_cancelWithLargeId_returnsCancelBookingWithId() {
        ParsedCommand cmd = ParsedCommand.parse("cancel booking 99999");
        assertEquals(BotCommand.CANCEL_BOOKING, cmd.getCommand());
        assertNotNull(cmd.getBookingId());
    }

    @Test
    @DisplayName("CANCEL_BOOKING - without ID returns null bookingId")
    void parse_cancelWithoutId_returnsCancelBookingWithNullId() {
        ParsedCommand cmd = ParsedCommand.parse("cancel my booking");
        assertEquals(BotCommand.CANCEL_BOOKING, cmd.getCommand());
        assertNull(cmd.getBookingId());
    }

    @Test
    @DisplayName("HELP - with 'help' keyword")
    void parse_helpKeyword_returnsHelp() {
        ParsedCommand cmd = ParsedCommand.parse("help");
        assertEquals(BotCommand.HELP, cmd.getCommand());
    }

    @Test
    @DisplayName("GREETING - with 'hi' keyword")
    void parse_hiKeyword_returnsGreeting() {
        ParsedCommand cmd = ParsedCommand.parse("hi");
        assertEquals(BotCommand.GREETING, cmd.getCommand());
    }

    @Test
    @DisplayName("GREETING - with 'hello' keyword")
    void parse_helloKeyword_returnsGreeting() {
        ParsedCommand cmd = ParsedCommand.parse("hello");
        assertEquals(BotCommand.GREETING, cmd.getCommand());
    }

    @Test
    @DisplayName("GREETING - with 'hey' keyword")
    void parse_heyKeyword_returnsGreeting() {
        ParsedCommand cmd = ParsedCommand.parse("hey");
        assertEquals(BotCommand.GREETING, cmd.getCommand());
    }

    @Test
    @DisplayName("GREETING - with 'good morning' keyword")
    void parse_goodMorningKeyword_returnsGreeting() {
        ParsedCommand cmd = ParsedCommand.parse("good morning");
        assertEquals(BotCommand.GREETING, cmd.getCommand());
    }

    @Test
    @DisplayName("UNKNOWN - unrecognized text")
    void parse_unrecognizedText_returnsUnknown() {
        ParsedCommand cmd = ParsedCommand.parse("what is the weather today");
        assertEquals(BotCommand.UNKNOWN, cmd.getCommand());
    }

    @Test
    @DisplayName("UNKNOWN - empty text")
    void parse_emptyText_returnsUnknown() {
        ParsedCommand cmd = ParsedCommand.parse("");
        assertEquals(BotCommand.UNKNOWN, cmd.getCommand());
    }

    @Test
    @DisplayName("UNKNOWN - null text")
    void parse_nullText_returnsUnknown() {
        ParsedCommand cmd = ParsedCommand.parse(null);
        assertEquals(BotCommand.UNKNOWN, cmd.getCommand());
    }

    @Test
    @DisplayName("RESET - with 'reset' keyword")
    void parse_resetKeyword_returnsReset() {
        ParsedCommand cmd = ParsedCommand.parse("reset");
        assertEquals(BotCommand.RESET, cmd.getCommand());
    }

    @Test
    @DisplayName("RESET - with 'start over' keyword")
    void parse_startOverKeyword_returnsReset() {
        ParsedCommand cmd = ParsedCommand.parse("start over");
        assertEquals(BotCommand.RESET, cmd.getCommand());
    }

    @Test
    @DisplayName("RESET - with 'cancel' keyword (no booking id)")
    void parse_cancelAlone_returnsReset() {
        ParsedCommand cmd = ParsedCommand.parse("cancel");
        assertEquals(BotCommand.RESET, cmd.getCommand());
    }

    @Test
    @DisplayName("LIST_ROOMS - with 'list rooms' keyword")
    void parse_listRoomsKeyword_returnsListRooms() {
        ParsedCommand cmd = ParsedCommand.parse("list rooms");
        assertEquals(BotCommand.LIST_ROOMS, cmd.getCommand());
    }

    @Test
    @DisplayName("LIST_ROOMS - with 'show all rooms' keyword")
    void parse_showAllRoomsKeyword_returnsListRooms() {
        ParsedCommand cmd = ParsedCommand.parse("show all rooms");
        assertEquals(BotCommand.LIST_ROOMS, cmd.getCommand());
    }

    @Test
    @DisplayName("CHECK_AVAILABILITY - with day-of-week 'monday'")
    void parse_checkAvailabilityWithDayOfWeek_returnsCheckAvailabilityWithDate() {
        ParsedCommand cmd = ParsedCommand.parse("check availability on monday from 10am to 11am");
        assertEquals(BotCommand.CHECK_AVAILABILITY, cmd.getCommand());
        assertNotNull(cmd.getDate());
        assertEquals(LocalTime.of(10, 0), cmd.getStartTime());
        assertEquals(LocalTime.of(11, 0), cmd.getEndTime());
    }

    @Test
    @DisplayName("CHECK_AVAILABILITY - with 'tmrw' abbreviation")
    void parse_tmrwAbbreviation_returnsTomorrowDate() {
        ParsedCommand cmd = ParsedCommand.parse("check availability tmrw from 9am to 10am");
        assertEquals(BotCommand.CHECK_AVAILABILITY, cmd.getCommand());
        assertEquals(LocalDate.now().plusDays(1), cmd.getDate());
    }

    @Test
    @DisplayName("BOOK_ROOM - with 'schedule' keyword")
    void parse_scheduleKeyword_returnsBookRoom() {
        ParsedCommand cmd = ParsedCommand.parse("schedule Boardroom tomorrow from 2pm to 3pm");
        assertEquals(BotCommand.BOOK_ROOM, cmd.getCommand());
        assertEquals(LocalDate.now().plusDays(1), cmd.getDate());
    }

    @Test
    @DisplayName("BOOK_ROOM - with DD-MM-YYYY date format")
    void parse_bookRoomWithDashDate_returnsBookRoomWithDate() {
        ParsedCommand cmd = ParsedCommand.parse("book Boardroom on 25-12-2025 from 10am to 11am");
        assertEquals(BotCommand.BOOK_ROOM, cmd.getCommand());
        assertEquals(LocalDate.of(2025, 12, 25), cmd.getDate());
    }

    @Test
    @DisplayName("BOOK_ROOM - with DD.MM.YYYY date format")
    void parse_bookRoomWithDotDate_returnsBookRoomWithDate() {
        ParsedCommand cmd = ParsedCommand.parse("book Boardroom on 25.12.2025 from 10am to 11am");
        assertEquals(BotCommand.BOOK_ROOM, cmd.getCommand());
        assertEquals(LocalDate.of(2025, 12, 25), cmd.getDate());
    }

    @Test
    @DisplayName("CANCEL_BOOKING - with '#5' format")
    void parse_cancelWithHashId_returnsCancelBookingWithId() {
        ParsedCommand cmd = ParsedCommand.parse("cancel #5");
        assertEquals(BotCommand.CANCEL_BOOKING, cmd.getCommand());
        assertEquals(5L, cmd.getBookingId());
    }

    @Test
    @DisplayName("HELP - with '?' keyword")
    void parse_questionMarkKeyword_returnsHelp() {
        ParsedCommand cmd = ParsedCommand.parse("?");
        assertEquals(BotCommand.HELP, cmd.getCommand());
    }

    @Test
    @DisplayName("HELP - with 'commands' keyword")
    void parse_commandsKeyword_returnsHelp() {
        ParsedCommand cmd = ParsedCommand.parse("commands");
        assertEquals(BotCommand.HELP, cmd.getCommand());
    }

    @Test
    @DisplayName("rawText preserved in parsed command")
    void parse_rawTextPreserved() {
        String input = "Check availability today";
        ParsedCommand cmd = ParsedCommand.parse(input);
        assertEquals(input, cmd.getRawText());
    }

    @Test
    @DisplayName("Case insensitive - uppercase input")
    void parse_uppercaseInput_normalizesCorrectly() {
        ParsedCommand cmd = ParsedCommand.parse("BOOK ROOM A TODAY FROM 10AM TO 11AM");
        assertEquals(BotCommand.BOOK_ROOM, cmd.getCommand());
        assertEquals(LocalDate.now(), cmd.getDate());
    }
}
