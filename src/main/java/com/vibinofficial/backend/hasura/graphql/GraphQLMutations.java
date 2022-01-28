package com.vibinofficial.backend.hasura.graphql;

import com.vibinofficial.backend.QueueMatch;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GraphQLMutations {
    public static final String SET_ONLINE = "mutation " +
            "SetOnline {" +
            "  vibin_set_online { uid }" +
            "}";

    public static final String INSERT_INITIAL_USER_MATCH_ENTRY = "mutation " +
            "AddToQueueTable($user: uuid!) {" +
            "  insert_queue_matches(" +
            "    objects: {" +
            "      user: $user," +
            "      active: true," +
            "      accepted: false," +
            "      partner: null," +
            "      match_time: null" +
            "    }," +
            "    on_conflict: {" +
            "      constraint: queue_matches_pkey, update_columns: [accepted, active, partner, match_time]" +
            "    }) {" +
            "    affected_rows" +
            "  }" +
            "}";
    ;

    public static final String UPDATE_WITH_MATCH = "mutation " +
            "SetMatch($user1: uuid!, $user2: uuid!) { " +
            "  user1: update_queue_matches( " +
            "    where: { " +
            "      user: {_eq: $user1} " +
            "    },  " +
            "    _set: { " +
            "      partner: $user2, " +
            "      match_time: \"now()\", " +
            "      active: true, " +
            "      accepted: false " +
            "    } " +
            "  ) {affected_rows} " +
            " " +
            "  user2: update_queue_matches( " +
            "    where: { " +
            "      user: {_eq: $user2} " +
            "    },  " +
            "    _set: { " +
            "      partner: $user1, " +
            "      match_time: \"now()\", " +
            "      active: true, " +
            "      accepted: false " +
            "    } " +
            "  ) {affected_rows} " +
            "}";

    public static String updateUserMatch(QueueMatch match) {
        // TODO
        return null;
    }
}
