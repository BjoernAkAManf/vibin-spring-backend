package com.vibinofficial.backend;

import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.vibinofficial.backend.api.QueueJoinResult;
import com.vibinofficial.backend.api.RoomInfo;
import com.vibinofficial.backend.hasura.GraphQlExceptions;
import com.vibinofficial.backend.hasura.Hasura;
import com.vibinofficial.backend.hasura.HasuraBody;
import com.vibinofficial.backend.twilio.RoomGrants;
import com.vibinofficial.backend.twilio.VibinConfig;
import com.vibinofficial.backend.twilio.VideoService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor
public class VibinController {

    private final VibinQueue queueService;
    private final VideoService videoService;
    private final VibinConfig config;
    private final Map<String, List<Consumer<String>>> events = new HashMap<>();
    private final Hasura hasuraService;

    @Scheduled(fixedDelay = 3, timeUnit = TimeUnit.SECONDS)
    public void queryRoomList() {
//        final var rooms = this.hasuraService
//                .map(GraphQlExceptions::checkResult)
//                .queryRoomList()
//                .executeQuery(GraphQLQueries.ROOM_LIST)
//                .map(this::extractRooms)
//                .block();
//
//        logFoundRooms(rooms);
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
    public void createRooms() {
        this.hasuraService
                .queryMatchesReady()
                .flatMap(this::createRoom)
                .blockLast();
    }

    private Mono<GraphQLResponse> createRoom(QueueMatch match) {
        RoomGrants roomGrants = videoService.createRoomForMatch(match.getLexSmallerUser(), match.getLexGreaterUser());

        return this.hasuraService
                .insertRoomEntry(roomGrants)
                .map(GraphQlExceptions::checkResult)
                .map(response -> checkRoomResult(response, match))
                .doOnError(ex -> log.error("Error creating a room for {}/{}", match.getLexSmallerUser(), match.getLexGreaterUser(), ex));
        // TODO on error: cancel queue -> set active false and delete other entries
    }

    private GraphQLResponse checkRoomResult(GraphQLResponse response, QueueMatch match) {
        Integer affectedRows_match1 = response.extractValueAsObject("match1.affected_rows", Integer.class);
        Integer affectedRows_match2 = response.extractValueAsObject("match2.affected_rows", Integer.class);
        if (!Objects.equals(affectedRows_match1, 1) || !Objects.equals(affectedRows_match2, 1)) {
            String msg = String.format("Could not update room for match (%s, %s)", match.getLexSmallerUser(), match.getLexGreaterUser());
            throw new IllegalStateException(msg);
        }

        Integer affectedRows_auth1 = response.extractValueAsObject("room_auth1.affected_rows", Integer.class);
        Integer affectedRows_auth2 = response.extractValueAsObject("room_auth2.affected_rows", Integer.class);
        if (!Objects.equals(affectedRows_auth1, 1) || !Objects.equals(affectedRows_auth2, 1)) {
            String msg = String.format("Could not update room_auth for match (%s, %s)", match.getLexSmallerUser(), match.getLexGreaterUser());
            throw new IllegalStateException(msg);
        }

        Integer affectedRows_list = response.extractValueAsObject("room_list.affected_rows", Integer.class);
        if (!Objects.equals(affectedRows_list, 1)) {
            String msg = String.format("Could not update room_list for match (%s, %s)", match.getLexSmallerUser(), match.getLexGreaterUser());
            throw new IllegalStateException(msg);
        }

        // chat1 and chat2 do not need to be checked. if affected_rows == 0, the entry already existed

        return response;
    }

    @Scheduled(fixedRate = 1000, timeUnit = TimeUnit.MILLISECONDS)
    public void createPolling() {
        if (this.config.isDisabled()) {
            log.warn("Polling disabled!");
            return;
        }
        final Optional<QueueMatch> queueMatch = this.queueService.pollMatch();
        queueMatch.ifPresent(this::dispatchMatchEvent);
        // TODO fka insert into queue match table
    }

    private void dispatchMatchEvent(QueueMatch queueMatch) {
        final String user1 = queueMatch.getUser1();
        final String user2 = queueMatch.getUser2();

        // 		-> Update matches in Hasura (Spring -> Hasura)
        this.hasuraService.notifyMatch(queueMatch)
                .map(GraphQlExceptions::checkResult)
                .map(x -> checkMatchUpdate(x, user1, user2))
                .doOnSubscribe(v -> log.info("Dispatching Match {} with {}", user1, user2))
                .doOnError(ex -> log.error("Matching {} with {} failed", user1, user2, ex))
                .subscribe();
    }

    private GraphQLResponse checkMatchUpdate(GraphQLResponse response, String user1, String user2) {

        Integer affectedRows1 = response.extractValueAsObject("user1.affected_rows", Integer.class);
        Integer affectedRows2 = response.extractValueAsObject("user2.affected_rows", Integer.class);
        if (!Objects.equals(affectedRows1, 1) || !Objects.equals(affectedRows2, 1)) {
            String msg = String.format("Unknown error, could not update match for users %s, %s (%s, %s)",
                    user1, user2, affectedRows1, affectedRows2);
            throw new IllegalStateException(msg);
        }
        return response;
    }


    @PostMapping("/match/accept")
    // TODO: Type obv. wrong-ish
    public Mono<QueueJoinResult> matchAccept(final Principal principal) {
        final String user = principal.getName();
        log.info("Match Accepted: {}", user);

        return this.hasuraService.acceptMatch(user)
                .map(GraphQlExceptions::checkResult)
                .map(v -> checkAcceptResult(v, user))
                .doOnError(ex -> log.error("Accepting match ({} with partner {}) failed.", user, "TODO", ex))
                .onErrorReturn(QueueJoinResult.ERROR);
    }

    @Data
    static class MatchInput {
        // TODO do same for affected rows and other jsons
        private String partner;
    }

    @PostMapping("/match/decline")
    public Mono<QueueJoinResult> matchDecline(final Principal principal, @RequestBody final HasuraBody<MatchInput> body) {
        final String user = principal.getName();
        final String partner = body.getInput().getPartner();
        log.info("Match Declined: {} with {}", user, partner);

        return this.hasuraService.declineMatch(user, partner)
                .map(GraphQlExceptions::checkResult)
                .map(v -> this.checkDeclineResult(v, user, partner))
                .doOnError(ex -> log.error("Declining match ({} with partner {}) failed.", user, partner, ex))
                .onErrorReturn(QueueJoinResult.ERROR);
    }

    private QueueJoinResult checkAcceptResult(GraphQLResponse response, final String user) {
        Integer affectedRows = response.extractValueAsObject("update_queue_matches.affected_rows", Integer.class);
        if (!Objects.equals(affectedRows, 1)) {
            log.error("Superficial Accept ({}): {} could not accept.", affectedRows, user);
            return QueueJoinResult.ERROR;
        }
        return QueueJoinResult.SUCCESS;
    }

    private QueueJoinResult checkDeclineResult(GraphQLResponse response, String user, String match) {
        // REALLY THROW ERROR PLS
        if (response.hasErrors()) {
            log.error("Decline failed: {}", response.getErrors());
            return QueueJoinResult.ERROR;
        }

        Integer affectedRows = response.extractValueAsObject("self.affected_rows", Integer.class);
        if (!Objects.equals(affectedRows, 1)) {
            log.error("Superficial Decline ({}): {} has no such match: {}", affectedRows, user, match);
            return QueueJoinResult.ERROR;
        }
        return QueueJoinResult.SUCCESS;
    }

    @PostMapping("/queue/join")
    public Mono<QueueJoinResult> joinQueue(Principal principal) {
        // TODO: IF user has current match, set that match to active = false
        final String user = principal.getName();
        log.info("joining queue: {}", user);
        // TODO: Check Criteria list (e.g. Language; Interest, if applicable)
        // TODO: Check Blocklist
        // TODO: Check TMP Blocklist (Mismatches do not get rematched until a cool off period)

        return this.hasuraService.createInitialMatchEntry(user)
                .map(GraphQlExceptions::checkResult)
                .map(v -> checkQueueJoin(v, user))
                .doOnError(ex -> log.error("Error while storing queue join ({}).", user, ex))
                .onErrorReturn(QueueJoinResult.ERROR)
                .doOnSuccess((r) -> this.queueService.join(user));
    }

    private QueueJoinResult checkQueueJoin(GraphQLResponse response, String user) {
        Integer affectedRows = response.extractValueAsObject("insert_queue_matches.affected_rows", Integer.class);
        if (!Objects.equals(affectedRows, 1)) {
            String msg = String.format("Could not update queue_match for user %s (%s)", user, affectedRows);
            throw new IllegalStateException(msg);
        }
        return QueueJoinResult.SUCCESS;
    }

    @PostMapping("/queue/leave")
    public Mono<QueueJoinResult> leaveQueue(Principal principal) {
        final String user = principal.getName();
        log.info("leaving queue: {}", user);

        return this.hasuraService.deleteQueueEntry(user)
                .map(GraphQlExceptions::checkResult)
                .map(v -> checkDeleteResult(v, user))
                .doOnError(ex -> log.error("Leaving queue failed for user {}.", user, ex))
                .onErrorReturn(QueueJoinResult.ERROR)
                .doOnSubscribe((r) -> this.queueService.leave(user));
    }

    private QueueJoinResult checkDeleteResult(GraphQLResponse response, String user) {
        Integer affectedRows = response.extractValueAsObject("delete_queue_matches.affected_rows", Integer.class);
        if (!Objects.equals(affectedRows, 1)) {
            log.error("Superficial delete ({}): {} has no such queue entry.", affectedRows, user);
            return QueueJoinResult.ERROR;
        }
        return QueueJoinResult.SUCCESS;
    }

    public RoomInfo syncMatch(final Principal principal) {
        // IF you already accepted
        //    await Partner response
        //    accepted: create room (for both match participants)
        //    declined: return cancelled (for both match participants)

        // IF you already declined
        //    return cancelled (for both match participants)

        // If no decision and your partner decides
        //      declined: return cancelled (for both match)
        //      accepted: await your decision

        // Otherwise: await any response
        // TODO: Also at most wait x minutes otherwise decline automatically
        return RoomInfo.builder()
                .success(false)
                .token(principal.getName())
                .build();
    }

    public void respondToMatch(final Principal principal, final String token, final boolean accept) {
        final String uid = principal.getName();
        // Server notifies waiting clients in syncRoom.
    }
    // TODO: If any party does not join a room within x minutes after a successful match, complete room automatically
    // TODO: If any party leaves a room permanently (in contrast to closing the window) complete room automatically
}
