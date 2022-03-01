package com.vibinofficial.backend.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClients;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.io.IOException;
import java.util.function.Supplier;

@RequiredArgsConstructor
public final class KeycloakExtendedClient {
    static final ObjectMapper MAPPER = new ObjectMapper();
    private final Keycloak client;
    final String baseUrl;

    public KeycloakUser createUser(final String realm, final String userName, final String password) {
        final var users = this.client.realm(realm).users();

        final var userRep = new UserRepresentation();
        userRep.setEnabled(true);
        userRep.setUsername(userName);
        userRep.setFirstName("U" + userName);
        userRep.setLastName("P" + password);
        userRep.setEmail(userName + "." + password + "@example.com");
        // After Request
        userRep.setId(this.create(users, userRep));

        final var pass = new CredentialRepresentation();
        pass.setTemporary(false);
        pass.setType(CredentialRepresentation.PASSWORD);
        pass.setValue(password);

        final var user = users.get(userRep.getId());
        user.resetPassword(pass);

        return new KeycloakUser(this, this.baseUrl, userRep.getId(), realm, userName, password);
    }

    CloseableHttpResponse sendRequest(final Supplier<HttpUriRequest> fn) throws IOException {
        try (final var client = HttpClients.createDefault()) {
            return client.execute(fn.get());
        }
    }

    private String create(final UsersResource users, final UserRepresentation user) {
        try (final var resp = users.create(user)) {
            return CreatedResponseUtil.getCreatedId(resp);
        }
    }
}
