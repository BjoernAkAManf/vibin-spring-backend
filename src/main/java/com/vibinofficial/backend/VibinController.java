package com.vibinofficial.backend;

import com.vibinofficial.backend.api.AsyncAction;
import com.vibinofficial.backend.api.MatchInfo;
import com.vibinofficial.backend.api.RoomInfo;
import com.vibinofficial.backend.api.SyncAction;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor
public class VibinController {
    @PostMapping("/queue/join")
    @AsyncAction
    public MatchInfo joinQueue(final Principal principal) {
        final var uid = principal.getName();
        // Add to Queue
        // wait until we has partner
        // TODO: Check Criteria list (e.g. Language; Interest, if applicable)
        // TODO: Check Blocklist
        // TODO: Check TMP Blocklist (Mismatches do not get rematched until a cool off period)
        // Return Match
        return MatchInfo.builder()
                .matchUserId("")
                .matchToken("")
                .build();
    }

    @AsyncAction
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

    @SyncAction
    public void respondToMatch(final Principal principal, final String token, final boolean accept) {
        final var uid = principal.getName();
        // Server notifies waiting clients in syncRoom.
    }

    // TODO: If any party does not join a room within x minutes after a successful match, complete room automatically
    // TODO: If any party leaves a room permanently (in contrast to closing the window) complete room automatically
}
