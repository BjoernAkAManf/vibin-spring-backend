package com.vibinofficial.backend.hasura.graphql;

import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;
import com.vibinofficial.backend.hasura.HasuraConfig;
import com.vibinofficial.backend.keycloak.KeycloakTokenUpdateEvent;
import lombok.Data;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Data
@Service
public final class QStuff implements QClient {
    private final HasuraConfig config;
    private String token;

    @EventListener
    public void onTokenUpdate(final KeycloakTokenUpdateEvent ev) {
        this.token = ev.getToken();
    }

    @NonNull
    private WebClientGraphQLClient createClient() {
        if (this.token == null) {
            // TODO: Handle this better: This should return a mono that resolves once a token has successfully been retrived
            // TODO: Should any auth error occur during execution, invalidate token
            throw new IllegalStateException("Successful Login has not (yet) occurred!");
        }
        return new WebClientGraphQLClient(
                WebClient.create(this.config.getHost()),
                headers -> {
                    headers.add("Authorization", "Bearer " + this.token);
                    headers.add("X-Hasura-Role", "backend");
                }
        );
    }

    @Override
    public Mono<GraphQLResponse> executeQuery(final String query) {
        return this.createClient().reactiveExecuteQuery(query);
    }

    @Override
    public Mono<GraphQLResponse> executeQuery(String query, Map<String, Object> variables) {
        return this.createClient().reactiveExecuteQuery(query, variables);
    }

    @Override
    public Mono<GraphQLResponse> executeMutation(String mutation) {
        // This client does not differentiate between mutations or queries
        return this.executeQuery(mutation);
    }

    @Override
    public Mono<GraphQLResponse> executeMutation(String query, Map<String, Object> variables) {
        // This client does not differentiate between mutations or queries
        return this.executeQuery(query, variables);
    }
}
