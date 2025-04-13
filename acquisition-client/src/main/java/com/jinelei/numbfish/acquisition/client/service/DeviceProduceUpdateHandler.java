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
public class DeviceProduceUpdateHandler implements MessageHandler {
    private static final Logger log = LoggerFactory.getLogger(DeviceProduceUpdateHandler.class);
    public final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 5, 10, TimeUnit.MINUTES,
            new LinkedBlockingQueue<>(1000), r -> new Thread(r, this.getClass().getSimpleName()),
            (r, executor) -> r.run());

    @Override
    public void handleMessage(@NotNull Message<?> message) throws MessagingException {
        try {
            if (message.getPayload() instanceof DeviceParameter dp) {
                if (Number.class.isAssignableFrom(dp.getValue().getClass())) {
                    log.info("DeviceProduceUpdateHandler: {}", dp);
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
}
