package com.yourcompany.roombooking.graph;

public record GraphUserProfile(
        String id,
        String email,
        String displayName,
        String jobTitle,
        String department
) {}
