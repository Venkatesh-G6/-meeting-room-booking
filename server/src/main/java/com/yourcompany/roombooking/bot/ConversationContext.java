package com.yourcompany.roombooking.bot;

import com.yourcompany.roombooking.dto.response.RoomResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationContext {

    private ConversationStep step;
    private BotCommand command;
    private LocalDate selectedDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long selectedRoomId;
    private String selectedRoomName;
    private Long pendingBookingId;
    private List<RoomResponse> availableRooms;
    private String userEmail;
    private LocalDateTime lastUpdated;

    public void reset() {
        step = ConversationStep.IDLE;
        command = null;
        selectedDate = null;
        startTime = null;
        endTime = null;
        selectedRoomId = null;
        selectedRoomName = null;
        pendingBookingId = null;
        availableRooms = new ArrayList<>();
        userEmail = null;
        lastUpdated = LocalDateTime.now();
    }

    public boolean isExpired() {
        if (lastUpdated == null) {
            return true;
        }
        return lastUpdated.isBefore(LocalDateTime.now().minusMinutes(10));
    }
}
