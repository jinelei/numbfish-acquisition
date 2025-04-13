package com.jinelei.numbfish.acquisition.client.service;

import com.jinelei.numbfish.acquisition.client.influx.InfluxService;
import com.jinelei.numbfish.acquisition.client.influx.bean.DeviceStateMessage;
import com.jinelei.numbfish.common.exception.InternalException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

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

    public DeviceStateHandler(InfluxService influxService) {
        this.influxService = influxService;
    }

    @Override
    public void handleMessage(@NotNull Message<?> message) throws MessagingException {
        try {
            if (message.getPayload() instanceof DeviceStateMessage ds) {
                influxService.savePointsAsync(ds);
                log.debug("saveDeviceRunState success: {}", ds);
            }
        } catch (Throwable throwable) {
            log.error("saveDeviceRunState failure: {}", throwable.getMessage());
        }
    }

    @Override
    public void afterPropertiesSet() {
        Optional.ofNullable(influxService).orElseThrow(() -> new InternalException("influxService is null"));
    }
}
