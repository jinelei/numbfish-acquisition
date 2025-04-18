package com.jinelei.numbfish.acquisition.client.mqtt.configuration;

import com.jinelei.numbfish.acquisition.client.mqtt.handler.ExceptionHandler;
import com.jinelei.numbfish.acquisition.client.mqtt.splitter.MixinSplitter;
import com.jinelei.numbfish.acquisition.client.service.*;
import com.jinelei.numbfish.acquisition.influx.bean.DeviceConnectMessage;
import com.jinelei.numbfish.acquisition.influx.bean.DeviceParameterMessage;
import com.jinelei.numbfish.acquisition.influx.bean.DeviceStateMessage;
import com.jinelei.numbfish.acquisition.property.AcquisitionProperty;
import com.jinelei.numbfish.acquisition.property.MqttProperty;
import com.jinelei.numbfish.acquisition.property.TopicProperty;
import com.jinelei.numbfish.common.exception.InvalidArgsException;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.StandardIntegrationFlow;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;

import java.util.Optional;

/**
 * @Author: jinelei
 * @Description:
 * @Date: 2023/02/09
 * @Version: 1.0.0
 */
@SuppressWarnings("unused")
@Configuration
@ConditionalOnProperty(name = "numbfish.acquisition.mqtt.enabled", havingValue = "true")
public class MqttConfiguration {
    private static final Logger log = LoggerFactory.getLogger(MqttConfiguration.class);
    private final AcquisitionProperty property;
    private final ExceptionHandler exceptionHandler;

    public MqttConfiguration(AcquisitionProperty property, ExceptionHandler exceptionHandler) {
        this.property = property;
        this.exceptionHandler = exceptionHandler;
    }

    @Bean
    public StandardIntegrationFlow standardIntegrationFlow(
            @Autowired(required = false) MixinSplitter mixinSplitter,
            @Autowired(required = false) DeviceActivateHandler deviceActivateHandler,
            @Autowired(required = false) DeviceConnectionHandler deviceConnectionHandler,
            @Autowired(required = false) DeviceAlarmHandler deviceAlarmHandler,
            @Autowired(required = false) DeviceProduceHandler deviceProduceHandler,
            @Autowired(required = false) DeviceParameterHandler deviceParameterHandler,
            @Autowired(required = false) DeviceStateHandler deviceStateHandler
    ) {
        log.info("MqttConfiguration register standardIntegrationFlow");
        final MqttPahoMessageDrivenChannelAdapter adapter = getMqttPahoMessageDrivenChannelAdapter();
        Optional.of(this.property)
                .map(AcquisitionProperty::getMqtt)
                .map(MqttProperty::getTopics)
                .ifPresent(topicsProps -> {
                    Optional.ofNullable(topicsProps.getConnect())
                            .filter(TopicProperty::getEnabled)
                            .ifPresent(topic -> adapter.addTopic(topic.getName(),
                                    topic.getQos()));
                    Optional.ofNullable(topicsProps.getState())
                            .filter(TopicProperty::getEnabled)
                            .ifPresent(topic -> adapter.addTopic(topic.getName(),
                                    topic.getQos()));
                    Optional.ofNullable(topicsProps.getParameter())
                            .filter(TopicProperty::getEnabled)
                            .ifPresent(topic -> adapter.addTopic(topic.getName(),
                                    topic.getQos()));
                    Optional.ofNullable(topicsProps.getMixin())
                            .filter(TopicProperty::getEnabled)
                            .ifPresent(topic -> adapter.addTopic(topic.getName(),
                                    topic.getQos()));
                });
        return IntegrationFlow.from(adapter)
                .split(Optional.ofNullable(mixinSplitter).orElseThrow(() -> new InvalidArgsException("消息分割器未配置")))
                .route(
                        Message.class,
                        message -> message.getPayload().getClass().getSimpleName(),
                        spec -> spec
                                .subFlowMapping(DeviceConnectMessage.class.getSimpleName(),
                                        definition -> definition
                                                .wireTap(it -> Optional.ofNullable(deviceActivateHandler)
                                                        .ifPresent(it::handle))
                                                .wireTap(it -> Optional.ofNullable(deviceConnectionHandler)
                                                        .ifPresent(it::handle))
                                                .channel(IntegrationContextUtils.NULL_CHANNEL_BEAN_NAME))
                                .subFlowMapping(DeviceStateMessage.class.getSimpleName(),
                                        definition -> definition
                                                .wireTap(it -> Optional.ofNullable(deviceStateHandler)
                                                        .ifPresent(it::handle))
                                                .channel(IntegrationContextUtils.NULL_CHANNEL_BEAN_NAME))
                                .subFlowMapping(DeviceParameterMessage.class.getSimpleName(),
                                        definition -> definition
                                                .wireTap(it -> Optional.ofNullable(deviceParameterHandler)
                                                        .ifPresent(it::handle))
                                                .wireTap(it -> Optional.ofNullable(deviceAlarmHandler)
                                                        .ifPresent(it::handle))
                                                .wireTap(it -> Optional.ofNullable(deviceProduceHandler)
                                                        .ifPresent(it::handle))
                                                .channel(IntegrationContextUtils.NULL_CHANNEL_BEAN_NAME))
                                .defaultOutputToParentFlow())
                .get();
    }

    @NotNull
    private MqttPahoMessageDrivenChannelAdapter getMqttPahoMessageDrivenChannelAdapter() {
        final MqttConnectOptions options = getMqttConnectOptions();
        final DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(options);
        final MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(this.property.getMqtt().getClientId(), factory);
        adapter.setCompletionTimeout(60_000);
        adapter.setSendTimeout(60_000);
        adapter.setDisconnectCompletionTimeout(30_000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        return adapter;
    }

    @NotNull
    private MqttConnectOptions getMqttConnectOptions() {
        final MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{Optional.ofNullable(this.property.getMqtt().getUrl())
                .orElseThrow(() -> new RuntimeException("mqtt url is null"))});
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