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
public class DeviceProduceHandler implements MessageHandler, InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(DeviceProduceHandler.class);

    private final InfluxService influxService;

    public DeviceProduceHandler(InfluxService influxService) {
        this.influxService = influxService;
    }

    @Override
    public void handleMessage(@NotNull Message<?> message) throws MessagingException {
        try {
            if (message.getPayload() instanceof DeviceParameterMessage dp) {
                if (Number.class.isAssignableFrom(dp.getValue().getClass())) {
                    log.info("DeviceProduceHandler: {}", dp);
//                    SpringHelper.getBean(InfluxService.class).ifPresent(influxService -> SpringHelper
//                            .getBean(DeviceService.class).ifPresent(deviceService -> executor.submit(
//                                    () -> deviceService.findByDeviceCode(dp.getDeviceCode()).ifPresent(device -> {
//                                        try {
//                                            final Set<ParameterEntity> produces = deviceProduceParameterCache.get(
//                                                    device,
//                                                    it -> deviceService.findSpecificParameters(device, model -> true,
//                                                            p -> Optional.ofNullable(p.getAbilities())
//                                                                    .map(c -> c.contains(Ability.PRODUCE))
//                                                                    .orElse(false)));
//                                            if (produces.parallelStream().map(ParameterEntity::getCode)
//                                                    .anyMatch(it -> dp.getName().equals(it))) {
//                                                final DeviceProduce deviceProduce = new DeviceProduce(device.getCode(),
//                                                        dp.getTime(), Double.parseDouble(dp.getValue().toString()),
//                                                        Double.parseDouble(dp.getValue().toString()));
//                                                influxService.savePointsAsync(deviceProduce);
//                                                log.debug("Write deviceProduce success: {}", deviceProduce);
//                                            }
//                                        } catch (Throwable throwable) {
//                                            log.error("save produce error: {}", throwable.getMessage());
//                                        }
//                                    }))));
                }
            }
        } catch (Throwable throwable) {
            log.error("updateDeviceProduceState: {}", throwable.getMessage());
        }
    }

    @Override
    public void afterPropertiesSet() {
        Optional.ofNullable(influxService).orElseThrow(() -> new InternalException("influxService is null"));
    }
}
