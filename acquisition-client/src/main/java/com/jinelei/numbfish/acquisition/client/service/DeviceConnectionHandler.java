package com.jinelei.numbfish.acquisition.client.service;

import com.jinelei.numbfish.acquisition.influx.InfluxService;
import com.jinelei.numbfish.acquisition.influx.bean.DeviceConnectMessage;
import com.jinelei.numbfish.common.exception.InternalException;
import com.jinelei.numbfish.common.view.BaseView;
import com.jinelei.numbfish.device.api.DeviceApi;
import com.jinelei.numbfish.device.dto.DeviceActivateStateUpdateRequest;
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
    private final DeviceApi deviceApi;

    public DeviceConnectionHandler(InfluxService influxService, DeviceApi deviceApi) {
        this.influxService = influxService;
        this.deviceApi = deviceApi;
    }

    @Override
    public void handleMessage(@NotNull Message<?> message) throws MessagingException {
        try {
            if (message.getPayload() instanceof DeviceConnectMessage dc) {
                Optional.ofNullable(deviceApi).ifPresent(api -> {
                    DeviceActivateStateUpdateRequest request = new DeviceActivateStateUpdateRequest();
                    request.setDeviceCode(dc.getDeviceCode());
                    request.setTimestamp(LocalDateTime.ofInstant(dc.getTime(), ZoneId.systemDefault()));
                    BaseView<Void> result = api.updateActivateState(request);
                    log.debug("updateActivateState success: {}", result);
                });
                log.info("updateActivateState success: {}", dc);
            }
        } catch (Throwable throwable) {
            log.error("updateActivateState failure: {}", throwable.getMessage());
        }
    }

    @Override
    public void afterPropertiesSet() {
        Optional.ofNullable(influxService).orElseThrow(() -> new InternalException("influxService is null"));
    }
}
