package com.vibinofficial.backend;

import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

final class DemoApplicationTests extends AbstractDBTest {
    @Inject
    private FriendService service;

    @Test
    void sendAndRequestWorks() {
        final var user = UUID.fromString("f27ec396-35b1-4111-a5c0-af66530997db");
        final var friend = UUID.fromString("f25bc70f-c31b-491d-af4c-a6a76eeb3d25");
        assertThat(this.service.deleteFriendship(user, friend)).isFalse();

        assertSimpleFriendRequestAcceptFlow(user, friend);
        assertThat(this.service.deleteFriendship(user, friend)).isTrue();

        assertSimpleFriendRequestAcceptFlow(friend, user);
    }

    private void assertSimpleFriendRequestAcceptFlow(final UUID user, final UUID friend) {
        assertNoFriends(user, friend);
        assertThat(this.service.acceptFriendRequest(friend, user))
                .as("User cannot accept friend request before a request was sent")
                .isFalse();
        assertNoFriends(user, friend);

        this.service.sendFriendRequest(user, friend);
        assertNoFriends(user, friend);

        assertThat(this.service.acceptFriendRequest(friend, user)).isTrue();
        assertThat(this.service.findFriendsOf(user)).containsExactly(friend);
        assertThat(this.service.findFriendsOf(friend)).containsExactly(user);
    }

    private void assertNoFriends(UUID... users) {
        for(final UUID user: users) {
            assertThat(this.service.findFriendsOf(user)).isEmpty();
        }
    }
}
