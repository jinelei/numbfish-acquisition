package com.jinelei.numbfish.acquisition.client.mqtt.service;

import cn.jinelei.app.dal.influx.bean.DeviceConnect;
import cn.jinelei.app.dal.influx.bean.DeviceParameter;
import cn.jinelei.app.dal.influx.bean.DeviceState;
import cn.jinelei.app.dal.mqtt.splitter.MixinSplitter;
import cn.jinelei.app.dal.service.*;
import cn.jinelei.app.property.MqttProperty;
import cn.jinelei.app.property.Property;
import cn.jinelei.app.property.TopicProperty;
import cn.jinelei.core.exception.InternalException;
import cn.jinelei.core.exception.InvalidArgsException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.StandardIntegrationFlow;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * @Author: jinelei
 * @Description:
 * @Date: 2024/3/22 20:09
 * @Version: 1.0.0
 */
@Service
@ConditionalOnProperty(value = "a.mqtt.enabled", havingValue = "true")
public class MqttService {
  protected final Property property;
  private final Supplier<MqttConnectOptions> mqttConnectOptionsSupplier;
  private final Supplier<MqttPahoClientFactory> mqttPahoClientFactorySupplier;
  private final Supplier<MqttPahoMessageDrivenChannelAdapter> mqttPahoMessageDrivenChannelAdapterSupplier;
  private final Optional<DeviceActivateStateHandler> deviceActivateStateHandlerOptional;
  private final Optional<DeviceConnectionHandler> deviceConnectionHandlerOptional;
  private final Optional<DeviceAlarmUpdateHandler> deviceAlarmUpdateHandlerOptional;
  private final Optional<DeviceProduceUpdateHandler> deviceProduceUpdateHandlerOptional;
  private final Optional<DeviceParameterHandler> deviceParameterHandlerOptional;
  private final Optional<DeviceStateSaveHandler> deviceStateSaveHandlerOptional;
  private final Optional<MixinSplitter> mixinSplitter;

  public MqttService(Property property,
      Optional<DeviceActivateStateHandler> deviceActivateStateHandlerOptional,
      Optional<DeviceConnectionHandler> deviceConnectionHandlerOptional,
      Optional<DeviceAlarmUpdateHandler> deviceAlarmUpdateHandlerOptional,
      Optional<DeviceProduceUpdateHandler> deviceProduceUpdateHandlerOptional,
      Optional<DeviceParameterHandler> deviceParameterHandlerOptional,
      Optional<DeviceStateSaveHandler> deviceStateSaveHandlerOptional,
      Optional<MixinSplitter> mixinSplitter) {
    this.property = Optional.ofNullable(property).orElseThrow(() -> new InternalException("mqtt配置不合法"));
    this.deviceActivateStateHandlerOptional = deviceActivateStateHandlerOptional;
    this.deviceConnectionHandlerOptional = deviceConnectionHandlerOptional;
    this.deviceAlarmUpdateHandlerOptional = deviceAlarmUpdateHandlerOptional;
    this.deviceProduceUpdateHandlerOptional = deviceProduceUpdateHandlerOptional;
    this.deviceParameterHandlerOptional = deviceParameterHandlerOptional;
    this.deviceStateSaveHandlerOptional = deviceStateSaveHandlerOptional;
    this.mixinSplitter = mixinSplitter;
    this.mqttConnectOptionsSupplier = () -> {
      final MqttConnectOptions options = new MqttConnectOptions();
      options.setServerURIs(new String[] { Optional.ofNullable(this.property.getMqtt().getUrl())
          .orElseThrow(() -> new RuntimeException("mqtt url is null")) });
      options.setUserName(Optional.ofNullable(this.property.getMqtt().getUsername())
          .orElseThrow(() -> new RuntimeException("mqtt username is null")));
      options.setPassword(Optional.ofNullable(this.property.getMqtt().getPassword())
          .orElseThrow(() -> new RuntimeException("mqtt password is null"))
          .toCharArray());
      options.setCleanSession(true);
      options.setKeepAliveInterval(60);
      options.setAutomaticReconnect(true);
      options.setConnectionTimeout(60);
      options.setExecutorServiceTimeout(60);
      return options;
    };
    this.mqttPahoClientFactorySupplier = () -> {
      final DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
      factory.setConnectionOptions(this.mqttConnectOptionsSupplier.get());
      return factory;
    };
    this.mqttPahoMessageDrivenChannelAdapterSupplier = () -> {
      final MqttPahoMessageDrivenChannelAdapter a = new MqttPahoMessageDrivenChannelAdapter(
          this.property.getMqtt().getClientId(), mqttPahoClientFactorySupplier.get());
      a.setCompletionTimeout(60_000);
      a.setSendTimeout(60_000);
      a.setDisconnectCompletionTimeout(30_000);
      a.setConverter(new DefaultPahoMessageConverter());
      a.setQos(1);
      Optional.of(this.property)
          .map(Property::getMqtt)
          .map(MqttProperty::getTopics)
          .ifPresent(topicsProps -> {
            Optional.ofNullable(topicsProps.getConnect())
                .filter(TopicProperty::getEnabled)
                .ifPresent(topic -> a.addTopic(topic.getName(),
                    topic.getQos()));
            Optional.ofNullable(topicsProps.getState())
                .filter(TopicProperty::getEnabled)
                .ifPresent(topic -> a.addTopic(topic.getName(),
                    topic.getQos()));
            Optional.ofNullable(topicsProps.getParameter())
                .filter(TopicProperty::getEnabled)
                .ifPresent(topic -> a.addTopic(topic.getName(),
                    topic.getQos()));
            Optional.ofNullable(topicsProps.getMixin())
                .filter(TopicProperty::getEnabled)
                .ifPresent(topic -> a.addTopic(topic.getName(),
                    topic.getQos()));
          });
      return a;
    };
    log.info("MqttService init success");
  }

  public MqttPahoMessageDrivenChannelAdapter getAdapter() {
    return mqttPahoMessageDrivenChannelAdapterSupplier.get();
  }

  public StandardIntegrationFlow getStandardIntegrationFlow() {
    final MqttPahoMessageDrivenChannelAdapter adapter = getAdapter();
    return IntegrationFlow.from(adapter)
        .split(mixinSplitter.orElseThrow(() -> new InvalidArgsException("消息分割器未配置")))
        .route(
            Message.class,
            message -> message.getPayload().getClass().getSimpleName(),
            spec -> spec
                .subFlowMapping(DeviceConnect.class.getSimpleName(),
                    definition -> definition
                        .wireTap(it -> deviceActivateStateHandlerOptional
                            .ifPresent(it::handle))
                        .wireTap(it -> deviceConnectionHandlerOptional
                            .ifPresent(it::handle))
                        .channel(IntegrationContextUtils.NULL_CHANNEL_BEAN_NAME))
                .subFlowMapping(DeviceState.class.getSimpleName(),
                    definition -> definition
                        .wireTap(it -> deviceStateSaveHandlerOptional
                            .ifPresent(it::handle))
                        .channel(IntegrationContextUtils.NULL_CHANNEL_BEAN_NAME))
                .subFlowMapping(DeviceParameter.class.getSimpleName(),
                    definition -> definition
                        .wireTap(it -> deviceParameterHandlerOptional
                            .ifPresent(it::handle))
                        .wireTap(it -> deviceAlarmUpdateHandlerOptional
                            .ifPresent(it::handle))
                        .wireTap(it -> deviceProduceUpdateHandlerOptional
                            .ifPresent(it::handle))
                        .channel(IntegrationContextUtils.NULL_CHANNEL_BEAN_NAME))
                .defaultOutputToParentFlow())
        .get();
  }
}
