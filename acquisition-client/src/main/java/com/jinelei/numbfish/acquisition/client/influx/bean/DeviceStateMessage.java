package com.jinelei.numbfish.acquisition.client.influx.bean;

import com.jinelei.numbfish.common.helper.EnumerationHelper;
import com.jinelei.numbfish.device.enumeration.RunningState;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @Author: jinelei
 * @Description:
 * @Date: 2024/7/14
 * @Version: 1.0.0
 */
@SuppressWarnings("unused")
public class DeviceStateMessage extends AbstractMessage {
    public static final String DURATION = "duration";
    public static final String STATUS = "status";
    private Long duration;
    private RunningState state;

    @Override
    public String bucket() {
        return "DeviceState";
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
        return Map.of(DURATION, getDuration(), STATUS, getState().ordinal());
    }

    @SafeVarargs
    public final DeviceStateMessage parse(final Map<String, Object>... maps) {
        for (Map<String, Object> map : maps) {
            Optional.ofNullable(map.get(DEVICE_CODE)).map(Object::toString).ifPresent(this::setDeviceCode);
            Optional.ofNullable(map.get(TIME)).map(Object::toString).map(Instant::parse).ifPresent(this::setTime);
            Optional.ofNullable(map.get(FIELD)).map(Object::toString).ifPresent(it -> {
                switch (it) {
                    case STATUS -> Optional.ofNullable(map.get(VALUE)).map(Object::toString).map(Long::parseLong)
                            .map(i -> EnumerationHelper.parseFrom(RunningState.class, i)).ifPresent(this::setState);
                    case DURATION -> Optional.ofNullable(map.get(VALUE)).map(Object::toString).map(Long::parseLong)
                            .ifPresent(this::setDuration);
                    default -> {
                    }
                }
            });
        }
        return this;
    }

    public DeviceStateMessage() {
    }

    public DeviceStateMessage(String deviceCode, Instant time, Long duration, RunningState state) {
        super.setDeviceCode(deviceCode);
        super.setTime(time);
        this.duration = duration;
        this.state = state;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public RunningState getState() {
        return state;
    }

    public void setState(RunningState state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DeviceStateMessage that))
            return false;
        if (!super.equals(o))
            return false;
        return Objects.equals(duration, that.duration) && state == that.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), duration, state);
    }

    @Override
    public String toString() {
        return "DeviceStateMessage{" +
                "duration=" + duration +
                ", state=" + state +
                ", deviceCode='" + deviceCode + '\'' +
                ", time=" + time +
                '}';
    }
}