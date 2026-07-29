package com.yourcompany.roombooking.audit;

import com.yourcompany.roombooking.dto.response.AuditLogResponse;
import com.yourcompany.roombooking.dto.response.PagedResponse;
import com.yourcompany.roombooking.entity.AuditLog;
import com.yourcompany.roombooking.enums.AuditAction;
import com.yourcompany.roombooking.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = AuditLog.builder()
                .id(1L)
                .actorEmail("admin@company.com")
                .action(AuditAction.ROOM_CREATED)
                .entityType("Room")
                .entityId("uuid-123")
                .metaJson("{\"roomName\":\"Conference Room A\"}")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void log_savesAuditLogToRepository() {
        auditService.log("admin@company.com", AuditAction.ROOM_CREATED, "Room", "uuid-123",
                java.util.Map.of("roomName", "Conference Room A"));

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void log_withUnserializableMeta_usesEmptyJson() {
        auditService.log("admin@company.com", AuditAction.ROOM_CREATED, "Room", "uuid-123",
                new Object() {
                    @Override
                    public String toString() {
                        throw new RuntimeException("Cannot serialize");
                    }
                });

        verify(auditLogRepository).save(argThat(log -> "{}".equals(log.getMetaJson())));
    }

    @Test
    void getAllAuditLogs_returnsPagedResponse() {
        Page<AuditLog> page = new PageImpl<>(List.of(auditLog), PageRequest.of(0, 20), 1);
        when(auditLogRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(page);

        PagedResponse<AuditLogResponse> result = auditService.getAllAuditLogs(0, 20);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getActorEmail()).isEqualTo("admin@company.com");
        assertThat(result.getContent().get(0).getAction()).isEqualTo(AuditAction.ROOM_CREATED);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.isLast()).isTrue();
    }

    @Test
    void getAllAuditLogs_emptyPage_returnsEmptyResponse() {
        Page<AuditLog> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(auditLogRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class))).thenReturn(emptyPage);

        PagedResponse<AuditLogResponse> result = auditService.getAllAuditLogs(0, 20);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void getAuditLogsByEntity_returnsMatchingLogs() {
        when(auditLogRepository.findAllByEntityTypeAndEntityIdOrderByCreatedAtDesc("Room", "uuid-123"))
                .thenReturn(List.of(auditLog));

        List<AuditLogResponse> result = auditService.getAuditLogsByEntity("Room", "uuid-123");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEntityType()).isEqualTo("Room");
        assertThat(result.get(0).getEntityId()).isEqualTo("uuid-123");
    }

    @Test
    void getAuditLogsByEntity_noMatches_returnsEmptyList() {
        when(auditLogRepository.findAllByEntityTypeAndEntityIdOrderByCreatedAtDesc("Room", "nonexistent"))
                .thenReturn(List.of());

        List<AuditLogResponse> result = auditService.getAuditLogsByEntity("Room", "nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void getAuditLogsByActor_returnsMatchingLogs() {
        when(auditLogRepository.findAllByActorEmailOrderByCreatedAtDesc("admin@company.com"))
                .thenReturn(List.of(auditLog));

        List<AuditLogResponse> result = auditService.getAuditLogsByActor("admin@company.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getActorEmail()).isEqualTo("admin@company.com");
    }

    @Test
    void getAuditLogsByActor_noMatches_returnsEmptyList() {
        when(auditLogRepository.findAllByActorEmailOrderByCreatedAtDesc("nobody@company.com"))
                .thenReturn(List.of());

        List<AuditLogResponse> result = auditService.getAuditLogsByActor("nobody@company.com");

        assertThat(result).isEmpty();
    }
}
