package com.vibinofficial.backend.twilio;

import com.twilio.Twilio;
import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.rest.video.v1.Room;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Data
@Slf4j
@Service
public class VideoService {

    private final VibinConfig config;

    @EventListener
    public void onInit(final ApplicationReadyEvent ev) {
        // TODO: This sucks balls
        if (!this.config.isEnabled()) {
            log.warn("VideoService disabled!");
            return;
        }
        Twilio.init(this.config.getKey(), this.config.getToken());
    }

    public RoomGrants createRoomForMatch(String user1, String user2) {
        if (!this.config.isEnabled()) {
            throw new UnsupportedOperationException();
        }

        log.info("VidService Config: {}", config);
        Room room = Room.creator().create();
        String roomSid = room.getSid();

        AccessToken.Builder grantBuilder = config.getGrantBuilder(roomSid);
        String grantUser1 = grantBuilder.identity(user1).build().toJwt();
        String grantUser2 = grantBuilder.identity(user2).build().toJwt();

        return new RoomGrants(roomSid, user1, user2, grantUser1, grantUser2);
    }
}