package com.yourcompany.roombooking;

import com.yourcompany.roombooking.config.BotProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(BotProperties.class)
@EnableScheduling
public class RoombookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoombookingApplication.class, args);
    }
}
