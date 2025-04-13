package com.jinelei.numbfish.acquisition.client.influx.bean;

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
public class DeviceParameterMessage extends AbstractMessage {
    public static final String NAME = "name";
    public static final String VALUE1 = "value";
    private String name;
    private Object value;

    @Override
    public String bucket() {
        return "DeviceParameter";
    }

    @Override
    public String measurement() {
        return "%s_%s".formatted(getDeviceCode(), getName());
    }

    @Override
    public Map<String, String> tags() {
        return Map.of(DEVICE_CODE, getDeviceCode(), NAME, getName());
    }

    @Override
    public Map<String, Object> fields() {
        return Map.of(VALUE1, getValue());
    }

    @SafeVarargs
    public final DeviceParameterMessage parse(final Map<String, Object>... maps) {
        for (Map<String, Object> map : maps) {
            Optional.ofNullable(map.get(DEVICE_CODE)).map(Object::toString).ifPresent(this::setDeviceCode);
            Optional.ofNullable(map.get(TIME)).map(Object::toString).map(Instant::parse).ifPresent(this::setTime);
            Optional.ofNullable(map.get(NAME)).map(Object::toString).ifPresent(this::setName);
            Optional.ofNullable(map.get(VALUE)).ifPresent(this::setValue);
        }
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public DeviceParameterMessage() {
    }

    public DeviceParameterMessage(String deviceCode, Instant time, String name, Object value) {
        super.setDeviceCode(deviceCode);
        super.setTime(time);
        this.name = name;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DeviceParameterMessage that))
            return false;
        if (!super.equals(o))
            return false;
        return Objects.equals(name, that.name) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, value);
    }

    @Override
    public String toString() {
        return "DeviceParameterMessage{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", deviceCode='" + deviceCode + '\'' +
                ", time=" + time +
                '}';
    }
}