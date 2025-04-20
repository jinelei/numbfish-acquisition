package com.jinelei.numbfish.acquisition.service;

import com.jinelei.numbfish.acquisition.influx.InfluxService;
import com.jinelei.numbfish.acquisition.influx.bean.DeviceStateMessage;
import com.jinelei.numbfish.common.exception.InternalException;
import com.jinelei.numbfish.common.view.BaseView;
import com.jinelei.numbfish.device.api.DeviceApi;
import com.jinelei.numbfish.device.dto.DeviceRunningStateUpdateRequest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * @Author: jinelei
 * @Description:
 * @Date: 2024/7/16
 * @Version: 1.0.0
 */
@Component
public class DeviceStateHandler implements MessageHandler, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(DeviceStateHandler.class);

    private final InfluxService influxService;
    private final DeviceApi deviceApi;

    public DeviceStateHandler(InfluxService influxService, DeviceApi deviceApi) {
        this.influxService = influxService;
        this.deviceApi = deviceApi;
    }

    @Override
    public void handleMessage(@NotNull Message<?> message) throws MessagingException {
        try {
            if (message.getPayload() instanceof DeviceStateMessage ds) {
                influxService.saveDeviceStateMessages(List.of(ds), false);
                log.debug("saveDeviceRunState success: {}", ds);
            }
        } catch (Throwable throwable) {
            log.error("saveDeviceRunState failure: {}", throwable.getMessage());
        }
        try {
            if (message.getPayload() instanceof DeviceStateMessage ds) {
                Optional.ofNullable(deviceApi).ifPresent(api -> {
                    DeviceRunningStateUpdateRequest request = new DeviceRunningStateUpdateRequest();
                    request.setDeviceCode(ds.getDeviceCode());
                    request.setRunningState(ds.getState());
                    request.setTimestamp(LocalDateTime.ofInstant(ds.getTime(), ZoneId.systemDefault()));
                    BaseView<Void> result = api.updateRunningState(request);
                    log.debug("updateRunningState success: {}", result);
                });
            }
        } catch (Throwable throwable) {
            log.error("updateRunningState failure: {}", throwable.getMessage());
        }
    }

    @Override
    public void afterPropertiesSet() {
        Optional.ofNullable(influxService).orElseThrow(() -> new InternalException("influxService is null"));
    }
}
