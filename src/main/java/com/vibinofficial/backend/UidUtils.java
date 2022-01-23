package com.vibinofficial.backend;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UidUtils {

    private UidUtils() {
    }

    public static String shorten(String uid) {
        return shorten(uid, 4);
    }

    public static String shorten(String uid, int length) {
        return uid.substring(0, Math.min(uid.length(), length));
    }

    public static List<String> shorten(Collection<String> uids) {
        return uids.stream().map(UidUtils::shorten).collect(Collectors.toList());
    }
}
