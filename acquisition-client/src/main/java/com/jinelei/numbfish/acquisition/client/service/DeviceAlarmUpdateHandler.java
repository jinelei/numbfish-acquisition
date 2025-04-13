package com.jinelei.numbfish.acquisition.client.service;

import com.jinelei.numbfish.acquisition.client.influx.bean.DeviceParameter;
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
public class DeviceAlarmUpdateHandler implements MessageHandler {
    private static final Logger log = LoggerFactory.getLogger(DeviceAlarmUpdateHandler.class);
    public final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 5, 10, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(1000), r -> new Thread(r, this.getClass().getSimpleName()),
            (r, executor) -> r.run());

    @Override
    public void handleMessage(@NotNull Message<?> message) throws MessagingException {
        try {
            if (message.getPayload() instanceof DeviceParameter dp) {
//                SpringHelper.getBean(InfluxService.class).ifPresent(influxService -> SpringHelper
//                        .getBean(DeviceService.class)
//                        .ifPresent(deviceService -> SpringHelper.getBean(AlarmConfigService.class)
//                                .ifPresent(alarmConfigService -> SpringHelper.getBean(AlarmRecordService.class)
//                                        .ifPresent(alarmRecordService -> SpringHelper
//                                                .getBean(DeviceParameterCacheService.class)
//                                                .ifPresent(deviceParameterCacheService -> executor
//                                                        .submit(() -> deviceService.findByDeviceCode(dp.getDeviceCode())
//                                                                .ifPresent(device -> {
//                                                                    try {
//                                                                        final List<AlarmConfigEntity> alarmConfigEntities = alarmConfigCache
//                                                                                .get(device.getId(),
//                                                                                        alarmConfigService::findDeviceAlarmConfigByDeviceId);
//                                                                        alarmConfigEntities.parallelStream()
//                                                                                .map(config -> new AlarmHelper(
//                                                                                        config.getAlarmRule(),
//                                                                                        device.getId(), config.getId()))
//                                                                                .forEach(helper -> {
//                                                                                    // Map<String, Object> map =
//                                                                                    // influxService.queryLatestDeviceParameters(device.getCode(),
//                                                                                    // helper.getVariableNames());
//                                                                                    Map<String, Object> map = deviceParameterCacheService
//                                                                                            .queryByDeviceCode(
//                                                                                                    device.getCode());
//                                                                                    helper.setVariables(map);
//                                                                                    Boolean result = helper.calculate();
//                                                                                    final Long deviceId = helper
//                                                                                            .getDeviceId();
//                                                                                    final Long configId = helper
//                                                                                            .getConfigId();
//                                                                                    final Instant localDateTime = dp
//                                                                                            .getTime();
//                                                                                    alarmRecordService
//                                                                                            .saveOrUpdateAsync(
//                                                                                                    new AlarmUpdateRequest(
//                                                                                                            deviceId,
//                                                                                                            configId,
//                                                                                                            localDateTime,
//                                                                                                            result));
//                                                                                });
//                                                                    } catch (Throwable throwable) {
//                                                                        log.error(
//                                                                                "update or create alarm record: {}",
//                                                                                throwable.getMessage());
//                                                                    }
//                                                                })))))));
            }
        } catch (Throwable throwable) {
            log.error("deviceAlarmUpdate: {}", throwable.getMessage());
        }
    }
}
