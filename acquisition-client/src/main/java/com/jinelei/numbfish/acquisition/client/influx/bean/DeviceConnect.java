package com.jinelei.numbfish.acquisition.client.influx.bean;

import cn.jinelei.app.enumrica.EventType;
import cn.jinelei.app.property.Influx2Property;
import cn.jinelei.app.property.MeasurementProperty;
import cn.jinelei.app.property.Property;
import cn.jinelei.core.helper.SpringHelper;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * @Author: jinelei
 * @Description: 设备连接事件
 * @Date: 2024/7/14
 * @Version: 1.0.0
 */
public class DeviceConnect extends AbstractMessage {
  private EventType event;

  @Override
  public String bucket() {
    return SpringHelper.getBean(Property.class)
        .map(Property::getInflux2)
        .map(Influx2Property::getMeasurements)
        .map(MeasurementProperty::getDeviceConnect)
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
    return Map.of("event", getEvent());
  }

  public DeviceConnect() {
  }

  public DeviceConnect(String deviceCode, Instant time, EventType event) {
    super(deviceCode, time);
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
    if (!(o instanceof DeviceConnect that))
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
    return "DeviceConnect{" +
        "event=" + event +
        "} " + super.toString();
  }
}