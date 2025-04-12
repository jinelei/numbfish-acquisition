package com.jinelei.numbfish.acquisition.client.mqtt.splitter;

import cn.jinelei.app.dal.influx.bean.DeviceConnect;
import cn.jinelei.app.dal.influx.bean.DeviceParameter;
import cn.jinelei.app.dal.influx.bean.DeviceState;
import cn.jinelei.app.enumrica.EventType;
import cn.jinelei.app.enumrica.RunningState;
import cn.jinelei.app.property.Property;
import cn.jinelei.core.exception.InvalidArgsException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
public class MixinSplitter extends AbstractMessageSplitter {
  private final ObjectMapper objectMapper;
  private final Property property;

  public MixinSplitter(ObjectMapper objectMapper, Property property) {
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
              case NUMBER ->
                Optional.of(n.asLong()).map(Instant::ofEpochMilli)
                    .orElseThrow(() -> new InvalidArgsException("时间戳不合法"));
              case STRING ->
                Optional.of(n.asText()).filter(StringUtils::hasLength).map(Instant::parse)
                    .orElseThrow(() -> new InvalidArgsException("时间戳不合法"));
              case MISSING -> throw new InvalidArgsException("时间戳不存在");
              default -> throw new InvalidArgsException("不支持的数据类型");
            }).orElseThrow();
        Optional.ofNullable(node.get(property.getAlias().getEvent()))
            .filter(JsonNode::isTextual)
            .map(JsonNode::asText)
            .filter(StringUtils::hasLength)
            .map(EventType::parseFrom)
            .ifPresent(value -> {
              final DeviceConnect deviceConnect = new DeviceConnect();
              deviceConnect.setDeviceCode(deviceCode);
              deviceConnect.setTime(instant);
              deviceConnect.setEvent(value);
              result.add(deviceConnect);
            });
        Optional.ofNullable(node.get(property.getAlias().getState()))
            .map(n -> switch (n.getNodeType()) {
              case NUMBER ->
                Optional.of(n.asInt()).map(RunningState::parseFrom)
                    .orElseThrow(() -> new InvalidArgsException("运行状态不合法"));
              case STRING ->
                Optional.of(n.asText()).map(RunningState::parseFrom)
                    .orElseThrow(() -> new InvalidArgsException("运行状态不合法"));
              case MISSING, NULL, POJO, ARRAY, BINARY, OBJECT, BOOLEAN ->
                throw new InvalidArgsException("运行状态不支持的类型");
            }).ifPresent(value -> {
              final DeviceState deviceState = new DeviceState();
              deviceState.setDeviceCode(deviceCode);
              deviceState.setTime(instant);
              deviceState.setState(value);
              deviceState.setDuration(0L);
              deviceState.setState(value);
              result.add(deviceState);
            });
        node.fields().forEachRemaining(entry -> {
          if (!fields.contains(entry.getKey())) {
            Optional.ofNullable(entry.getValue()).map(JsonNode::getNodeType).ifPresent(type -> {
              Optional<DeviceParameter> optional = switch (type) {
                case NUMBER ->
                  Optional.of(new DeviceParameter(deviceCode, instant, entry.getKey(), entry.getValue().asLong()));
                case STRING ->
                  Optional.of(new DeviceParameter(deviceCode, instant, entry.getKey(), entry.getValue().asText()));
                case BOOLEAN ->
                  Optional.of(new DeviceParameter(deviceCode, instant, entry.getKey(), entry.getValue().asBoolean()));
                case ARRAY ->
                  Optional.of(new DeviceParameter(deviceCode, instant, entry.getKey(), entry.getValue().toString()));
                case OBJECT, POJO, BINARY ->
                  Optional
                      .of(new DeviceParameter(deviceCode, instant, entry.getKey(), entry.getValue().toPrettyString()));
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
