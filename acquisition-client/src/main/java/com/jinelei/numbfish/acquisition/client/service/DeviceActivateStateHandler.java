package com.jinelei.numbfish.acquisition.client.service;

import cn.jinelei.app.dal.influx.bean.DeviceConnect;
import cn.jinelei.app.domain.query.DeviceQuery;
import cn.jinelei.app.enumrica.DeviceActivateState;
import cn.jinelei.app.service.DeviceService;
import cn.jinelei.core.helper.Builder;
import cn.jinelei.core.helper.SpringHelper;
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
public class DeviceActivateStateHandler implements MessageHandler {
  public final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 5, 10, TimeUnit.MINUTES,
      new LinkedBlockingQueue<>(1000), r -> new Thread(r, this.getClass().getSimpleName()),
      (r, executor) -> r.run());

  @Override
  public void handleMessage(@NotNull Message<?> message) throws MessagingException {
    try {
      if (message.getPayload() instanceof DeviceConnect dc) {
        SpringHelper.getBean(DeviceService.class).ifPresent(deviceService -> deviceService
            .findByDeviceCode(dc.getDeviceCode()).ifPresent(device -> executor.submit(() -> {
              DeviceQuery build = Builder.of(DeviceQuery::new).with(DeviceQuery::setId, device.getId())
                  .with(DeviceQuery::setActivateState, DeviceActivateState.ACTIVATED).build();
              deviceService.update(build);
              log.debug("updateDeviceActiveState success: {}", message);
            })));
      }
    } catch (Throwable throwable) {
      log.error("updateDeviceActiveState failure: {}", throwable.getMessage());
    }
  }
}
