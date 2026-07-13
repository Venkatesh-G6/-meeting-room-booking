package com.yourcompany.roombooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourcompany.roombooking.config.CorsConfig;
import com.yourcompany.roombooking.config.SecurityConfig;
import com.yourcompany.roombooking.dto.request.CreateRoomRequest;
import com.yourcompany.roombooking.dto.response.RoomResponse;
import com.yourcompany.roombooking.enums.RoomType;
import com.yourcompany.roombooking.exception.ResourceNotFoundException;
import com.yourcompany.roombooking.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomController.class)
@Import({SecurityConfig.class, CorsConfig.class})
@ActiveProfiles("test")
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomService roomService;

    @MockBean
    private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    private static org.springframework.test.web.servlet.request.RequestPostProcessor jwtWithRole(String role) {
        return jwt().jwt(j -> j
                        .claim("roles", java.util.List.of(role))
                        .claim("preferred_username", "test-" + role.toLowerCase() + "@company.com"))
                .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role));
    }

    private RoomResponse sampleRoomResponse(UUID id) {
        return RoomResponse.builder()
                .id(id)
                .roomName("Conference Room A")
                .roomType(RoomType.MEETING)
                .capacity(10)
                .location("Floor 2")
                .active(true)
                .build();
    }

    @Test
    void createRoom_withAdminRole_returns201() throws Exception {
        UUID roomId = UUID.randomUUID();
        CreateRoomRequest request = CreateRoomRequest.builder()
                .roomName("Conference Room A")
                .roomType(RoomType.MEETING)
                .capacity(10)
                .location("Floor 2")
                .build();
        when(roomService.createRoom(any(), anyString())).thenReturn(sampleRoomResponse(roomId));

        mockMvc.perform(post("/api/v1/rooms")
                        .with(jwtWithRole("ADMIN"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.roomName").value("Conference Room A"));
    }

    @Test
    void createRoom_withEmployeeRole_returns403() throws Exception {
        CreateRoomRequest request = CreateRoomRequest.builder()
                .roomName("Conference Room A")
                .roomType(RoomType.MEETING)
                .capacity(10)
                .build();

        mockMvc.perform(post("/api/v1/rooms")
                        .with(jwtWithRole("EMPLOYEE"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createRoom_withoutAuth_returns401() throws Exception {
        CreateRoomRequest request = CreateRoomRequest.builder()
                .roomName("Conference Room A")
                .roomType(RoomType.MEETING)
                .capacity(10)
                .build();

        mockMvc.perform(post("/api/v1/rooms")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createRoom_invalidRequest_returns400() throws Exception {
        CreateRoomRequest invalidRequest = CreateRoomRequest.builder()
                .roomName("")
                .build();

        mockMvc.perform(post("/api/v1/rooms")
                        .with(jwtWithRole("ADMIN"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllRooms_withEmployeeRole_returns200() throws Exception {
        when(roomService.getAllRooms(0, 10)).thenReturn(
                com.yourcompany.roombooking.dto.response.PagedResponse.<RoomResponse>builder()
                        .content(java.util.List.of(sampleRoomResponse(UUID.randomUUID())))
                        .pageNumber(0)
                        .pageSize(10)
                        .totalElements(1)
                        .totalPages(1)
                        .last(true)
                        .build());

        mockMvc.perform(get("/api/v1/rooms")
                        .with(jwtWithRole("EMPLOYEE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getRoomById_notFound_returns404() throws Exception {
        UUID roomId = UUID.randomUUID();
        when(roomService.getRoomById(roomId)).thenThrow(new ResourceNotFoundException("Room not found"));

        mockMvc.perform(get("/api/v1/rooms/" + roomId)
                        .with(jwtWithRole("EMPLOYEE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void disableRoom_withAdminRole_returns200() throws Exception {
        UUID roomId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/rooms/" + roomId + "/disable")
                        .with(jwtWithRole("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void disableRoom_withEmployeeRole_returns403() throws Exception {
        UUID roomId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/rooms/" + roomId + "/disable")
                        .with(jwtWithRole("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }
}
