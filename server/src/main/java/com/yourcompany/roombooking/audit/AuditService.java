package com.yourcompany.roombooking.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourcompany.roombooking.dto.response.AuditLogResponse;
import com.yourcompany.roombooking.dto.response.PagedResponse;
import com.yourcompany.roombooking.entity.AuditLog;
import com.yourcompany.roombooking.enums.AuditAction;
import com.yourcompany.roombooking.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Async
    public void log(
            String actorEmail,
            AuditAction action,
            String entityType,
            String entityId,
            Object meta) {

        String metaJson;
        try {
            metaJson = new ObjectMapper().writeValueAsString(meta);
        } catch (Exception e) {
            metaJson = "{}";
        }

        AuditLog auditLog = AuditLog.builder()
                .actorEmail(actorEmail)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .metaJson(metaJson)
                .build();

        auditLogRepository.save(auditLog);
    }

    public PagedResponse<AuditLogResponse> getAllAuditLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogPage = auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<AuditLogResponse> responses = auditLogPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return PagedResponse.of(auditLogPage, responses);
    }

    public List<AuditLogResponse> getAuditLogsByEntity(String entityType, String entityId) {
        return auditLogRepository.findAllByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AuditLogResponse> getAuditLogsByActor(String email) {
        return auditLogRepository.findAllByActorEmailOrderByCreatedAtDesc(email).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AuditLogResponse mapToResponse(AuditLog auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .actorEmail(auditLog.getActorEmail())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .metaJson(auditLog.getMetaJson())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}
