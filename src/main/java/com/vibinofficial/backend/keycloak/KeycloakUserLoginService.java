package com.vibinofficial.backend.keycloak;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakUserLoginService {

    private final KeycloakSpringBootProperties server;
    private final KeycloakClientConfig client;
    private Response lastResponse;

    @Scheduled(fixedRate = 60, timeUnit = TimeUnit.SECONDS)
    public void updateToken() {
        try {
            String baseUrl = String.format("%s/realms/%s", this.server.getAuthServerUrl(), this.server.getRealm());

            BodyInserters.FormInserter<String> body = BodyInserters
                    .fromFormData("username", this.client.getName())
                    .with("password", this.client.getPassword())
                    .with("grant_type", "password")
                    .with("client_id", this.server.getResource());
            // TODO: check if client is public or confidential (Requires different data)
            // &client_secret={{ CLIENTSECRET }}

            this.lastResponse = WebClient.builder()
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
                    .blockLast(Duration.ofMinutes(2));
        } catch (final WebClientResponseException ex) {
            // We do not update lastResponse here, so we might keep the JWT alive.
            log.error("Retrieving User Login failed", ex);
        }
    }

    public String getAuthToken() throws IOException {
        if (this.lastResponse == null) {
            throw new IOException("Login was not successful. Retry again later.");
        }
        return this.lastResponse.access_token;
    }

    @Data
    public static class Response {
        private String access_token;
    }
}
