package com.yourcompany.roombooking.controller;

import com.yourcompany.roombooking.audit.AuditService;
import com.yourcompany.roombooking.dto.response.AuditLogResponse;
import com.yourcompany.roombooking.dto.response.PagedResponse;
import com.yourcompany.roombooking.util.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@Tag(name = "Audit Logs")
public class AuditLogController {

    private final AuditService auditService;

    public AuditLogController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getAllAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<AuditLogResponse> logs = auditService.getAllAuditLogs(page, size);
        return ResponseEntity.ok(ApiResponse.success("Audit logs fetched successfully", logs));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogsByEntity(
            @PathVariable String entityType,
            @PathVariable String entityId) {
        List<AuditLogResponse> logs = auditService.getAuditLogsByEntity(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success("Entity audit logs fetched", logs));
    }

    @GetMapping("/actor/{email}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogsByActor(
            @PathVariable String email) {
        List<AuditLogResponse> logs = auditService.getAuditLogsByActor(email);
        return ResponseEntity.ok(ApiResponse.success("Actor audit logs fetched", logs));
    }
}
