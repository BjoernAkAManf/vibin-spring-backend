package com.vibinofficial.backend.twilio;

import lombok.Data;

@Data
public class RoomGrants {
    private final String roomSid;
    private final String user1;
    private final String user2;
    private final String grantUser1;
    private final String grantUser2;
}
