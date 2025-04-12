package com.jinelei.numbfish.acquisition.client.service;

import cn.jinelei.app.dal.influx.InfluxService;
import cn.jinelei.app.dal.influx.bean.DeviceParameter;
import cn.jinelei.app.service.DeviceParameterCacheService;
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
public class DeviceParameterHandler implements MessageHandler {

  @Override
  public void handleMessage(@NotNull Message<?> message) throws MessagingException {
    try {
      if (message.getPayload() instanceof DeviceParameter dp) {
        SpringHelper.getBean(InfluxService.class)
            .ifPresent(influxService -> SpringHelper.getBean(DeviceService.class)
                .ifPresent(deviceService -> deviceService.findByDeviceCode(dp.getDeviceCode())
                    .ifPresent(device -> {
                      influxService.savePointsAsync(dp);
                      SpringHelper.getBean(DeviceParameterCacheService.class)
                          .ifPresent(s -> s.save(dp));
                      log.debug("Write deviceParameter success: {}", dp);
                    })));
      }
    } catch (Throwable throwable) {
      log.error("saveDeviceParameter: {}", throwable.getMessage());
    }
  }
}
