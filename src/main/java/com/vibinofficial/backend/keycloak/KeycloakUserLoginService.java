package com.vibinofficial.backend.keycloak;

import com.vibinofficial.backend.twilio.VibinConfig;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakUserLoginService {
    private final KeycloakSpringBootProperties server;
    private final KeycloakClientConfig client;
    private final ApplicationEventPublisher publisher;
    private final VibinConfig config;

    @Scheduled(fixedRate = 60, timeUnit = TimeUnit.SECONDS)
    public void updateToken() {
        if (this.config.isDisabled()) {
            return;
        }
        try {
            String baseUrl = String.format("%s/realms/%s", this.server.getAuthServerUrl(), this.server.getRealm());

            BodyInserters.FormInserter<String> body = BodyInserters
                    .fromFormData("username", this.client.getName())
                    .with("password", this.client.getPassword())
                    .with("grant_type", "password")
                    .with("client_id", this.server.getResource());
            // TODO: check if client is public or confidential (Requires different data)
            // &client_secret={{ CLIENTSECRET }}

            final var lastResponse = WebClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .build()
                    // Send Request
                    .post()
                    .uri("/protocol/openid-connect/token")
                    .body(body)
                    .retrieve()
                    .bodyToFlux(Response.class)
                    // TODO: Configure timeout
                    // TODO: Is this catched with WebClientResponseException
                    .blockLast(Duration.ofMinutes(2));

            if (lastResponse == null) {
                log.warn("Retrieving User Login failed: Null returned");
                return;
            }

            this.publisher.publishEvent(new KeycloakTokenUpdateEvent(lastResponse.access_token));
        } catch (final WebClientResponseException ex) {
            // We do not update lastResponse here, so we might keep the JWT alive.
            log.error("Retrieving User Login failed", ex);
        }
    }

    @Data
    public static class Response {
        private String access_token;
    }
}
