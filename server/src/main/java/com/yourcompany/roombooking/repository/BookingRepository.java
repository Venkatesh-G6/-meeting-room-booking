package com.yourcompany.roombooking.repository;

import com.yourcompany.roombooking.entity.Booking;
import com.yourcompany.roombooking.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId " +
            "AND b.status = :status " +
            "AND b.startTime < :endTime " +
            "AND b.endTime > :startTime")
    List<Booking> findOverlappingBookings(@Param("roomId") Long roomId,
                                          @Param("status") BookingStatus status,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    @Query("SELECT b FROM Booking b WHERE b.startTime >= :startOfDay " +
            "AND b.startTime < :endOfDay " +
            "AND b.status = :status " +
            "ORDER BY b.room.id ASC, b.startTime ASC")
    List<Booking> findTodayBookings(@Param("startOfDay") LocalDateTime startOfDay,
                                    @Param("endOfDay") LocalDateTime endOfDay,
                                    @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.createdAt >= :fromDate " +
            "AND b.status = :status " +
            "ORDER BY b.createdAt DESC")
    List<Booking> findRecentBookings(@Param("fromDate") LocalDateTime fromDate,
                                     @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId " +
            "AND b.status = :status " +
            "AND b.startTime >= :afterTime " +
            "AND b.startTime < :endOfDay " +
            "ORDER BY b.startTime ASC")
    Optional<Booking> findNextAvailableSlot(@Param("roomId") Long roomId,
                                            @Param("status") BookingStatus status,
                                            @Param("afterTime") LocalDateTime afterTime,
                                            @Param("endOfDay") LocalDateTime endOfDay);
}
