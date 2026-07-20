package com.yourcompany.roombooking.controller;

import com.microsoft.bot.builder.Bot;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.schema.Activity;
import com.yourcompany.roombooking.config.BotProperties;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

/*
 * This endpoint receives ALL messages
 * from Microsoft Teams via Azure Bot Service.
 * In dev mode: called by simulation UI.
 * In prod mode: called by Teams via ngrok/URL.
 * Bot App ID and Password authenticate
 * the incoming requests.
 */
@RestController
@RequestMapping("/api/messages")
@Tag(name = "Teams Bot")
public class BotController {

    private static final Logger log = LoggerFactory.getLogger(BotController.class);

    private final Bot bot;
    private final BotProperties botProperties;
    private final BotFrameworkHttpAdapter adapter;
    private final Environment environment;

    @Autowired
    public BotController(Bot bot,
                         BotProperties botProperties,
                         BotFrameworkHttpAdapter adapter,
                         Environment environment) {
        this.bot = bot;
        this.botProperties = botProperties;
        this.adapter = adapter;
        this.environment = environment;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<Void>> messages(@RequestBody Activity activity,
                                                            HttpServletRequest request) {
        log.info("Incoming activity type: {} from bot: {}", activity.getType(), botProperties.getName());

        boolean isDev = environment.matchesProfiles("dev");

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null) {
            authHeader = "";
        }

        if (!isDev && authHeader.isBlank()) {
            log.warn("Missing Authorization header in prod mode");
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        return adapter.processIncomingActivity(authHeader, activity, bot)
                .thenApply(result -> ResponseEntity.ok().build());
    }
}
