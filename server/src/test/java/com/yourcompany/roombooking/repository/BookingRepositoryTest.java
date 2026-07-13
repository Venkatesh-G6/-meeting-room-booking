package com.yourcompany.roombooking.repository;

import com.yourcompany.roombooking.entity.Booking;
import com.yourcompany.roombooking.entity.Room;
import com.yourcompany.roombooking.enums.BookingStatus;
import com.yourcompany.roombooking.enums.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    private Room room;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        room = roomRepository.save(Room.builder()
                .roomName("Test Room")
                .roomType(RoomType.MEETING)
                .capacity(10)
                .location("Floor 1")
                .active(true)
                .build());
    }

    private Booking buildBooking(String bookedBy, LocalDateTime start, LocalDateTime end, BookingStatus status) {
        return Booking.builder()
                .room(room)
                .bookedBy(bookedBy)
                .title("Meeting")
                .attendeeCount(3)
                .startTime(start)
                .endTime(end)
                .status(status)
                .build();
    }

    @Test
    void findAllByBookedByOrderByStartTimeDesc_returnsOnlyMatchingUser() {
        LocalDateTime baseStart = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        bookingRepository.save(buildBooking("alice@company.com",
                baseStart, baseStart.plusHours(1), BookingStatus.CONFIRMED));
        bookingRepository.save(buildBooking("bob@company.com",
                baseStart, baseStart.plusHours(1), BookingStatus.CONFIRMED));

        var page = bookingRepository.findAllByBookedByOrderByStartTimeDesc("alice@company.com", PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getBookedBy()).isEqualTo("alice@company.com");
    }

    @Test
    void findOverlappingBookings_detectsOverlap() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);
        bookingRepository.save(buildBooking("alice@company.com", start, end, BookingStatus.CONFIRMED));

        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                room.getId(), start.plusMinutes(30), end.plusMinutes(30));

        assertThat(overlapping).hasSize(1);
    }

    @Test
    void findOverlappingBookings_ignoresCancelledBookings() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);
        bookingRepository.save(buildBooking("alice@company.com", start, end, BookingStatus.CANCELLED));

        List<Booking> overlapping = bookingRepository.findOverlappingBookings(room.getId(), start, end);

        assertThat(overlapping).isEmpty();
    }

    @Test
    void findDuplicateBooking_findsExactMatch() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);
        bookingRepository.save(buildBooking("alice@company.com", start, end, BookingStatus.CONFIRMED));

        Optional<Booking> duplicate = bookingRepository.findDuplicateBooking(
                room.getId(), "alice@company.com", start, end);

        assertThat(duplicate).isPresent();
    }

    @Test
    void findDuplicateBooking_noDuplicateForDifferentUser() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);
        bookingRepository.save(buildBooking("alice@company.com", start, end, BookingStatus.CONFIRMED));

        Optional<Booking> duplicate = bookingRepository.findDuplicateBooking(
                room.getId(), "bob@company.com", start, end);

        assertThat(duplicate).isEmpty();
    }

    @Test
    void findBookedRoomIds_returnsDistinctRoomIdsForOverlappingWindow() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);
        bookingRepository.save(buildBooking("alice@company.com", start, end, BookingStatus.CONFIRMED));

        List<java.util.UUID> bookedRoomIds = bookingRepository.findBookedRoomIds(start, end);

        assertThat(bookedRoomIds).containsExactly(room.getId());
    }

    @Test
    void findBookedRoomIds_emptyWhenNoOverlap() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);
        bookingRepository.save(buildBooking("alice@company.com", start, end, BookingStatus.CONFIRMED));

        List<java.util.UUID> bookedRoomIds = bookingRepository.findBookedRoomIds(
                start.plusHours(5), end.plusHours(6));

        assertThat(bookedRoomIds).isEmpty();
    }
}
