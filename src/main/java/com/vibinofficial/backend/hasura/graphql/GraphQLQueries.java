package com.vibinofficial.backend.hasura.graphql;

public class GraphQLQueries {

    private GraphQLQueries() {
    }

    public static final String ROOM_LIST = "query " +
            "GetRoomList {" +
            "  room_list {" +
            "    user1" +
            "    user2" +
            "    room_id" +
            "    last_check" +
            "  }" +
            "}";

    public static final String MATCHES_READY = "query " +
            "GetMatchesReady {" +
            "  queue_matches_ready {" +
            "    user" +
            "    partner" +
            "  }" +
            "}";
}
