package com.yourcompany.facilityscheduler.repository;

import com.yourcompany.facilityscheduler.entity.Room;
import com.yourcompany.facilityscheduler.enums.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findAllByStatus(RoomStatus status);
}
