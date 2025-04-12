package com.jinelei.numbfish.acquisition.client.mqtt.configuration;

import cn.jinelei.app.dal.mqtt.handler.ExceptionHandler;
import cn.jinelei.app.dal.mqtt.service.MqttService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.StandardIntegrationFlow;

/**
 * @Author: jinelei
 * @Description:
 * @Date: 2023/02/09
 * @Version: 1.0.0
 */
@Configuration
@ConditionalOnBean(MqttService.class)
public class MqttConfiguration {
  private final MqttService mqttService;
  private final ExceptionHandler exceptionHandler;

  public MqttConfiguration(MqttService mqttService, ExceptionHandler exceptionHandler) {
    this.mqttService = mqttService;
    this.exceptionHandler = exceptionHandler;
  }

  @Bean
  public StandardIntegrationFlow standardIntegrationFlow() {
    log.info("MqttConfiguration register standardIntegrationFlow");
    return mqttService.getStandardIntegrationFlow();
  }

  /**
   * 公共消息集成异常处理配置
   *
   * @return org.springframework.integration.dsl.IntegrationFlow
   */
  @Bean
  public IntegrationFlow errorHandlerFlow() {
    log.debug("integration error handler flow");
    return IntegrationFlow.from(IntegrationContextUtils.ERROR_CHANNEL_BEAN_NAME)
        .handle(exceptionHandler)
        .get();
  }
}