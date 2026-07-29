package com.yourcompany.roombooking.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RateLimitFilterTest {

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter();
    }

    @Test
    void nonApiRequests_passThroughWithoutRateLimit() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui.html");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void apiRequestWithinLimit_passesThrough() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/rooms");
        request.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void apiRequestExceedingLimit_returns429() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/rooms");
        request.setRemoteAddr("10.0.0.1");
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 30; i++) {
            filter.doFilterInternal(request, new MockHttpServletResponse(), chain);
        }

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(response.getContentAsString()).contains("Too many requests");
        verify(chain, times(30)).doFilter(any(), any());
    }

    @Test
    void differentClientsHaveSeparateBuckets() throws ServletException, IOException {
        MockHttpServletRequest req1 = new MockHttpServletRequest("GET", "/api/v1/rooms");
        req1.setRemoteAddr("192.168.1.1");
        MockHttpServletRequest req2 = new MockHttpServletRequest("GET", "/api/v1/rooms");
        req2.setRemoteAddr("192.168.1.2");
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 30; i++) {
            filter.doFilterInternal(req1, new MockHttpServletResponse(), chain);
        }

        MockHttpServletResponse response2 = new MockHttpServletResponse();
        filter.doFilterInternal(req2, response2, chain);

        assertThat(response2.getStatus()).isEqualTo(200);
        verify(chain, times(31)).doFilter(any(), any());
    }

    @Test
    void usesXForwardedForHeaderWhenPresent() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/rooms");
        request.addHeader("X-Forwarded-For", "203.0.113.5, 70.41.3.18");
        request.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void xForwardedForUsesFirstIpForRateLimiting() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/rooms");
        request.addHeader("X-Forwarded-For", "203.0.113.5");
        request.setRemoteAddr("192.168.1.1");
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 30; i++) {
            filter.doFilterInternal(request, new MockHttpServletResponse(), chain);
        }

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(429);
    }
}
