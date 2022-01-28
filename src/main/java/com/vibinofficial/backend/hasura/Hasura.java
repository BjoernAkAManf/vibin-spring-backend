package com.vibinofficial.backend.hasura;

import com.jayway.jsonpath.TypeRef;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.vibinofficial.backend.hasura.graphql.GraphQLMutations;
import com.vibinofficial.backend.hasura.graphql.GraphQLQueries;
import com.vibinofficial.backend.hasura.graphql.QClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class Hasura {
    private final QClient client;

    public void queryRoomList() {
        final var rooms = this.client
                .executeQuery(GraphQLQueries.ROOM_LIST)
                .map(this::extractRooms)
                .block();

        logFoundRooms(rooms);
    }

    public Mono<GraphQLResponse> createInitialMatchEntry(String user) {
        return this.client
                .executeMutation(GraphQLMutations.INSERT_INITIAL_USER_MATCH_ENTRY, Map.of("user", user));
    }

    public Mono<GraphQLResponse> notifyMatch(String user1, String user2) {
        // TODO: assert both users have been updated (once)
        return this.client
                .executeMutation(GraphQLMutations.UPDATE_WITH_MATCH, Map.of(
                        "user1", user1,
                        "user2", user2
                ));
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
