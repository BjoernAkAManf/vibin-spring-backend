package com.vibinofficial.backend.hasura;

import com.jayway.jsonpath.TypeRef;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.WebClientGraphQLClient;
import com.vibinofficial.backend.hasura.graphql.GraphQLQueries;
import com.vibinofficial.backend.keycloak.KeycloakUserLoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class Hasura {
    private final HasuraConfig config;
    private final KeycloakUserLoginService keycloak;

    @Scheduled(fixedDelay = 3, timeUnit = TimeUnit.SECONDS)
    public void queryRoomList() {
        //Configure a WebClient for your needs, e.g. including authentication headers and TLS.
        WebClientGraphQLClient client = createClient();

        //The GraphQLResponse contains data and errors.
        Mono<GraphQLResponse> responseMono = client.reactiveExecuteQuery(GraphQLQueries.ROOM_LIST);

        //GraphQLResponse has convenience methods to extract fields using JsonPath.
        List<Room> rooms = responseMono.map(this::extractRooms).block();

        logFoundRooms(rooms);
    }

    @NonNull
    private WebClientGraphQLClient createClient() {
        return new WebClientGraphQLClient(
                WebClient.create(this.config.getHost()),
                headers -> {
                    try {
                        headers.add("Authorization", "Bearer " + this.keycloak.getAuthToken());
                        headers.add("X-Hasura-Role", "backend");
                    } catch (final IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
        );
    }

    private List<Room> extractRooms(GraphQLResponse r) {
        if (r.hasErrors()) {
            throw new GraphQlExceptions(r.getErrors());
        }
        return r.extractValueAsObject("room_list", new TypeRef<>() {
        });
    }

    private void logFoundRooms(List<Room> rooms) {
        if (rooms == null) {
            log.warn("Received null for rooms!");
        } else if (rooms.isEmpty()) {
            log.info("No rooms found.");
        } else {
            String roomsString = rooms.toString().replace("),", "),\n\t\t");
            log.info("Found {} room(s):\n\t\t{}", rooms.size(), roomsString);
        }
    }
}
