package com.yourcompany.roombooking.audit;

import java.util.UUID;

public class AuditMeta {

    public record BookingMeta(
            UUID bookingId,
            UUID roomId,
            String roomName,
            String bookedBy,
            String startTime,
            String endTime
    ) {}

    public record RoomMeta(
            UUID roomId,
            String roomName,
            String roomType,
            Integer capacity
    ) {}
}
