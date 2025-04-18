package com.jinelei.numbfish.acquisition.client.service;

import com.jinelei.numbfish.acquisition.influx.InfluxService;
import com.jinelei.numbfish.acquisition.influx.bean.DeviceConnectMessage;
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
public class DeviceConnectionHandler implements MessageHandler, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(DeviceConnectionHandler.class);

    private final InfluxService influxService;

    public DeviceConnectionHandler(InfluxService influxService) {
        this.influxService = influxService;
    }

    @Override
    public void handleMessage(@NotNull Message<?> message) throws MessagingException {
        try {
            if (message.getPayload() instanceof DeviceConnectMessage dc) {
                influxService.savePoints(dc);
                log.info("saveDeviceConnect success: {}", dc);
            }
        } catch (Throwable throwable) {
            log.error("saveDeviceConnect failure: {}", throwable.getMessage());
        }
    }

    @Override
    public void afterPropertiesSet() {
        Optional.ofNullable(influxService).orElseThrow(() -> new InternalException("influxService is null"));
    }
}
