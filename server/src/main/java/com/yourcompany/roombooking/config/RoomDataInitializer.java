package com.yourcompany.roombooking.config;

import com.yourcompany.roombooking.entity.Room;
import com.yourcompany.roombooking.enums.RoomType;
import com.yourcompany.roombooking.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomDataInitializer implements ApplicationRunner {

    private final RoomRepository roomRepository;

    private static final List<Room> SEED_ROOMS = List.of(
            Room.builder()
                    .roomName("ROY")
                    .roomType(RoomType.MEETING)
                    .capacity(12)
                    .location("Main Floor")
                    .active(true)
                    .build(),
            Room.builder()
                    .roomName("KALAM")
                    .roomType(RoomType.MEETING)
                    .capacity(12)
                    .location("Main Floor")
                    .active(true)
                    .build(),
            Room.builder()
                    .roomName("VIVEKANANDA")
                    .roomType(RoomType.MEETING)
                    .capacity(12)
                    .location("Main Floor")
                    .active(true)
                    .build(),
            Room.builder()
                    .roomName("AUROBINDO")
                    .roomType(RoomType.MEETING)
                    .capacity(12)
                    .location("Main Floor")
                    .active(true)
                    .build(),
            Room.builder()
                    .roomName("Client Space 1")
                    .roomType(RoomType.MEETING)
                    .capacity(4)
                    .location("Reception")
                    .active(true)
                    .build(),
            Room.builder()
                    .roomName("Client Space 2")
                    .roomType(RoomType.MEETING)
                    .capacity(4)
                    .location("Reception")
                    .active(true)
                    .build(),
            Room.builder()
                    .roomName("JANE GOODALL")
                    .roomType(RoomType.POD)
                    .capacity(2)
                    .location("Pod Area")
                    .active(true)
                    .build(),
            Room.builder()
                    .roomName("HAWKING")
                    .roomType(RoomType.POD)
                    .capacity(2)
                    .location("Pod Area")
                    .active(true)
                    .build(),
            Room.builder()
                    .roomName("STROUSTRUP")
                    .roomType(RoomType.POD)
                    .capacity(2)
                    .location("Pod Area")
                    .active(true)
                    .build(),
            Room.builder()
                    .roomName("RICH HICKEY")
                    .roomType(RoomType.POD)
                    .capacity(2)
                    .location("Pod Area")
                    .active(true)
                    .build()
    );

    @Override
    public void run(ApplicationArguments args) {
        long existingCount = roomRepository.count();
        if (existingCount > 0) {
            log.info("Rooms already exist ({}), skipping seed data", existingCount);
            return;
        }

        log.info("Seeding {} rooms into database...", SEED_ROOMS.size());
        roomRepository.saveAll(SEED_ROOMS);
        log.info("Room seed data inserted successfully: {}", SEED_ROOMS.stream()
                .map(Room::getRoomName).toList());
    }
}
