package com.vibinofficial.backend.twilio;

import com.twilio.Twilio;
import com.twilio.http.TwilioRestClient;
import com.twilio.jwt.accesstoken.AccessToken;
import com.twilio.rest.video.v1.Room;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Data
@Slf4j
@Service
public class VideoService {
    private final VibinConfig config;
    private TwilioRestClient client;

    @EventListener
    public void onInit(final ApplicationReadyEvent ev) {
        if (this.config.isDisabled()) {
            log.warn("VideoService disabled!");
            return;
        }
        Twilio.init(this.config.getKey(), this.config.getToken());
        this.client = Twilio.getRestClient();
    }

    public RoomGrants createRoomForMatch(String user1, String user2) {
        if (this.config.isDisabled()) {
            throw new UnsupportedOperationException();
        }

        Room room = Room.creator().create();
        String roomSid = room.getSid();

        AccessToken.Builder grantBuilder = config.getGrantBuilder(roomSid);
        String grantUser1 = grantBuilder.identity(user1).build().toJwt();
        String grantUser2 = grantBuilder.identity(user2).build().toJwt();

        log.info("Created room for {}/{}: {}", user1, user2, roomSid);
        return new RoomGrants(roomSid, user1, user2, grantUser1, grantUser2);
    }

    public Mono<Void> deleteRoom(final String roomId) {
        return Mono.fromRunnable(() -> {
            if (this.config.isDisabled()) {
                throw new UnsupportedOperationException();
            }

            Room.updater(roomId, Room.RoomStatus.COMPLETED).update(this.client);
        });
    }
}