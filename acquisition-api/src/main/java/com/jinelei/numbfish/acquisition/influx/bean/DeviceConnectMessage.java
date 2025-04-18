package com.jinelei.numbfish.acquisition.influx.bean;

import com.jinelei.numbfish.acquisition.enumeration.EventType;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * @Author: jinelei
 * @Description: 设备连接事件
 * @Date: 2024/7/14
 * @Version: 1.0.0
 */
@SuppressWarnings("unused")
public class DeviceConnectMessage extends AbstractMessage {
    public static final String EVENT = "event";
    private EventType event;

    @Override
    public String bucket() {
        return "DeviceConnect";
    }

    @Override
    public String measurement() {
        return getDeviceCode();
    }

    @Override
    public Map<String, String> tags() {
        return Map.of(DEVICE_CODE, getDeviceCode());
    }

    @Override
    public Map<String, Object> fields() {
        return Map.of(EVENT, getEvent());
    }

    public DeviceConnectMessage() {
    }

    public DeviceConnectMessage(String deviceCode, Instant time, EventType event) {
        super.setDeviceCode(deviceCode);
        super.setTime(time);
        this.event = event;
    }

    public EventType getEvent() {
        return event;
    }

    public void setEvent(EventType event) {
        this.event = event;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DeviceConnectMessage that))
            return false;
        if (!super.equals(o))
            return false;
        return event == that.event;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), event);
    }

    @Override
    public String toString() {
        return "DeviceConnectMessage{" +
                "event=" + event +
                ", deviceCode='" + deviceCode + '\'' +
                ", time=" + time +
                '}';
    }
}