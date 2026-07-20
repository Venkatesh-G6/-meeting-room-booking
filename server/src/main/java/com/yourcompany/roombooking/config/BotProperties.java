package com.yourcompany.roombooking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bot")
@Data
public class BotProperties {
    private String appId;
    private String appPassword;
    private String name;
}
