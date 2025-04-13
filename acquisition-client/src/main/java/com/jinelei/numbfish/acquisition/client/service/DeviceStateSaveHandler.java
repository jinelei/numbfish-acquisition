package com.jinelei.numbfish.acquisition.client.service;

import com.jinelei.numbfish.acquisition.client.influx.bean.DeviceState;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author: jinelei
 * @Description:
 * @Date: 2024/7/16
 * @Version: 1.0.0
 */
@Service
public class DeviceStateSaveHandler implements MessageHandler {
    private static final Logger log = LoggerFactory.getLogger(DeviceStateSaveHandler.class);
    public final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 5, 10, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(1000), r -> new Thread(r, this.getClass().getSimpleName()),
            (r, executor) -> r.run());

    @Override
    public void handleMessage(@NotNull Message<?> message) throws MessagingException {
        try {
            if (message.getPayload() instanceof DeviceState ds) {
                log.debug("saveDeviceRunState: {}", ds);
//                SpringHelper.getBean(InfluxService.class).ifPresent(
//                        influxService -> SpringHelper.getBean(DeviceService.class).ifPresent(deviceService -> executor
//                                .submit(() -> deviceService.findByDeviceCode(ds.getDeviceCode()).ifPresent(device -> {
//                                    influxService.savePointsAsync(ds);
//                                    deviceStateCache.put(ds.getDeviceCode(), ds.getState());
//                                    log.debug("Write deviceState success: {}", ds);
//                                }))));
            }
        } catch (Throwable throwable) {
            log.error("saveDeviceRunState: {}", throwable.getMessage());
        }
    }
}
