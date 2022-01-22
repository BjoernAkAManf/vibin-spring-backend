package com.vibinofficial.backend.twilio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("vibin")
public class VibinConfig {

    private boolean enabled;
    private String token;
    private String key;
    private String account;
}
