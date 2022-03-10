package com.vibinofficial.backend;

import com.vibinofficial.backend.twilio.VibinConfig;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@EnableScheduling
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
@KeycloakConfiguration
public class VibinBackend {

    public static void main(String[] args) {
        SpringApplication.run(VibinBackend.class, args);
    }

    @Bean
    public KeycloakConfigResolver keycloakConfigResolver(final VibinConfig config) {
        return new KeycloakSpringBootConfigResolver();
    }

    @Bean
    @ConditionalOnProperty(
        value = "logging.level.web",
        havingValue = "DEBUG"
    )
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        final var filter = new CommonsRequestLoggingFilter();
        filter.setIncludeClientInfo(true);
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setIncludeHeaders(false);
        return filter;
    }
}
