package com.vibinofficial.backend.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RequiredArgsConstructor
public class KeycloakUser {
    private final KeycloakExtendedClient client;
    private final String baseUrl;
    @Getter
    private final String id;
    private final String realm;
    private final String username;
    private final String password;

    public Credentials login(final String clientId) throws IOException {
        try (final var resp = this.sendLoginRequest(clientId)) {
            final var m = resp.getEntity();
            return KeycloakExtendedClient.MAPPER.readValue(m.getContent(), Credentials.class);
        }
    }

    private CloseableHttpResponse sendLoginRequest(final String clientId) throws IOException {
        return this.client.sendRequest(() -> {
            final var req = new HttpPost(this.baseUrl + "/realms/" + realm + "/protocol/openid-connect/token");
            req.setEntity(new UrlEncodedFormEntity(List.of(
                new BasicNameValuePair("grant_type", "password"),
                new BasicNameValuePair("client_id", clientId),
//                new BasicNameValuePair("client_secret", ""),
                new BasicNameValuePair("username", username),
                new BasicNameValuePair("password", password)
            ), StandardCharsets.UTF_8));
            return req;
        });
    }
}
