package com.yourcompany.roombooking.repository;

import com.yourcompany.roombooking.entity.Room;
import com.yourcompany.roombooking.enums.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findAllByStatus(RoomStatus status);
}
