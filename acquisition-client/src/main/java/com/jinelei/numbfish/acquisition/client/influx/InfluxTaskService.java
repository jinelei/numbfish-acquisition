package com.jinelei.numbfish.acquisition.client.influx;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@AllArgsConstructor
@ConditionalOnBean(value = InfluxService.class)
public class InfluxTaskService implements InitializingBean {
  private InfluxService influxService;
  private final ScheduledExecutorService service = new ScheduledThreadPoolExecutor(5, Thread::new,
      (r, e) -> log.error("InfluxService push task failure: task queue full"));

  @Override
  public void afterPropertiesSet() throws Exception {
    this.service.scheduleWithFixedDelay(() -> influxService.asyncSaveDeviceParameterBatch(), 13, 5, TimeUnit.SECONDS);
    this.service.scheduleWithFixedDelay(() -> influxService.asyncSaveDeviceProduceBatch(), 15, 10, TimeUnit.SECONDS);
    this.service.scheduleWithFixedDelay(() -> influxService.asyncSaveDeviceStateBatch(), 10, 10, TimeUnit.SECONDS);
  }
}
