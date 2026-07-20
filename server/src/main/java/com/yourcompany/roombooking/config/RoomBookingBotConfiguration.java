package com.yourcompany.roombooking.config;

import com.microsoft.bot.builder.Bot;
import com.microsoft.bot.integration.spring.BotDependencyConfiguration;
import com.yourcompany.roombooking.bot.RoomBookingBotHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoomBookingBotConfiguration extends BotDependencyConfiguration {

    @Bean
    public Bot bot(RoomBookingBotHandler handler) {
        return handler;
    }
}
