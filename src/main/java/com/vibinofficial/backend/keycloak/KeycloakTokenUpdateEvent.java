package com.vibinofficial.backend.keycloak;

import lombok.Data;

@Data
public final class KeycloakTokenUpdateEvent {
    private final String token;
}
