package com.vibinofficial.backend.hasura;

import com.netflix.graphql.dgs.client.GraphQLError;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public final class GraphQlExceptions extends RuntimeException {
    private final List<GraphQLError> errors;

    public GraphQlExceptions(final List<GraphQLError> errors) {
        super(createMessage(errors));
        this.errors = errors;
    }

    public static GraphQLResponse checkResult(GraphQLResponse response) {
        if (response.hasErrors()) {
            throw new GraphQlExceptions(response.getErrors());
        }
        return response;
    }

    private static String createMessage(final List<GraphQLError> errors) {
        if (errors.isEmpty()) {
            return "Programming error";
        }
        final int size = errors.size();
        if (size == 1) {
            final GraphQLError error = errors.get(0);
            return error.getMessage();
        }
        return "Multiple Errors occurred";
    }
}
