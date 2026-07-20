package com.yourcompany.roombooking.bot;

public enum ConversationStep {
    IDLE,
    AWAITING_DATE,
    AWAITING_START_TIME,
    AWAITING_END_TIME,
    AWAITING_ROOM_SELECTION,
    AWAITING_BOOKING_CONFIRMATION,
    AWAITING_CANCEL_CONFIRMATION
}
