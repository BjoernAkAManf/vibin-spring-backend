package com.vibinofficial.backend.hasura;

import com.vibinofficial.backend.UidUtils;
import lombok.Data;

@Data
public class Room {
    private String user1;
    private String user2;
    private String room_id;
    private String last_check;

    @Override
    public String toString() {
        return "Room(" +
                "user1=" + UidUtils.shorten(user1) +
                ", user2=" + UidUtils.shorten(user2) +
                ", room_id=" + UidUtils.shorten(room_id) +
                ", last_check=" + last_check +
                ')';
    }
}
