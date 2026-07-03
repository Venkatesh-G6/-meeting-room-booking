package com.yourcompany.roombooking.repository;

import com.yourcompany.roombooking.entity.Booking;
import com.yourcompany.roombooking.enums.BookingStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Page<Booking> findAllByBookedByOrderByStartTimeDesc(String bookedBy, Pageable pageable);

    Page<Booking> findAll(Pageable pageable);

    List<Booking> findAllByRoomIdAndStatus(UUID roomId, BookingStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId AND b.status = 'CONFIRMED' AND b.startTime < :endTime AND b.endTime > :startTime")
    List<Booking> findOverlappingBookings(
            @Param("roomId") UUID roomId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId AND b.bookedBy = :bookedBy AND b.status = 'CONFIRMED' AND b.startTime = :startTime AND b.endTime = :endTime")
    Optional<Booking> findDuplicateBooking(
            @Param("roomId") UUID roomId,
            @Param("bookedBy") String bookedBy,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT DISTINCT b.room.id FROM Booking b WHERE b.status = 'CONFIRMED' AND b.startTime < :endTime AND b.endTime > :startTime")
    List<UUID> findBookedRoomIds(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
