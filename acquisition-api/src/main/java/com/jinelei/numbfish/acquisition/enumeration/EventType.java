package com.jinelei.numbfish.acquisition.enumeration;

/**
 * @Author: jinelei
 * @Description:
 * @Date: 2023/07/27
 * @Version: 1.0.0
 */
@SuppressWarnings("unused")
public enum EventType {
    UNKNOWN(0, "未知"),
    CLIENT_CONNECTED(1, "设备上线"),
    CLIENT_DISCONNECTED(2, "设备下线");
    private final int value;
    private final String name;

    EventType(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

}
