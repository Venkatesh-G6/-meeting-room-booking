package com.yourcompany.roombooking.controller;

import com.microsoft.bot.builder.Bot;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.schema.Activity;
import com.yourcompany.roombooking.config.BotProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BotControllerTest {

    @Mock
    private Bot bot;

    @Mock
    private BotProperties botProperties;

    @Mock
    private BotFrameworkHttpAdapter adapter;

    @Mock
    private Environment environment;

    private BotController botController;

    @BeforeEach
    void setUp() {
        botController = new BotController(bot, botProperties, adapter, environment);
    }

    @Test
    void messages_inDevMode_returnsOk() throws Exception {
        when(environment.matchesProfiles("dev")).thenReturn(true);
        when(botProperties.getName()).thenReturn("dev-bot");
        when(adapter.processIncomingActivity(anyString(), any(Activity.class), any(Bot.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        Activity activity = Activity.createMessageActivity();
        activity.setText("hello");
        MockHttpServletRequest request = new MockHttpServletRequest();

        CompletableFuture<org.springframework.http.ResponseEntity<Void>> result =
                botController.messages(activity, request);

        assertThat(result.get().getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void messages_inProdModeWithoutAuthHeader_returns401() throws Exception {
        when(environment.matchesProfiles("dev")).thenReturn(false);
        when(botProperties.getName()).thenReturn("prod-bot");

        Activity activity = Activity.createMessageActivity();
        activity.setText("hello");
        MockHttpServletRequest request = new MockHttpServletRequest();

        CompletableFuture<org.springframework.http.ResponseEntity<Void>> result =
                botController.messages(activity, request);

        assertThat(result.get().getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void messages_inProdModeWithAuthHeader_returnsOk() throws Exception {
        when(environment.matchesProfiles("dev")).thenReturn(false);
        when(botProperties.getName()).thenReturn("prod-bot");
        when(adapter.processIncomingActivity(anyString(), any(Activity.class), any(Bot.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        Activity activity = Activity.createMessageActivity();
        activity.setText("hello");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer test-token");

        CompletableFuture<org.springframework.http.ResponseEntity<Void>> result =
                botController.messages(activity, request);

        assertThat(result.get().getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void messages_inDevModeWithEmptyAuthHeader_returnsOk() throws Exception {
        when(environment.matchesProfiles("dev")).thenReturn(true);
        when(botProperties.getName()).thenReturn("dev-bot");
        when(adapter.processIncomingActivity(anyString(), any(Activity.class), any(Bot.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        Activity activity = Activity.createMessageActivity();
        activity.setText("check availability");
        MockHttpServletRequest request = new MockHttpServletRequest();

        CompletableFuture<org.springframework.http.ResponseEntity<Void>> result =
                botController.messages(activity, request);

        assertThat(result.get().getStatusCode().is2xxSuccessful()).isTrue();
    }
}
