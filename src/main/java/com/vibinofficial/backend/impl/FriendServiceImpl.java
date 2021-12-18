package com.vibinofficial.backend.impl;

import com.vibinofficial.backend.FriendService;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

@Mapper
public interface FriendServiceImpl extends FriendService {
    @Override
    List<UUID> findFriendsOf(UUID user);

    @Override
    void sendFriendRequest(UUID user, UUID friend);

    @Override
    @Transactional
    default boolean acceptFriendRequest(final UUID friend, final UUID requester) {
        final var count = this.deleteFriendRequest(friend, requester);
        if (count == 0) {
            // User has no friend request sent
            return false;
        }
        this.insertFriendRelationship(friend, requester);
        return true;
    }

    @Override
    default boolean deleteFriendship(UUID user, UUID friend) {
        final var count = this.sortedCall(user, friend, this::deleteFriendshipImpl);
        return count != 0;
    }

    @Override
    void ignoreFriendRequest(UUID friend, UUID requester);

    @Override
    void blockUser(UUID source, UUID target);

    int deleteFriendRequest(final UUID friend, final UUID requester);

    default void insertFriendRelationship(final UUID user, final UUID friend) {
        this.sortedCall(user, friend, (a, b) -> {
            this.insertFriendRelationshipImpl(a, b);
            return null;
        });
    }

    int deleteFriendshipImpl(final UUID user, final UUID friend);

    void insertFriendRelationshipImpl(final UUID user, final UUID friend);

    default <T> T sortedCall(final UUID user, final UUID friend, BiFunction<UUID, UUID, T> callback) {
        if (user.compareTo(friend) > 0) {
            return callback.apply(user, friend);
        } else {
            return callback.apply(friend, user);
        }
    }
}
