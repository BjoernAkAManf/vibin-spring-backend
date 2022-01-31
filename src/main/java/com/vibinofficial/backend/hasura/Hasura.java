package com.vibinofficial.backend.hasura;

import com.jayway.jsonpath.TypeRef;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.vibinofficial.backend.QueueMatch;
import com.vibinofficial.backend.hasura.graphql.GraphQLMutations;
import com.vibinofficial.backend.hasura.graphql.GraphQLQueries;
import com.vibinofficial.backend.hasura.graphql.QClient;
import com.vibinofficial.backend.twilio.RoomGrants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public Mono<List<QueueMatch>> queryMatchesReady() {
        return this.client
                .executeQuery(GraphQLQueries.MATCHES_READY)
                .map(this::extractMatchesReady);
    }

    public Mono<GraphQLResponse> createInitialMatchEntry(String user) {
        return this.client
                .executeMutation(GraphQLMutations.INSERT_INITIAL_USER_MATCH_ENTRY, Map.of("user", user));
    }

    public Mono<GraphQLResponse> deleteQueueEntry(String user) {
        return this.client.executeMutation(GraphQLMutations.DELETE_QUEUE_ENTRY, Map.of("user", user));
    }

    public Mono<GraphQLResponse> notifyMatch(QueueMatch queueMatch) {
        String user1 = queueMatch.getUser1();
        String user2 = queueMatch.getUser2();

        // TODO: assert both users have been updated (once)
        return this.client
                .executeMutation(GraphQLMutations.UPDATE_WITH_MATCH, Map.of(
                        "user1", user1,
                        "user2", user2
                ));
    }

    public Mono<GraphQLResponse> acceptMatch(String acceptingUser) {
        return this.client.executeMutation(
                GraphQLMutations.ACCEPT_MATCH,
                Map.of("acceptingUser", acceptingUser)
        );
    }

    public Mono<GraphQLResponse> declineMatch(String decliningUser, String partnerUser) {
        return this.client.executeMutation(
                GraphQLMutations.DECLINE_MATCH,
                Map.of("decliningUser", decliningUser, "partnerUser", partnerUser)
        );
    }

    private <T> T extractData(GraphQLResponse response, String value, TypeRef<T> typeRef) {
        if (response.hasErrors()) {
            throw new GraphQlExceptions(response.getErrors());
        }

        return response.extractValueAsObject(value, typeRef);
    }

    private List<Room> extractRooms(GraphQLResponse r) {
        return this.extractData(r, "room_list", new TypeRef<>() {
        });
    }

    private List<QueueMatch> extractMatchesReady(GraphQLResponse response) {
        List<Map<String, String>> qm = this.extractData(response, "queue_matches_ready", new TypeRef<>() {
        });
        return qm.stream().map(x -> new QueueMatch(x.get("user"), x.get("partner"))).collect(Collectors.toList());
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

    public Mono<GraphQLResponse> insertRoomEntry(RoomGrants roomGrants) {
        return this.client.executeMutation(
                GraphQLMutations.SET_ROOM_INFO,
                Map.of("user1", roomGrants.getUser1(),
                        "user2", roomGrants.getUser2(),
                        "room", roomGrants.getRoomSid(),
                        "$room_grant1", roomGrants.getGrantUser1(),
                        "$room_grant2", roomGrants.getGrantUser2()));
        // todo insert into queuematches: user=user1, partner=user2, room=roomId
        // todo insert into queuematches: user=user2, partner=user1, room=roomId
        // todo insert rooms: user1=user1, user2=user2, room=roomId, grant1=grant1, grant2=grant2
    }
}
