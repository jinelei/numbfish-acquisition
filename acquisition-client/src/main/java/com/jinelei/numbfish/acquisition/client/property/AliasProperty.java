package com.jinelei.numbfish.acquisition.client.property;

import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * @Author: jinelei
 * @Description: 字段映射关系
 * @Date: 2024/7/28
 * @Version: 1.0.0
 */
@SuppressWarnings("unused")
public class AliasProperty {
    /**
     * 设备编码
     */
    @NotNull(message = "设备编码不合法")
    protected String deviceCode;
    /**
     * 设备状态
     */
    @NotNull(message = "设备状态不合法")
    protected String state;
    /**
     * 事件
     */
    @NotNull(message = "事件不合法")
    protected String event;
    /**
     * 设备产量
     */
    @NotNull(message = "设备产量不合法")
    protected String produce;
    /**
     * 时间戳
     */
    @NotNull(message = "时间戳不合法")
    protected String timestamp;

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getProduce() {
        return produce;
    }

    public void setProduce(String produce) {
        this.produce = produce;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AliasProperty that = (AliasProperty) o;
        return Objects.equals(deviceCode, that.deviceCode) && Objects.equals(state, that.state) && Objects.equals(event, that.event) && Objects.equals(produce, that.produce) && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceCode, state, event, produce, timestamp);
    }

    @Override
    public String toString() {
        return "AliasProperty{" +
                "deviceCode='" + deviceCode + '\'' +
                ", state='" + state + '\'' +
                ", event='" + event + '\'' +
                ", produce='" + produce + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}