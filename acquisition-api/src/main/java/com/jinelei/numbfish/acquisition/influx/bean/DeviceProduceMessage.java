package com.jinelei.numbfish.acquisition.influx.bean;

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
public class DeviceProduceMessage extends AbstractMessage {
    public static final String PRODUCE = "produce";
    public static final String DISPLAY = "display";
    private Double produce;
    private Double display;

    @Override
    public String bucket() {
        return "DeviceProduce";
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
        return Map.of(PRODUCE, getProduce(), DISPLAY, getDisplay());
    }

    public Double getProduce() {
        return produce;
    }

    public void setProduce(Double produce) {
        this.produce = produce;
    }

    public Double getDisplay() {
        return display;
    }

    public void setDisplay(Double display) {
        this.display = display;
    }

    public DeviceProduceMessage() {
    }

    public DeviceProduceMessage(String deviceCode, Instant time, Double produce, Double display) {
        super.setDeviceCode(deviceCode);
        super.setTime(time);
        this.produce = produce;
        this.display = display;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DeviceProduceMessage that))
            return false;
        if (!super.equals(o))
            return false;
        return Objects.equals(produce, that.produce) && Objects.equals(display, that.display);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), produce, display);
    }

    @Override
    public String toString() {
        return "DeviceProduceMessage{" +
                "produce=" + produce +
                ", display=" + display +
                ", deviceCode='" + deviceCode + '\'' +
                ", time=" + time +
                '}';
    }

    @SafeVarargs
    public final DeviceProduceMessage parse(final Map<String, Object>... maps) {
        for (Map<String, Object> map : maps) {
            Optional.ofNullable(map.get(DEVICE_CODE)).map(Object::toString).ifPresent(this::setDeviceCode);
            Optional.ofNullable(map.get(TIME)).map(Object::toString).map(Instant::parse).ifPresent(this::setTime);
            Optional.ofNullable(map.get(FIELD)).map(Object::toString).ifPresent(it -> {
                switch (it) {
                    case PRODUCE ->
                            Optional.ofNullable(map.get(VALUE)).map(Object::toString).map(Double::parseDouble)
                                    .ifPresent(this::setProduce);
                    case DISPLAY ->
                            Optional.ofNullable(map.get(VALUE)).map(Object::toString).map(Double::parseDouble)
                                    .ifPresent(this::setDisplay);
                    default -> {
                    }
                }
            });
        }
        return this;
    }
}