package com.jinelei.numbfish.acquisition.client.service;

import com.jinelei.numbfish.acquisition.client.influx.InfluxService;
import com.jinelei.numbfish.acquisition.client.influx.bean.DeviceParameterMessage;
import com.jinelei.numbfish.common.exception.InternalException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @Author: jinelei
 * @Description:
 * @Date: 2024/7/16
 * @Version: 1.0.0
 */
@Component
public class DeviceAlarmHandler implements MessageHandler, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(DeviceAlarmHandler.class);

    private final InfluxService influxService;

    public DeviceAlarmHandler(InfluxService influxService) {
        this.influxService = influxService;
    }

    @Override
    public void handleMessage(@NotNull Message<?> message) throws MessagingException {
        try {
            if (message.getPayload() instanceof DeviceParameterMessage dp) {
                log.info("DeviceAlarmUpdateHandler: {}", dp);
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

    @Override
    public void afterPropertiesSet() {
        Optional.ofNullable(influxService).orElseThrow(() -> new InternalException("influxService is null"));
    }

}
