package com.jinelei.numbfish.acquisition.client.influx.bean;

import cn.jinelei.app.property.Influx2Property;
import cn.jinelei.app.property.MeasurementProperty;
import cn.jinelei.app.property.Property;
import cn.jinelei.core.helper.SpringHelper;

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
public class DeviceProduce extends AbstractMessage {
  private Double produce;
  private Double display;

  @Override
  public String bucket() {
    return SpringHelper.getBean(Property.class)
        .map(Property::getInflux2)
        .map(Influx2Property::getMeasurements)
        .map(MeasurementProperty::getDeviceProduce)
        .orElse(getClass().getSimpleName());
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
    return Map.of("produce", getProduce(), "display", getDisplay());
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

  public DeviceProduce() {
  }

  public DeviceProduce(String deviceCode, Instant time, Double produce, Double display) {
    super(deviceCode, time);
    this.produce = produce;
    this.display = display;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof DeviceProduce that))
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
    return "DeviceProduce{" +
        "produce=" + produce +
        ", display=" + display +
        "} " + super.toString();
  }

  @SafeVarargs
  public final DeviceProduce parse(final Map<String, Object>... maps) {
    for (Map<String, Object> map : maps) {
      Optional.ofNullable(map.get("deviceCode")).map(Object::toString).ifPresent(this::setDeviceCode);
      Optional.ofNullable(map.get("_time")).map(Object::toString).map(Instant::parse).ifPresent(this::setTime);
      Optional.ofNullable(map.get("_field")).map(Object::toString).ifPresent(it -> {
        switch (it) {
          case "produce" ->
            Optional.ofNullable(map.get("_value")).map(Object::toString).map(Double::parseDouble)
                .ifPresent(this::setProduce);
          case "display" ->
            Optional.ofNullable(map.get("_value")).map(Object::toString).map(Double::parseDouble)
                .ifPresent(this::setDisplay);
          default -> {
          }
        }
      });
    }
    return this;
  }
}