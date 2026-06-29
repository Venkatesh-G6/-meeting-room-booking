package com.yourcompany.roombooking.repository;

import com.yourcompany.roombooking.entity.Room;
import com.yourcompany.roombooking.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findAllByActiveTrue();

    List<Room> findAllByRoomTypeAndActiveTrue(RoomType roomType);

    boolean existsByRoomName(String roomName);

    @Query("SELECT r FROM Room r WHERE r.active = true AND r.capacity >= :minCapacity AND (:bookedRoomIds IS NULL OR r.id NOT IN :bookedRoomIds)")
    List<Room> findAvailableRooms(
            @Param("minCapacity") Integer minCapacity,
            @Param("bookedRoomIds") List<UUID> bookedRoomIds
    );
}
