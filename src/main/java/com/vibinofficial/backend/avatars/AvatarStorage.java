package com.vibinofficial.backend.avatars;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public interface AvatarStorage {
    Avatar DEFAULT = new DefaultAvatar();

    Avatar read(String path) throws IOException;

    /** mediaType may be null if unknown or doesn't matter */
    void write(String path, InputStream is, String mediaType) throws IOException;

    interface Avatar extends Closeable {
        InputStream open() throws IOException;

        String getMediaType();
    }
}
