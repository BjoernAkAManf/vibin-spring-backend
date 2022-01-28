package com.vibinofficial.backend;

import com.vibinofficial.backend.api.QueueJoinResult;
import com.vibinofficial.backend.api.RoomInfo;
import com.vibinofficial.backend.hasura.Hasura;
import com.vibinofficial.backend.twilio.VibinConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor
public class VibinController {

    private final VibinQueue queueService;
    private final VibinConfig config;
    private final Map<String, List<Consumer<String>>> events = new HashMap<>();
    private final Hasura hasuraService;

    @Scheduled(fixedDelay = 3, timeUnit = TimeUnit.SECONDS)
    public void queryRoomList() {
//        final var rooms = this.hasuraService
//                .queryRoomList()
//                .executeQuery(GraphQLQueries.ROOM_LIST)
//                .map(this::extractRooms)
//                .block();
//
//        logFoundRooms(rooms);
    }

    @Scheduled(fixedRate = 1000, timeUnit = TimeUnit.MILLISECONDS)
    public void createPolling() {
        if (!this.config.isEnabled()) {
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
        this.hasuraService.notifyMatch(user1, user2)
                .doOnSubscribe(v -> log.info("Dispatching Match {} with {}", user1, user2))
                .doOnError(ex -> log.error("Matching {} with {} failed", user1, user2, ex))
                .subscribe();
    }

    @PostMapping("/match/accept")
    // TODO: Type obv. wrong-ish
    public Mono<QueueJoinResult> matchAccept(final Principal principal) {
        final String user = principal.getName();
        log.info("Match Accepted: {}", user);
        return Mono.just(QueueJoinResult.SUCCESS);
    }

    @PostMapping("/match/decline")
    public Mono<QueueJoinResult> matchDecline(final Principal principal) {
        final String user = principal.getName();
        log.info("Match Declined: {}", user);
        return Mono.just(QueueJoinResult.SUCCESS);
    }

    @PostMapping("/queue/join")
    public Mono<QueueJoinResult> joinQueue(final Principal principal) {
        // TODO: IF user has current match, set that match to active = false
        final String user = principal.getName();
        log.info("joining queue POST: {}", user);
        // Add to Queue
        this.queueService.join(user);

        // TODO: Check Criteria list (e.g. Language; Interest, if applicable)
        // TODO: Check Blocklist
        // TODO: Check TMP Blocklist (Mismatches do not get rematched until a cool off period)

        return this.hasuraService.createInitialMatchEntry(user)
                .map(v -> QueueJoinResult.SUCCESS)
                .onErrorReturn(QueueJoinResult.ERROR);
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
