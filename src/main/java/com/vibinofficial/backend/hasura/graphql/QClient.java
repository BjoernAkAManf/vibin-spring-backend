package com.vibinofficial.backend.hasura.graphql;

import com.netflix.graphql.dgs.client.GraphQLResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface QClient {
    /**
     * Needs more documentation.
     *
     * @see com.netflix.graphql.dgs.client.WebClientGraphQLClient#reactiveExecuteQuery(String)
     */
    Mono<GraphQLResponse> executeQuery(String query);

    /**
     * Needs more documentation.
     *
     * @see com.netflix.graphql.dgs.client.WebClientGraphQLClient#reactiveExecuteQuery(String, Map)
     */
    Mono<GraphQLResponse> executeQuery(String query, Map<String, Object> variables);

    /**
     * Needs more documentation.
     *
     * @see com.netflix.graphql.dgs.client.WebClientGraphQLClient#reactiveExecuteQuery(String)
     */
    Mono<GraphQLResponse> executeMutation(String mutation);

    /**
     * Needs more documentation.
     *
     * @see com.netflix.graphql.dgs.client.WebClientGraphQLClient#reactiveExecuteQuery(String, Map)
     */
    Mono<GraphQLResponse> executeMutation(String query, Map<String, Object> variables);
}
