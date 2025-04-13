package com.jinelei.numbfish.acquisition.client.influx.bean;

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
public class DeviceState extends AbstractMessage {
    private Long duration;
    private RunningState state;

    @Override
    public String bucket() {
//        return SpringHelper.getBean(Property.class)
//                .map(Property::getInflux2)
//                .map(Influx2Property::getMeasurements)
//                .map(MeasurementProperty::getDeviceState)
//                .orElse(getClass().getSimpleName());
        return "DeviceState";
    }

    @Override
    public String measurement() {
        return getDeviceCode();
    }

    @Override
    public Map<String, String> tags() {
        return Map.of("deviceCode", getDeviceCode());
    }

    @Override
    public Map<String, Object> fields() {
        return Map.of("duration", getDuration(), "status", getState().ordinal());
    }

    @SafeVarargs
    public final DeviceState parse(final Map<String, Object>... maps) {
        for (Map<String, Object> map : maps) {
            Optional.ofNullable(map.get("deviceCode")).map(Object::toString).ifPresent(this::setDeviceCode);
            Optional.ofNullable(map.get("_time")).map(Object::toString).map(Instant::parse).ifPresent(this::setTime);
            Optional.ofNullable(map.get("_field")).map(Object::toString).ifPresent(it -> {
                switch (it) {
                    case "status" -> Optional.ofNullable(map.get("_value")).map(Object::toString).map(Long::parseLong)
                            .map(RunningState::parseFrom).ifPresent(this::setState);
                    case "duration" -> Optional.ofNullable(map.get("_value")).map(Object::toString).map(Long::parseLong)
                            .ifPresent(this::setDuration);
                    default -> {
                    }
                }
            });
        }
        return this;
    }

    public DeviceState() {
    }

    public DeviceState(String deviceCode, Instant time, Long duration, RunningState state) {
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
        if (!(o instanceof DeviceState that))
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
        return "DeviceState{" +
                "duration=" + duration +
                ", state=" + state +
                "} " + super.toString();
    }
}