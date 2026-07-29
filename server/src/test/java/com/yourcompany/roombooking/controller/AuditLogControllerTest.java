package com.yourcompany.roombooking.controller;

import com.yourcompany.roombooking.audit.AuditService;
import com.yourcompany.roombooking.config.CorsConfig;
import com.yourcompany.roombooking.config.SecurityConfig;
import com.yourcompany.roombooking.dto.response.AuditLogResponse;
import com.yourcompany.roombooking.dto.response.PagedResponse;
import com.yourcompany.roombooking.enums.AuditAction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditLogController.class)
@Import({SecurityConfig.class, CorsConfig.class})
@ActiveProfiles("test")
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditService auditService;

    @MockBean
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    private static org.springframework.test.web.servlet.request.RequestPostProcessor jwtWithRole(String role) {
        return jwt().jwt(j -> j
                        .claim("roles", java.util.List.of(role))
                        .claim("preferred_username", "test-" + role.toLowerCase() + "@company.com"))
                .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role));
    }

    private AuditLogResponse sampleAuditLogResponse() {
        return AuditLogResponse.builder()
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
    void getAllAuditLogs_withAuth_returns200() throws Exception {
        when(auditService.getAllAuditLogs(anyInt(), anyInt())).thenReturn(
                PagedResponse.<AuditLogResponse>builder()
                        .content(List.of(sampleAuditLogResponse()))
                        .pageNumber(0)
                        .pageSize(20)
                        .totalElements(1)
                        .totalPages(1)
                        .last(true)
                        .build());

        mockMvc.perform(get("/api/v1/audit-logs")
                        .with(jwtWithRole("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].actorEmail").value("admin@company.com"))
                .andExpect(jsonPath("$.data.content[0].action").value("ROOM_CREATED"));
    }

    @Test
    void getAllAuditLogs_withCustomPaging_returns200() throws Exception {
        when(auditService.getAllAuditLogs(1, 5)).thenReturn(
                PagedResponse.<AuditLogResponse>builder()
                        .content(List.of())
                        .pageNumber(1)
                        .pageSize(5)
                        .totalElements(0)
                        .totalPages(0)
                        .last(true)
                        .build());

        mockMvc.perform(get("/api/v1/audit-logs")
                        .param("page", "1")
                        .param("size", "5")
                        .with(jwtWithRole("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pageNumber").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(5));
    }

    @Test
    void getAllAuditLogs_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAuditLogsByEntity_withAuth_returns200() throws Exception {
        when(auditService.getAuditLogsByEntity(anyString(), anyString()))
                .thenReturn(List.of(sampleAuditLogResponse()));

        mockMvc.perform(get("/api/v1/audit-logs/entity/Room/uuid-123")
                        .with(jwtWithRole("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].entityType").value("Room"))
                .andExpect(jsonPath("$.data[0].entityId").value("uuid-123"));
    }

    @Test
    void getAuditLogsByEntity_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs/entity/Room/uuid-123"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAuditLogsByActor_withAuth_returns200() throws Exception {
        when(auditService.getAuditLogsByActor(anyString()))
                .thenReturn(List.of(sampleAuditLogResponse()));

        mockMvc.perform(get("/api/v1/audit-logs/actor/admin@company.com")
                        .with(jwtWithRole("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].actorEmail").value("admin@company.com"));
    }

    @Test
    void getAuditLogsByActor_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/audit-logs/actor/admin@company.com"))
                .andExpect(status().isUnauthorized());
    }
}
