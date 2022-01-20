package com.vibinofficial.backend.twilio;

import com.twilio.Twilio;
import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.jwt.accesstoken.VideoGrant;
import com.twilio.rest.video.v1.Room;
import lombok.Data;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Data
@Service
public class VideoService {
    private final VibinConfig config;

    @EventListener
    public void onInit(final ApplicationReadyEvent ev) {
        Twilio.init(this.config.getKey(), this.config.getToken());
    }

    public void createRoomForMatch(String user1, String user2) {
        // TODO old strings: "meow", "foo"
        Room room = Room.creator().create();
        String roomSid = room.getSid();

        // TODO move in config
        AccessToken.Builder grantBuilder = new AccessToken.Builder(this.config.getAccount(), this.config.getKey(), this.config.getToken())
                .grant(new VideoGrant().setRoom(roomSid));

        String grantUser1 = grantBuilder.identity(user1).build().toJwt();
        String grantUser2 = grantBuilder.identity(user2).build().toJwt();

    }
}