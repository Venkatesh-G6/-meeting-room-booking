package com.yourcompany.roombooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourcompany.roombooking.config.CorsConfig;
import com.yourcompany.roombooking.config.SecurityConfig;
import com.yourcompany.roombooking.dto.request.CreateBookingRequest;
import com.yourcompany.roombooking.dto.response.BookingResponse;
import com.yourcompany.roombooking.enums.BookingStatus;
import com.yourcompany.roombooking.exception.BookingException;
import com.yourcompany.roombooking.exception.ResourceNotFoundException;
import com.yourcompany.roombooking.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@Import({SecurityConfig.class, CorsConfig.class})
@ActiveProfiles("test")
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private static RequestPostProcessor jwtWithRole(String role) {
        return jwt().jwt(j -> j
                        .claim("roles", List.of(role))
                        .claim("preferred_username", "test-" + role.toLowerCase() + "@company.com"))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private BookingResponse sampleBookingResponse(UUID id) {
        return BookingResponse.builder()
                .id(id)
                .roomId(UUID.randomUUID())
                .roomName("Conference Room A")
                .bookedBy("test-employee@company.com")
                .title("Team Sync")
                .attendeeCount(5)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .status(BookingStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createBooking_withEmployeeRole_returns201() throws Exception {
        UUID bookingId = UUID.randomUUID();
        CreateBookingRequest request = CreateBookingRequest.builder()
                .roomId(UUID.randomUUID())
                .title("Team Sync")
                .attendeeCount(5)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .build();
        when(bookingService.createBooking(any(), any())).thenReturn(sampleBookingResponse(bookingId));

        mockMvc.perform(post("/api/v1/bookings")
                        .with(jwtWithRole("EMPLOYEE"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createBooking_withoutAuth_returns401() throws Exception {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .roomId(UUID.randomUUID())
                .attendeeCount(5)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .build();

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createBooking_invalidRequest_returns400() throws Exception {
        CreateBookingRequest invalidRequest = CreateBookingRequest.builder().build();

        mockMvc.perform(post("/api/v1/bookings")
                        .with(jwtWithRole("EMPLOYEE"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_serviceThrowsBookingException_returns400() throws Exception {
        CreateBookingRequest request = CreateBookingRequest.builder()
                .roomId(UUID.randomUUID())
                .attendeeCount(5)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .build();
        when(bookingService.createBooking(any(), any()))
                .thenThrow(new BookingException("Room is already booked for the selected time slot"));

        mockMvc.perform(post("/api/v1/bookings")
                        .with(jwtWithRole("EMPLOYEE"))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getAllBookings_withAdminRole_returns200() throws Exception {
        when(bookingService.getAllBookings(0, 10)).thenReturn(
                com.yourcompany.roombooking.dto.response.PagedResponse.<BookingResponse>builder()
                        .content(List.of(sampleBookingResponse(UUID.randomUUID())))
                        .pageNumber(0)
                        .pageSize(10)
                        .totalElements(1)
                        .totalPages(1)
                        .last(true)
                        .build());

        mockMvc.perform(get("/api/v1/bookings")
                        .with(jwtWithRole("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getAllBookings_withEmployeeRole_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/bookings")
                        .with(jwtWithRole("EMPLOYEE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void cancelBooking_success_returns200() throws Exception {
        UUID bookingId = UUID.randomUUID();
        doNothing().when(bookingService).cancelBooking(eq(bookingId), any());

        mockMvc.perform(delete("/api/v1/bookings/" + bookingId)
                        .with(jwtWithRole("EMPLOYEE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void cancelBooking_notFound_returns404() throws Exception {
        UUID bookingId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Booking not found"))
                .when(bookingService).cancelBooking(eq(bookingId), any());

        mockMvc.perform(delete("/api/v1/bookings/" + bookingId)
                        .with(jwtWithRole("EMPLOYEE")))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelBooking_unauthorized_returns400() throws Exception {
        UUID bookingId = UUID.randomUUID();
        doThrow(new BookingException("You are not authorized to cancel this booking"))
                .when(bookingService).cancelBooking(eq(bookingId), any());

        mockMvc.perform(delete("/api/v1/bookings/" + bookingId)
                        .with(jwtWithRole("EMPLOYEE")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
