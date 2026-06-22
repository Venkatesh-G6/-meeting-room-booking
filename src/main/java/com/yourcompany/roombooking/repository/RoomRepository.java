package com.yourcompany.roombooking.repository;

import com.yourcompany.roombooking.entity.Room;
import com.yourcompany.roombooking.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findAllByActiveTrue();

    List<Room> findAllByRoomTypeAndActiveTrue(RoomType roomType);

    boolean existsByRoomName(String roomName);
}
