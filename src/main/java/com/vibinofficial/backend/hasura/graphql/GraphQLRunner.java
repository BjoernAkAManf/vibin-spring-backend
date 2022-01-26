package com.vibinofficial.backend.hasura.graphql;

import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;
import reactor.core.publisher.Mono;

public class GraphQLRunner {

    private GraphQLRunner() {
    }

    public static GraphQLResponse runQuery(String query, WebClientGraphQLClient client) {
        return client.reactiveExecuteQuery(query).block(); // TODO timeouts?
    }

    public static Mono<GraphQLResponse> runQueryAsync(String query, WebClientGraphQLClient client) {
        return client.reactiveExecuteQuery(query);
    }

    public static GraphQLResponse runMutation(String mutation, WebClientGraphQLClient client) {
        return client.reactiveExecuteQuery(mutation).block(); // TODO timeouts?
    }

    public static Mono<GraphQLResponse> runMutationAsync(String mutation, WebClientGraphQLClient client) {
        return client.reactiveExecuteQuery(mutation);
    }
}
