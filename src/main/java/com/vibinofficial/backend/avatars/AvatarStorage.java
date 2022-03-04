package com.vibinofficial.backend.avatars;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public interface AvatarStorage {
    Avatar DEFAULT = new DefaultAvatar();

    Avatar read(String path) throws IOException;

    void write(String path, InputStream is) throws IOException;

    interface Avatar extends Closeable {
        InputStream open() throws IOException;

        String getMediaType();
    }
}
