package com.vibinofficial.backend;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties("vibin.cors")
public class VibinCorsConfig {
    private final List<String> origins;
    private final List<String> methods;
    private boolean credentials;
    private final List<String> headers;
}
