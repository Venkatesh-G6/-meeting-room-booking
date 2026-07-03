package com.yourcompany.roombooking.dto.response;

import com.yourcompany.roombooking.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {

    private Long id;
    private String actorEmail;
    private AuditAction action;
    private String entityType;
    private String entityId;
    private String metaJson;
    private LocalDateTime createdAt;
}
