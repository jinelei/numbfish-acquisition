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
public class DeviceParameter extends AbstractMessage {
    private String name;
    private Object value;

    @Override
    public String bucket() {
//    return SpringHelper.getBean(Property.class)
//        .map(Property::getInflux2)
//        .map(Influx2Property::getMeasurements)
//        .map(MeasurementProperty::getDeviceParameter)
//        .orElse(getClass().getSimpleName());
        return "DeviceParameter";
    }

    @Override
    public String measurement() {
        return "%s_%s".formatted(getDeviceCode(), getName());
    }

    @Override
    public Map<String, String> tags() {
        return Map.of("deviceCode", getDeviceCode(), "name", getName());
    }

    @Override
    public Map<String, Object> fields() {
        return Map.of("value", getValue());
    }

    @SafeVarargs
    public final DeviceParameter parse(final Map<String, Object>... maps) {
        for (Map<String, Object> map : maps) {
            Optional.ofNullable(map.get("deviceCode")).map(Object::toString).ifPresent(this::setDeviceCode);
            Optional.ofNullable(map.get("_time")).map(Object::toString).map(Instant::parse).ifPresent(this::setTime);
            Optional.ofNullable(map.get("name")).map(Object::toString).ifPresent(this::setName);
            Optional.ofNullable(map.get("_value")).ifPresent(this::setValue);
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

    public DeviceParameter() {
    }

    public DeviceParameter(String deviceCode, Instant time, String name, Object value) {
        super.setDeviceCode(deviceCode);
        super.setTime(time);
        this.name = name;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DeviceParameter that))
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
        return "DeviceParameter{" +
                "name='" + name + '\'' +
                ", value=" + value +
                "} " + super.toString();
    }
}