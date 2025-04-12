package com.jinelei.numbfish.acquisition.client.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("unused")
@Component
@ConfigurationProperties(prefix = "numbfish.acquisition")
public class AcquisitionProperty {
    protected MqttProperty mqtt;

    public MqttProperty getMqtt() {
        return mqtt;
    }

    public void setMqtt(MqttProperty mqtt) {
        this.mqtt = mqtt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AcquisitionProperty that = (AcquisitionProperty) o;
        return Objects.equals(mqtt, that.mqtt);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mqtt);
    }

    @Override
    public String toString() {
        return "AcquisitionProperty{" +
                "mqtt=" + mqtt +
                '}';
    }
}