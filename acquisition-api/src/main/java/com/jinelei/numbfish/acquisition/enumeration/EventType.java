package com.jinelei.numbfish.acquisition.enumeration;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @Author: jinelei
 * @Description:
 * @Date: 2023/07/27
 * @Version: 1.0.0
 */
@SuppressWarnings("unused")
public enum EventType {
    UNKNOWN(0, "未知", value -> true),
    CLIENT_CONNECTED(1, "设备上线", v -> "client.connected".equalsIgnoreCase(Optional.ofNullable(v).map(Object::toString).orElse(""))),
    CLIENT_DISCONNECTED(2, "设备下线", v -> "client.disconnected".equalsIgnoreCase(Optional.ofNullable(v).map(Object::toString).orElse("")));
    private final int value;
    private final String name;
    private final Predicate<Object> matcher;

    EventType(int value, String name, Predicate<Object> matcher) {
        this.value = value;
        this.name = name;
        this.matcher = matcher;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public Predicate<Object> getMatcher() {
        return matcher;
    }

    public static EventType parseFrom(Object value) {
        EventType[] values = EventType.values();
        for (int i = 0; i < values.length; i++) {
            EventType v = values[values.length - 1 - i];
            if (v.matcher.test(value)) {
                return v;
            }
        }
        return UNKNOWN;
    }

}
