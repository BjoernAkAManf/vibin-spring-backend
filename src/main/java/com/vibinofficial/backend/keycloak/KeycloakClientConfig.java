package com.vibinofficial.backend.keycloak;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "vibin.backend.user")
public class KeycloakClientConfig {

    private String name;
    private String password;
}
