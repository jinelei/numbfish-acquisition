package com.jinelei.numbfish.acquisition.client.service;

import cn.jinelei.app.dal.influx.InfluxService;
import cn.jinelei.app.dal.influx.bean.DeviceConnect;
import cn.jinelei.app.service.DeviceService;
import cn.jinelei.core.helper.SpringHelper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
@Slf4j
@Service
public class DeviceConnectionHandler implements MessageHandler {

  @Override
  public void handleMessage(@NotNull Message<?> message) throws MessagingException {
    try {
      if (message.getPayload() instanceof DeviceConnect dc) {
        SpringHelper.getBean(InfluxService.class)
            .ifPresent(influxService -> SpringHelper.getBean(DeviceService.class)
                .ifPresent(deviceService -> deviceService.findByDeviceCode(dc.getDeviceCode())
                    .ifPresent(device -> {
                      influxService.savePoints(dc);
                      log.debug("Write deviceConnect success: {}", message);
                    })));
      }
    } catch (Throwable throwable) {
      log.error("saveDeviceConnect: {}", throwable.getMessage());
    }
  }
}
