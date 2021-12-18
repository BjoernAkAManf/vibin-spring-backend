package com.vibinofficial.backend;

import java.util.List;
import java.util.UUID;

public interface FriendService {
    List<UUID> findFriendsOf(UUID user);

    void sendFriendRequest(UUID user, UUID friend);

    boolean acceptFriendRequest(UUID friend, UUID requester);

    boolean deleteFriendship(UUID user, UUID friend);

    void ignoreFriendRequest(UUID friend, UUID requester);

    void blockUser(UUID source, UUID target);
}
