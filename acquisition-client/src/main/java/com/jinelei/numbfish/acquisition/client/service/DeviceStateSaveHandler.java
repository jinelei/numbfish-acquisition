package com.jinelei.numbfish.acquisition.client.service;

import cn.jinelei.app.dal.influx.InfluxService;
import cn.jinelei.app.dal.influx.bean.DeviceState;
import cn.jinelei.app.enumrica.RunningState;
import cn.jinelei.app.service.DeviceService;
import cn.jinelei.core.helper.SpringHelper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
@Slf4j
@Service
public class DeviceStateSaveHandler implements MessageHandler {
  public final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 5, 10, TimeUnit.MINUTES,
      new LinkedBlockingQueue<>(1000), r -> new Thread(r, this.getClass().getSimpleName()),
      (r, executor) -> r.run());
  private final Cache<String, RunningState> deviceStateCache = Caffeine.newBuilder().build();

  @Override
  public void handleMessage(@NotNull Message<?> message) throws MessagingException {
    try {
      if (message.getPayload() instanceof DeviceState ds) {
        SpringHelper.getBean(InfluxService.class).ifPresent(
            influxService -> SpringHelper.getBean(DeviceService.class).ifPresent(deviceService -> executor
                .submit(() -> deviceService.findByDeviceCode(ds.getDeviceCode()).ifPresent(device -> {
                  influxService.savePointsAsync(ds);
                  deviceStateCache.put(ds.getDeviceCode(), ds.getState());
                  log.debug("Write deviceState success: {}", ds);
                }))));
      }
    } catch (Throwable throwable) {
      log.error("saveDeviceRunState: {}", throwable.getMessage());
    }
  }
}
