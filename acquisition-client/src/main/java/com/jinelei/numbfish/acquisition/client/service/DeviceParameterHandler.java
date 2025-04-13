package com.jinelei.numbfish.acquisition.client.service;

import com.jinelei.numbfish.acquisition.client.influx.bean.DeviceParameter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

/**
 * @Author: jinelei
 * @Description:
 * @Date: 2024/7/16
 * @Version: 1.0.0
 */
@Service
public class DeviceParameterHandler implements MessageHandler {
    private static final Logger log = LoggerFactory.getLogger(DeviceParameterHandler.class);

    @Override
    public void handleMessage(@NotNull Message<?> message) throws MessagingException {
        try {
            if (message.getPayload() instanceof DeviceParameter dp) {
//                SpringHelper.getBean(InfluxService.class)
//                        .ifPresent(influxService -> SpringHelper.getBean(DeviceService.class)
//                                .ifPresent(deviceService -> deviceService.findByDeviceCode(dp.getDeviceCode())
//                                        .ifPresent(device -> {
//                                            influxService.savePointsAsync(dp);
//                                            SpringHelper.getBean(DeviceParameterCacheService.class)
//                                                    .ifPresent(s -> s.save(dp));
//                                            log.debug("Write deviceParameter success: {}", dp);
//                                        })));
            }
        } catch (Throwable throwable) {
            log.error("saveDeviceParameter: {}", throwable.getMessage());
        }
    }
}
