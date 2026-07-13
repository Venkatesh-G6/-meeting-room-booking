package com.yourcompany.roombooking.repository;

import com.yourcompany.roombooking.entity.Room;
import com.yourcompany.roombooking.enums.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RoomRepositoryTest {

    @Autowired
    private RoomRepository roomRepository;

    @BeforeEach
    void setUp() {
        roomRepository.deleteAll();
    }

    private Room buildRoom(String name, RoomType type, int capacity, boolean active) {
        return Room.builder()
                .roomName(name)
                .roomType(type)
                .capacity(capacity)
                .location("Floor 1")
                .active(active)
                .build();
    }

    @Test
    void findAllByActiveTrue_returnsOnlyActiveRooms() {
        roomRepository.save(buildRoom("Room A", RoomType.MEETING, 10, true));
        roomRepository.save(buildRoom("Room B", RoomType.MEETING, 8, false));

        var page = roomRepository.findAllByActiveTrue(PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getRoomName()).isEqualTo("Room A");
    }

    @Test
    void existsByRoomName_returnsTrueWhenExists() {
        roomRepository.save(buildRoom("Unique Room", RoomType.POD, 4, true));

        assertThat(roomRepository.existsByRoomName("Unique Room")).isTrue();
        assertThat(roomRepository.existsByRoomName("Nonexistent Room")).isFalse();
    }

    @Test
    void findAllByRoomTypeAndActiveTrue_filtersCorrectly() {
        roomRepository.save(buildRoom("Training Room", RoomType.TRAINING, 15, true));
        roomRepository.save(buildRoom("Meeting Room", RoomType.MEETING, 10, true));
        roomRepository.save(buildRoom("Inactive Training", RoomType.TRAINING, 15, false));

        List<Room> trainingRooms = roomRepository.findAllByRoomTypeAndActiveTrue(RoomType.TRAINING);

        assertThat(trainingRooms).hasSize(1);
        assertThat(trainingRooms.get(0).getRoomName()).isEqualTo("Training Room");
    }

    @Test
    void findAllByActiveTrueAndCapacityGreaterThanEqual_returnsMatchingRooms() {
        roomRepository.save(buildRoom("Small Room", RoomType.POD, 2, true));
        roomRepository.save(buildRoom("Big Room", RoomType.TRAINING, 20, true));

        List<Room> rooms = roomRepository.findAllByActiveTrueAndCapacityGreaterThanEqual(10);

        assertThat(rooms).hasSize(1);
        assertThat(rooms.get(0).getRoomName()).isEqualTo("Big Room");
    }

    @Test
    void findAvailableRooms_excludesBookedRoomIds() {
        Room availableRoom = roomRepository.save(buildRoom("Available Room", RoomType.MEETING, 10, true));
        Room bookedRoom = roomRepository.save(buildRoom("Booked Room", RoomType.MEETING, 10, true));

        List<Room> result = roomRepository.findAvailableRooms(5, List.of(bookedRoom.getId()));

        assertThat(result).extracting(Room::getId).containsExactly(availableRoom.getId());
    }
}
