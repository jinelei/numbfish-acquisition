package com.jinelei.numbfish.acquisition.client.service;

import cn.jinelei.app.dal.influx.InfluxService;
import cn.jinelei.app.dal.influx.bean.DeviceParameter;
import cn.jinelei.app.dal.influx.bean.DeviceProduce;
import cn.jinelei.app.domain.entity.DeviceEntity;
import cn.jinelei.app.domain.entity.ParameterEntity;
import cn.jinelei.app.enumrica.Ability;
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

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
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
public class DeviceProduceUpdateHandler implements MessageHandler {
  public final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 5, 10, TimeUnit.MINUTES,
      new LinkedBlockingQueue<>(1000), r -> new Thread(r, this.getClass().getSimpleName()),
      (r, executor) -> r.run());
  private final Cache<DeviceEntity, Set<ParameterEntity>> deviceProduceParameterCache = Caffeine.newBuilder()
      .expireAfterWrite(Duration.ofMinutes(5)).build();

  @Override
  public void handleMessage(@NotNull Message<?> message) throws MessagingException {
    try {
      if (message.getPayload() instanceof DeviceParameter dp) {
        if (Number.class.isAssignableFrom(dp.getValue().getClass())) {
          SpringHelper.getBean(InfluxService.class).ifPresent(influxService -> SpringHelper
              .getBean(DeviceService.class).ifPresent(deviceService -> executor.submit(
                  () -> deviceService.findByDeviceCode(dp.getDeviceCode()).ifPresent(device -> {
                    try {
                      final Set<ParameterEntity> produces = deviceProduceParameterCache.get(
                          device,
                          it -> deviceService.findSpecificParameters(device, model -> true,
                              p -> Optional.ofNullable(p.getAbilities())
                                  .map(c -> c.contains(Ability.PRODUCE))
                                  .orElse(false)));
                      if (produces.parallelStream().map(ParameterEntity::getCode)
                          .anyMatch(it -> dp.getName().equals(it))) {
                        final DeviceProduce deviceProduce = new DeviceProduce(device.getCode(),
                            dp.getTime(), Double.parseDouble(dp.getValue().toString()),
                            Double.parseDouble(dp.getValue().toString()));
                        influxService.savePointsAsync(deviceProduce);
                        log.debug("Write deviceProduce success: {}", deviceProduce);
                      }
                    } catch (Throwable throwable) {
                      log.error("save produce error: {}", throwable.getMessage());
                    }
                  }))));
        }
      }
    } catch (Throwable throwable) {
      log.error("updateDeviceProduceState: {}", throwable.getMessage());
    }
  }
}
