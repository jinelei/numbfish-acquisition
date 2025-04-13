package com.jinelei.numbfish.acquisition.client.influx.bean;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * @Author: jinelei
 * @Description:
 * @Date: 2024/8/1 22:07
 * @Version: 1.0.0
 */
public abstract class AbstractMessage implements Serializable {
    public static final String DEVICE_CODE = "deviceCode";
    public static final String TIME = "_time";
    public static final String FIELD = "_field";
    public static final String VALUE = "_value";

    @NotNull(message = "设备编码不能为空")
    protected String deviceCode;
    @NotNull(message = "时间不能为空")
    protected Instant time;

    public abstract @NotNull(message = "存储桶不能为空") String bucket();

    public abstract @NotNull(message = "测量点不能为空") String measurement();

    public abstract @NotNull(message = "tags不能为空") Map<String, String> tags();

    public abstract @NotNull(message = "fields不能为空") Map<String, Object> fields();

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AbstractMessage that = (AbstractMessage) o;
        return Objects.equals(deviceCode, that.deviceCode) && Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceCode, time);
    }

    @Override
    public String toString() {
        return "AbstractMessage{" +
                "deviceCode='" + deviceCode + '\'' +
                ", time=" + time +
                '}';
    }
}