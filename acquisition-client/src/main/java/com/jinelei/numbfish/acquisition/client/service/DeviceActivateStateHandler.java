package com.jinelei.numbfish.acquisition.client.service;

import com.jinelei.numbfish.acquisition.client.influx.bean.DeviceConnect;
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
public class DeviceActivateStateHandler implements MessageHandler {
    private static final Logger log = LoggerFactory.getLogger(DeviceActivateStateHandler.class);
    public final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 5, 10, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(1000), r -> new Thread(r, this.getClass().getSimpleName()),
            (r, executor) -> r.run());

    @Override
    public void handleMessage(@NotNull Message<?> message) throws MessagingException {
        try {
            if (message.getPayload() instanceof DeviceConnect dc) {
                log.debug("updateDeviceActiveState: {}", dc);
            }
        } catch (Throwable throwable) {
            log.error("updateDeviceActiveState failure: {}", throwable.getMessage());
        }
    }
}
