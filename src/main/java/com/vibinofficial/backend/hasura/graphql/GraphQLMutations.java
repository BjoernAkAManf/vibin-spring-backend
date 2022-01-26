package com.vibinofficial.backend.hasura.graphql;

import java.util.Arrays;
import java.util.stream.Collectors;

public class GraphQLMutations {

    private GraphQLMutations() {
    }

    public static final String SET_ONLINE = "mutation " +
            "SetOnline {" +
            "  vibin_set_online { uid }" +
            "}";

    public static String insertInitialUserMatch(String... uuids) {
        if (uuids.length == 0) {
            throw new IllegalArgumentException();
        }

        String args = Arrays.stream(uuids).map(GraphQLMutations::createInitialUserMatch).collect(Collectors.joining(","));
        return ADD_TO_QUEUE_TABLE_START + args + ADD_TO_QUEUE_TABLE_END;
    }

    private static String createInitialUserMatch(String uid) {
        return "{" +
                "   user:          \"" + uid + "\"," +
                "   active:        true," +
                "   accepted:      false," +
                "   partner:       null," +
                "   match_time:    null" +
                "}";
    }

    private static final String ADD_TO_QUEUE_TABLE_START = "mutation " +
            "AddToQueueTable {" +
            "insert_queue_matches(" +
            "    objects: [";

    private static final String ADD_TO_QUEUE_TABLE_END = "    ], " +
            "    on_conflict: {" +
            "      constraint: queue_matches_pkey, update_columns: [accepted, active, partner, match_time]" +
            "    }) {" +
            "    affected_rows" +
            "  }" +
            "}";


}
