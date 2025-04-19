package com.jinelei.numbfish.acquisition.configuration;

import com.jinelei.numbfish.acquisition.influx.InfluxService;
import com.jinelei.numbfish.acquisition.influx.bean.DeviceParameterMessage;
import com.jinelei.numbfish.acquisition.influx.bean.DeviceProduceMessage;
import com.jinelei.numbfish.acquisition.influx.bean.DeviceStateMessage;
import com.jinelei.numbfish.acquisition.property.AcquisitionProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@SuppressWarnings("unused")
@Configuration
public class InfluxConfiguration {

    @Bean
    public InfluxService influxService(@Autowired AcquisitionProperty property,
                                       @Autowired(required = false) RedisTemplate<String, DeviceStateMessage> redisTemplateDeviceState,
                                       @Autowired(required = false) RedisTemplate<String, DeviceParameterMessage> redisTemplateDeviceParameter,
                                       @Autowired(required = false) RedisTemplate<String, DeviceProduceMessage> redisTemplateDeviceProduce) {
        return new InfluxService(property, redisTemplateDeviceState, redisTemplateDeviceParameter, redisTemplateDeviceProduce);
    }

}
