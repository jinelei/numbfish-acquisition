package com.jinelei.numbfish.acquisition.influx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
@Service
@ConditionalOnBean(value = InfluxService.class)
public class InfluxTaskService implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(InfluxTaskService.class);
    private InfluxService influxService;
    private final ScheduledExecutorService service = new ScheduledThreadPoolExecutor(5, Thread::new,
            (r, e) -> log.error("InfluxService push task failure: task queue full"));

    @Override
    public void afterPropertiesSet() {
        this.service.scheduleWithFixedDelay(() -> influxService.taskBatchSaveDeviceParameter(), 13, 5, TimeUnit.SECONDS);
        this.service.scheduleWithFixedDelay(() -> influxService.taskBatchSaveDeviceProduce(), 15, 10, TimeUnit.SECONDS);
        this.service.scheduleWithFixedDelay(() -> influxService.taskBatchSaveDeviceState(), 10, 10, TimeUnit.SECONDS);
    }
}
