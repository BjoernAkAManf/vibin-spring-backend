package com.vibinofficial.backend.rest;

import com.vibinofficial.backend.FriendService;
import lombok.RequiredArgsConstructor;
import org.keycloak.KeycloakPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;
import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping(path = Constants.FRIEND_ROUTE_PREFIX)
@RequiredArgsConstructor
public class FriendResource {

    private final FriendService friendService;

    @GetMapping("/state/{id}")
//    @PreAuthorize("isAuthenticated()")
    public int getState(@PathVariable int id, final Principal principal) {
        principal.getName();
//        String curUuid = principal.getName();
//        UUID uuid = UUID.fromString(curUuid);
//        friendService.findFriendsOf(uuid).contains(id);

        return 5;
    }

    @RequestMapping(value = "/username", method = RequestMethod.GET)
    @RolesAllowed("test")
    public Optional<String> currentUserName(@AuthenticationPrincipal final KeycloakPrincipal<?> principal) {
        return Optional.ofNullable(principal).map(Principal::getName);
    }
}
