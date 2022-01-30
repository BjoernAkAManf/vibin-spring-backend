package com.vibinofficial.backend.hasura.graphql;

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
            "      accepted: null," +
            "      partner: null," +
            "      match_time: null," +
            "      room: null" +
            "    }," +
            "    on_conflict: {" +
            "      constraint: queue_matches_pkey, update_columns: [accepted, active, partner, match_time, room]" +
            "    }) {" +
            "    affected_rows" +
            "  }" +
            "}";

    public static final String UPDATE_WITH_MATCH = "mutation " +
            "SetMatch($user1: uuid!, $user2: uuid!) {" +
            "  user1: update_queue_matches(" +
            "    where: {" +
            "      user: {_eq: $user1}" +
            "    }," +
            "    _set: {" +
            "      partner: $user2," +
            "      match_time: \"now()\"," +
            "      active: true," +
            "      accepted: null" +
            "    }" +
            "  ) {affected_rows}" +
            "  user2: update_queue_matches(" +
            "    where: {" +
            "      user: {_eq: $user2}" +
            "    }," +
            "    _set: {" +
            "      partner: $user1," +
            "      match_time: \"now()\"," +
            "      active: true," +
            "      accepted: null" +
            "    }" +
            "  ) {affected_rows}" +
            "}";

    public static final String ACCEPT_MATCH = "mutation " +
            "AcceptMatch($acceptingUser: uuid!) {" +
            "  update_queue_matches(" +
            "    where: { _and: [" +
            "      { user: {_eq: $acceptingUser} }," +
           "       { partner: {_is_null: false} }," +
           "       { accepted: {_is_null: true} }" +
            "    ]}, " +
            "    _set: {accepted: true}" +
            "  ) {" +
            "    affected_rows" +
            "  }" +
            "}";

    public static final String DECLINE_MATCH = "mutation " +
            "DeclineMatch($decliningUser: uuid!, $partnerUser: uuid!) {" +
            "  self: update_queue_matches(" +
            "    where: { _and: [" +
            "        { user: {_eq: $decliningUser} }," +
            "        { partner: {_eq: $partnerUser} }" +
            "    ]}," +
            "    _set: { accepted: false, active: false }" +
            "  ) {" +
            "    affected_rows" +
            "  }" +
            "  partner: update_queue_matches(" +
            "    _set: { active: false }," +
            "    where: {" +
            "      _and: [" +
            "        { user: {_eq: $partnerUser} }," +
            "        { partner: {_eq: $decliningUser} }" +
            "      ]" +
            "    }" +
            "  ) {" +
            "    affected_rows" +
            "  }" +
            "}";

    public static final String DELETE_QUEUE_ENTRY = "mutation " +
            "DeleteQueueEntry($user: uuid!) {" +
            "  delete_queue_matches(where: {user: {_eq: $user}}) {" +
            "    affected_rows" +
            "  }" +
            "}";
}
