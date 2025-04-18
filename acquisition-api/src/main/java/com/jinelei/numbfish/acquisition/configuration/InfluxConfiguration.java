package com.jinelei.numbfish.acquisition.configuration;

import com.jinelei.numbfish.acquisition.influx.InfluxService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("unused")
@Configuration
public class InfluxConfiguration {

    @Bean
    public InfluxService influxService() {
        return new InfluxService();
    }

}
