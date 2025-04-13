package com.jinelei.numbfish.acquisition.client.mqtt.splitter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinelei.numbfish.acquisition.client.influx.bean.DeviceConnectMessage;
import com.jinelei.numbfish.acquisition.client.influx.bean.DeviceParameterMessage;
import com.jinelei.numbfish.acquisition.client.influx.bean.DeviceStateMessage;
import com.jinelei.numbfish.acquisition.client.property.AcquisitionProperty;
import com.jinelei.numbfish.common.exception.InvalidArgsException;
import com.jinelei.numbfish.common.helper.EnumerationHelper;
import com.jinelei.numbfish.device.enumeration.EventType;
import com.jinelei.numbfish.device.enumeration.RunningState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.splitter.AbstractMessageSplitter;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @Author: jinelei
 * @Description:
 * @Date: 2024/7/16
 * @Version: 1.0.0
 */
@Service
public class MixinSplitter extends AbstractMessageSplitter {
    private static final Logger log = LoggerFactory.getLogger(MixinSplitter.class);
    private final ObjectMapper objectMapper;
    private final AcquisitionProperty property;

    public MixinSplitter(ObjectMapper objectMapper, AcquisitionProperty property) {
        this.objectMapper = objectMapper;
        this.property = property;
    }

    @Override
    protected Object splitMessage(Message<?> message) {
        Object payload = message.getPayload();
        List<Object> result = new ArrayList<>();
        if (payload instanceof String s) {
            try {
                final List<String> fields = List.of(
                        property.getAlias().getDeviceCode(),
                        property.getAlias().getTimestamp(),
                        property.getAlias().getEvent());
                final JsonNode node = objectMapper.readTree(s);
                final String deviceCode = Optional.ofNullable(node.get(property.getAlias().getDeviceCode()).asText())
                        .orElseThrow(() -> new InvalidArgsException("deviceCode不合法"));
                final Instant instant = Optional.ofNullable(node.get(property.getAlias().getTimestamp()))
                        .map(n -> switch (n.getNodeType()) {
                            case NUMBER -> Optional.of(n.asLong()).map(Instant::ofEpochMilli)
                                    .orElseThrow(() -> new InvalidArgsException("时间戳不合法"));
                            case STRING -> Optional.of(n.asText()).filter(StringUtils::hasLength).map(Instant::parse)
                                    .orElseThrow(() -> new InvalidArgsException("时间戳不合法"));
                            case MISSING -> throw new InvalidArgsException("时间戳不存在");
                            default -> throw new InvalidArgsException("不支持的数据类型");
                        }).orElseThrow();
                Optional.ofNullable(node.get(property.getAlias().getEvent()))
                        .filter(JsonNode::isTextual)
                        .map(JsonNode::asText)
                        .filter(StringUtils::hasLength)
                        .map(i -> EnumerationHelper.parseFrom(EventType.class, i))
                        .ifPresent(value -> {
                            final DeviceConnectMessage deviceConnectMessage = new DeviceConnectMessage();
                            deviceConnectMessage.setDeviceCode(deviceCode);
                            deviceConnectMessage.setTime(instant);
                            deviceConnectMessage.setEvent(value);
                            result.add(deviceConnectMessage);
                        });
                Optional.ofNullable(node.get(property.getAlias().getState()))
                        .map(n -> switch (n.getNodeType()) {
                            case NUMBER ->
                                    Optional.of(n.asInt()).map(i -> EnumerationHelper.parseFrom(RunningState.class, i))
                                            .orElseThrow(() -> new InvalidArgsException("运行状态不合法"));
                            case STRING ->
                                    Optional.of(n.asText()).map(i -> EnumerationHelper.parseFrom(RunningState.class, i))
                                            .orElseThrow(() -> new InvalidArgsException("运行状态不合法"));
                            case MISSING, NULL, POJO, ARRAY, BINARY, OBJECT, BOOLEAN ->
                                    throw new InvalidArgsException("运行状态不支持的类型");
                        }).ifPresent(value -> {
                            final DeviceStateMessage deviceStateMessage = new DeviceStateMessage();
                            deviceStateMessage.setDeviceCode(deviceCode);
                            deviceStateMessage.setTime(instant);
                            deviceStateMessage.setState(value);
                            deviceStateMessage.setDuration(0L);
                            deviceStateMessage.setState(value);
                            result.add(deviceStateMessage);
                        });
                node.fields().forEachRemaining(entry -> {
                    if (!fields.contains(entry.getKey())) {
                        Optional.ofNullable(entry.getValue()).map(JsonNode::getNodeType).ifPresent(type -> {
                            Optional<DeviceParameterMessage> optional = switch (type) {
                                case NUMBER ->
                                        Optional.of(new DeviceParameterMessage(deviceCode, instant, entry.getKey(), entry.getValue().asLong()));
                                case STRING ->
                                        Optional.of(new DeviceParameterMessage(deviceCode, instant, entry.getKey(), entry.getValue().asText()));
                                case BOOLEAN ->
                                        Optional.of(new DeviceParameterMessage(deviceCode, instant, entry.getKey(), entry.getValue().asBoolean()));
                                case ARRAY ->
                                        Optional.of(new DeviceParameterMessage(deviceCode, instant, entry.getKey(), entry.getValue().toString()));
                                case OBJECT, POJO, BINARY -> Optional
                                        .of(new DeviceParameterMessage(deviceCode, instant, entry.getKey(), entry.getValue().toPrettyString()));
                                case MISSING, NULL -> Optional.empty();
                            };
                            optional.ifPresent(result::add);
                        });
                    }
                });
            } catch (JsonProcessingException e) {
                log.error("message convert failure: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        }
        log.debug("message convert success: {}", result.size());
        return result.stream().map(MessageBuilder::withPayload).map(MessageBuilder::build).toList();
    }
}
