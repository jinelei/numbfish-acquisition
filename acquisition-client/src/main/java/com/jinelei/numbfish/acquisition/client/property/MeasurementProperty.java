package com.jinelei.numbfish.acquisition.client.property;

import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * @Author: jinelei
 * @Description:
 * @Date: 2024/7/28
 * @Version: 1.0.0
 */
@SuppressWarnings("unused")
public class MeasurementProperty {
    @NotNull(message = "设备参数表明不合法")
    protected String DeviceParameter;
    @NotNull(message = "设备连接表明不合法")
    protected String DeviceConnect;
    @NotNull(message = "设备状态表明不合法")
    protected String DeviceState;
    @NotNull(message = "设备产量表明不合法")
    protected String DeviceProduce;

    public String getDeviceParameter() {
        return DeviceParameter;
    }

    public void setDeviceParameter(String deviceParameter) {
        DeviceParameter = deviceParameter;
    }

    public String getDeviceConnect() {
        return DeviceConnect;
    }

    public void setDeviceConnect(String deviceConnect) {
        DeviceConnect = deviceConnect;
    }

    public String getDeviceState() {
        return DeviceState;
    }

    public void setDeviceState(String deviceState) {
        DeviceState = deviceState;
    }

    public String getDeviceProduce() {
        return DeviceProduce;
    }

    public void setDeviceProduce(String deviceProduce) {
        DeviceProduce = deviceProduce;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MeasurementProperty that = (MeasurementProperty) o;
        return Objects.equals(DeviceParameter, that.DeviceParameter) && Objects.equals(DeviceConnect, that.DeviceConnect) && Objects.equals(DeviceState, that.DeviceState) && Objects.equals(DeviceProduce, that.DeviceProduce);
    }

    @Override
    public int hashCode() {
        return Objects.hash(DeviceParameter, DeviceConnect, DeviceState, DeviceProduce);
    }

    @Override
    public String toString() {
        return "MeasurementProperty{" +
                "DeviceParameter='" + DeviceParameter + '\'' +
                ", DeviceConnect='" + DeviceConnect + '\'' +
                ", DeviceState='" + DeviceState + '\'' +
                ", DeviceProduce='" + DeviceProduce + '\'' +
                '}';
    }
}