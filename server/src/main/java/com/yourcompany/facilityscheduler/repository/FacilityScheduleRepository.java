package com.yourcompany.facilityscheduler.repository;

import com.yourcompany.facilityscheduler.entity.FacilitySchedule;
import com.yourcompany.facilityscheduler.enums.FacilityScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FacilityScheduleRepository extends JpaRepository<FacilitySchedule, Long> {

    @Query("SELECT b FROM FacilitySchedule b WHERE b.room.id = :roomId " +
            "AND b.status = :status " +
            "AND b.startTime < :endTime " +
            "AND b.endTime > :startTime")
    List<FacilitySchedule> findOverlappingFacilitySchedules(@Param("roomId") Long roomId,
                                          @Param("status") FacilityScheduleStatus status,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    @Query("SELECT b FROM FacilitySchedule b WHERE b.startTime >= :startOfDay " +
            "AND b.startTime < :endOfDay " +
            "AND b.status = :status " +
            "ORDER BY b.room.id ASC, b.startTime ASC")
    List<FacilitySchedule> findTodayFacilitySchedules(@Param("startOfDay") LocalDateTime startOfDay,
                                    @Param("endOfDay") LocalDateTime endOfDay,
                                    @Param("status") FacilityScheduleStatus status);

    @Query("SELECT b FROM FacilitySchedule b WHERE b.createdAt >= :fromDate " +
            "AND b.status = :status " +
            "ORDER BY b.createdAt DESC")
    List<FacilitySchedule> findRecentFacilitySchedules(@Param("fromDate") LocalDateTime fromDate,
                                     @Param("status") FacilityScheduleStatus status);

    @Query("SELECT b FROM FacilitySchedule b WHERE b.room.id = :roomId " +
            "AND b.status = :status " +
            "AND b.startTime >= :afterTime " +
            "AND b.startTime < :endOfDay " +
            "ORDER BY b.startTime ASC")
    Optional<FacilitySchedule> findNextAvailableSlot(@Param("roomId") Long roomId,
                                            @Param("status") FacilityScheduleStatus status,
                                            @Param("afterTime") LocalDateTime afterTime,
                                            @Param("endOfDay") LocalDateTime endOfDay);
}
