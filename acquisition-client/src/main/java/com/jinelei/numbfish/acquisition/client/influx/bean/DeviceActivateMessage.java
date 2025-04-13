package com.jinelei.numbfish.acquisition.client.influx.bean;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * @Author: jinelei
 * @Description:
 * @Date: 2024/7/14
 * @Version: 1.0.0
 */
@SuppressWarnings("unused")
public class DeviceActivateMessage extends AbstractMessage {

    @Override
    public String bucket() {
        return "DeviceActivate";
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
        return Map.of();
    }

    @SafeVarargs
    public final DeviceActivateMessage parse(final Map<String, Object>... maps) {
        for (Map<String, Object> map : maps) {
            Optional.ofNullable(map.get(DEVICE_CODE)).map(Object::toString).ifPresent(this::setDeviceCode);
            Optional.ofNullable(map.get(TIME)).map(Object::toString).map(Instant::parse).ifPresent(this::setTime);
        }
        return this;
    }

    public DeviceActivateMessage() {
    }

    public DeviceActivateMessage(String deviceCode, Instant time) {
        super.setDeviceCode(deviceCode);
        super.setTime(time);
    }

    @Override
    public String toString() {
        return "DeviceActivateMessage{" +
                "deviceCode='" + deviceCode + '\'' +
                ", time=" + time +
                '}';
    }
}