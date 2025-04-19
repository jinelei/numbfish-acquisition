package com.jinelei.numbfish.acquisition.influx;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.jinelei.numbfish.acquisition.influx.bean.*;
import com.jinelei.numbfish.acquisition.property.AcquisitionProperty;
import com.jinelei.numbfish.acquisition.property.Influx2Property;
import com.jinelei.numbfish.acquisition.property.MeasurementProperty;
import com.jinelei.numbfish.common.exception.InvalidArgsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @Author: jinelei
 * @Description:
 * @Date: 2023/02/10
 * @Version: 1.0.0
 */
@SuppressWarnings("unused")
@ConditionalOnProperty(value = "numbfish.acquisition.influx2.enabled", havingValue = "true")
public class InfluxService implements InitializingBean, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(InfluxService.class);
    public static final String DEVICE_STATE_BATCH_SAVE = "DEVICE_STATE_BATCH_SAVE";
    public static final String DEVICE_PARAMETER_BATCH_SAVE = "DEVICE_PARAMETER_BATCH_SAVE";
    public static final String DEVICE_PRODUCE_BATCH_SAVE = "DEVICE_PRODUCE_BATCH_SAVE";
    private final Supplier<String> supplierUrl;
    private final Supplier<String> supplierToken;
    private final Supplier<String> supplierOrganization;
    private final Supplier<String> supplierDeviceParameterMeasurement;
    private final Supplier<String> supplierDeviceStateMeasurement;
    private final Supplier<String> supplierDeviceProduceMeasurement;
    private final RedisTemplate<String, DeviceStateMessage> redisTemplateDeviceState;
    private final RedisTemplate<String, DeviceParameterMessage> redisTemplateDeviceParameter;
    private final RedisTemplate<String, DeviceProduceMessage> redisTemplateDeviceProduce;
    private final AtomicReference<InfluxDBClient> client = new AtomicReference<>();

    public InfluxService(final AcquisitionProperty property,
                         final RedisTemplate<String, DeviceStateMessage> redisTemplateDeviceState,
                         final RedisTemplate<String, DeviceParameterMessage> redisTemplateDeviceParameter,
                         final RedisTemplate<String, DeviceProduceMessage> redisTemplateDeviceProduce) {
        Optional.ofNullable(property).orElseThrow(() -> new InvalidArgsException("The property cannot be empty"));
        this.supplierUrl = () -> Optional.of(property).map(AcquisitionProperty::getInflux2).map(Influx2Property::getUrl).orElseThrow(() -> new InvalidArgsException("The url cannot be empty"));
        this.supplierToken = () -> Optional.of(property).map(AcquisitionProperty::getInflux2).map(Influx2Property::getToken).orElseThrow(() -> new InvalidArgsException("The token cannot be empty"));
        this.supplierOrganization = () -> Optional.of(property).map(AcquisitionProperty::getInflux2).map(Influx2Property::getOrg).orElseThrow(() -> new InvalidArgsException("The organization cannot be empty"));
        this.supplierDeviceStateMeasurement = () -> Optional.of(property).map(AcquisitionProperty::getInflux2).map(Influx2Property::getMeasurements).map(MeasurementProperty::getDeviceState).orElseThrow(() -> new InvalidArgsException("The device state measurement cannot be empty"));
        this.supplierDeviceParameterMeasurement = () -> Optional.of(property).map(AcquisitionProperty::getInflux2).map(Influx2Property::getMeasurements).map(MeasurementProperty::getDeviceParameter).orElseThrow(() -> new InvalidArgsException("The device parameter measurement cannot be empty"));
        this.supplierDeviceProduceMeasurement = () -> Optional.of(property).map(AcquisitionProperty::getInflux2).map(Influx2Property::getMeasurements).map(MeasurementProperty::getDeviceProduce).orElseThrow(() -> new InvalidArgsException("The device produce measurement cannot be empty"));
        this.redisTemplateDeviceState = redisTemplateDeviceState;
        this.redisTemplateDeviceParameter = redisTemplateDeviceParameter;
        this.redisTemplateDeviceProduce = redisTemplateDeviceProduce;
    }

    /**
     * 获取客户端
     *
     * @return 客户端
     */
    public InfluxDBClient getClient() {
        client.compareAndSet(null, InfluxDBClientFactory.create(supplierUrl.get(), supplierToken.get().toCharArray()));
        return client.get();
    }

    /**
     * 查询设备状态
     *
     * @param deviceCode    设备编码
     * @param parameterCode 参数编码
     * @param startTime     开始时间
     * @param stopTime      结束时间
     * @param limit         条数
     * @return 查询结果
     */
    public List<DeviceParameterMessage> queryDeviceParameter(String deviceCode, String parameterCode, LocalDateTime startTime, LocalDateTime stopTime, Integer limit) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("from(bucket: \"");
        buffer.append(supplierDeviceParameterMeasurement.get());
        buffer.append("\") ");
        if (Optional.ofNullable(startTime).isPresent() && Optional.ofNullable(stopTime).isPresent()) {
            buffer.append("|> range(");
            buffer.append("start: ");
            buffer.append(startTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            buffer.append(", ");
            buffer.append("stop: ");
            buffer.append(stopTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            buffer.append(") ");
        }
        buffer.append("|> filter(fn: (r) => r[\"_measurement\"] == \"");
        buffer.append(deviceCode);
        buffer.append("_");
        buffer.append(parameterCode);
        buffer.append("\") ");
        buffer.append("|> sort(columns: [\"_time\"], desc: true) ");
        if (Optional.ofNullable(limit).isPresent()) {
            buffer.append(" |> limit(n:");
            buffer.append(limit);
            buffer.append(")");
        }
        return Optional.ofNullable(getClient()).map(InfluxDBClient::getQueryApi)
                .map(q -> q.query(buffer.toString(), supplierOrganization.get(), DeviceParameterMessage.class)).orElse(new ArrayList<>());
    }

    /**
     * 查询设备状态
     *
     * @param deviceCode 设备编码
     * @param startTime  开始时间
     * @param stopTime   结束时间
     * @param limit      条数
     * @return 查询结果
     */
    public List<DeviceStateMessage> queryDeviceState(String deviceCode, LocalDateTime startTime, LocalDateTime stopTime, Integer limit) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("from(bucket: \"");
        buffer.append(supplierDeviceStateMeasurement.get());
        buffer.append("\") ");
        if (Optional.ofNullable(startTime).isPresent() && Optional.ofNullable(stopTime).isPresent()) {
            buffer.append("|> range(");
            buffer.append("start: ");
            buffer.append(startTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            buffer.append(", ");
            buffer.append("stop: ");
            buffer.append(stopTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            buffer.append(") ");
        }
        buffer.append("|> filter(fn: (r) => r[\"_measurement\"] == \"");
        buffer.append(deviceCode);
        buffer.append("\") ");
        buffer.append("|> sort(columns: [\"_time\"], desc: true) ");
        if (Optional.ofNullable(limit).isPresent()) {
            buffer.append(" |> limit(n:");
            buffer.append(limit);
            buffer.append(")");
        }
        final List<FluxTable> result = getClient().getQueryApi().query(buffer.toString(), supplierOrganization.get());
        final List<DeviceStateMessage> collect = new ArrayList<>();
        if (result.size() == 2) {
            for (int j = 0; j < result.get(0).getRecords().size(); j++) {
                final FluxRecord first = result.get(0).getRecords().get(j);
                final FluxRecord second = result.get(1).getRecords().get(j);
                collect.add(new DeviceStateMessage().parse(first.getValues(), second.getValues()));
            }
        }
        return collect;
    }

    /**
     * 查询设备产量
     *
     * @param deviceCode 设备编码
     * @param startTime  开始时间
     * @param stopTime   结束时间
     * @param limit      条数
     * @return 查询结果
     */
    public List<DeviceProduceMessage> queryDeviceProduce(String deviceCode, LocalDateTime startTime, LocalDateTime stopTime, Integer limit) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("from(bucket: \"");
        buffer.append(supplierDeviceProduceMeasurement.get());
        buffer.append("\") ");
        if (Optional.ofNullable(startTime).isPresent() && Optional.ofNullable(stopTime).isPresent()) {
            buffer.append("|> range(");
            buffer.append("start: ");
            buffer.append(startTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            buffer.append(", ");
            buffer.append("stop: ");
            buffer.append(stopTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            buffer.append(") ");
        }
        buffer.append("|> filter(fn: (r) => r[\"_measurement\"] == \"");
        buffer.append(deviceCode);
        buffer.append("\") ");
        buffer.append("|> sort(columns: [\"_time\"], desc: true) ");
        if (Optional.ofNullable(limit).isPresent()) {
            buffer.append(" |> limit(n:");
            buffer.append(limit);
            buffer.append(")");
        }
        final List<FluxTable> result = getClient().getQueryApi().query(buffer.toString(), supplierOrganization.get());
        final List<DeviceProduceMessage> collect = new ArrayList<>();
        if (result.size() == 2) {
            for (int j = 0; j < result.get(0).getRecords().size(); j++) {
                final FluxRecord first = result.get(0).getRecords().get(j);
                final FluxRecord second = result.get(1).getRecords().get(j);
                collect.add(new DeviceProduceMessage().parse(first.getValues(), second.getValues()));
            }
        }
        return collect;
    }

    /**
     * 查询测量点数量
     *
     * @param bucket    bucket
     * @param startTime 开始时间
     * @param stopTime  结束时间
     * @return 查询结果
     */
    public Long queryMeasurementCount(final String bucket, final LocalDateTime startTime, final LocalDateTime stopTime) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("from(bucket: \"");
        buffer.append(bucket);
        buffer.append("\") ");
        if (Optional.ofNullable(startTime).isPresent() && Optional.ofNullable(stopTime).isPresent()) {
            buffer.append("|> range(");
            buffer.append("start: ");
            buffer.append(startTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            buffer.append(", ");
            buffer.append("stop: ");
            buffer.append(stopTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            buffer.append(") ");
        }
        buffer.append("|> group() ");
        buffer.append("|> count()");
        final List<FluxTable> result = getClient().getQueryApi().query(buffer.toString(), supplierOrganization.get());
        final AtomicLong count = new AtomicLong(0);
        result.forEach(fluxTable -> Optional.ofNullable(fluxTable)
                .map(FluxTable::getRecords)
                .stream()
                .flatMap(Collection::stream)
                .forEach(fluxRecord -> Optional.ofNullable(fluxRecord)
                        .map(FluxRecord::getValues)
                        .map(m -> m.get("_value"))
                        .map(Object::toString)
                        .map(Long::parseLong)
                        .ifPresent(count::set)));
        return count.get();
    }

    /**
     * 批量保存设备状态
     *
     * @param messages 消息列表
     * @param isAsync  是否异步
     */
    public final void saveDeviceStateMessages(List<DeviceStateMessage> messages, Boolean isAsync) {
        if (Optional.of(messages).map(List::isEmpty).orElse(true)) {
            return;
        }
        if (Optional.ofNullable(isAsync).orElse(false)) {
            redisTemplateDeviceState.opsForList().rightPushAll(DEVICE_STATE_BATCH_SAVE, messages);
        } else {
            final Map<String, List<DeviceStateMessage>> mapping = messages.parallelStream()
                    .filter(i -> !ObjectUtils.isEmpty(i.bucket())).collect(Collectors.groupingBy(DeviceStateMessage::bucket));
            mapping.entrySet().parallelStream().filter(e -> !CollectionUtils.isEmpty(e.getValue())).forEach(entry -> {
                try (WriteApi writeApi = getClient().makeWriteApi()) {
                    List<Point> collect = entry.getValue().parallelStream().map(ds -> Point.measurement(ds.measurement())
                            .time(ds.getTime(), WritePrecision.MS)
                            .addTags(ds.tags())
                            .addFields(ds.fields())).collect(Collectors.toList());
                    writeApi.writePoints(entry.getKey(), supplierOrganization.get(), collect);
                } catch (Throwable throwable) {
                    log.error("Write DeviceStateMessage batch error: {}, {}", entry, throwable.getMessage());
                }
            });
        }
    }

    /**
     * 批量保存设备参数
     *
     * @param messages 消息列表
     * @param isAsync  是否异步
     */
    public final void saveDeviceParameterMessages(List<DeviceParameterMessage> messages, Boolean isAsync) {
        if (Optional.of(messages).map(List::isEmpty).orElse(true)) {
            return;
        }
        if (Optional.ofNullable(isAsync).orElse(false)) {
            redisTemplateDeviceParameter.opsForList().rightPushAll(DEVICE_PARAMETER_BATCH_SAVE, messages);
        } else {
            final Map<String, List<DeviceParameterMessage>> mapping = messages.parallelStream()
                    .filter(i -> !ObjectUtils.isEmpty(i.bucket())).collect(Collectors.groupingBy(DeviceParameterMessage::bucket));
            mapping.entrySet().parallelStream().filter(e -> !CollectionUtils.isEmpty(e.getValue())).forEach(entry -> {
                try (WriteApi writeApi = getClient().makeWriteApi()) {
                    List<Point> collect = entry.getValue().parallelStream().map(ds -> Point.measurement(ds.measurement())
                            .time(ds.getTime(), WritePrecision.MS)
                            .addTags(ds.tags())
                            .addFields(ds.fields())).collect(Collectors.toList());
                    writeApi.writePoints(entry.getKey(), supplierOrganization.get(), collect);
                } catch (Throwable throwable) {
                    log.error("Write DeviceParameterMessage batch error: {}, {}", entry, throwable.getMessage());
                }
            });
        }
    }

    /**
     * 批量保存设备产量
     *
     * @param messages 消息列表
     * @param isAsync  是否异步
     */
    public final void saveDeviceProduceMessages(List<DeviceProduceMessage> messages, Boolean isAsync) {
        if (Optional.ofNullable(isAsync).orElse(false)) {
            redisTemplateDeviceProduce.opsForList().rightPushAll(DEVICE_PRODUCE_BATCH_SAVE, messages);
        } else {
            final Map<String, List<DeviceProduceMessage>> mapping = messages.parallelStream()
                    .filter(i -> !ObjectUtils.isEmpty(i.bucket())).collect(Collectors.groupingBy(DeviceProduceMessage::bucket));
            mapping.entrySet().parallelStream().filter(e -> !CollectionUtils.isEmpty(e.getValue())).forEach(entry -> {
                try (WriteApi writeApi = getClient().makeWriteApi()) {
                    List<Point> collect = entry.getValue().parallelStream().map(ds -> Point.measurement(ds.measurement())
                            .time(ds.getTime(), WritePrecision.MS)
                            .addTags(ds.tags())
                            .addFields(ds.fields())).collect(Collectors.toList());
                    writeApi.writePoints(entry.getKey(), supplierOrganization.get(), collect);
                } catch (Throwable throwable) {
                    log.error("Write DeviceProduceMessage batch error: {}, {}", entry, throwable.getMessage());
                }
            });
        }
    }

    /**
     * 任务批量保存设备参数
     */
    public void taskBatchSaveDeviceParameter() {
        try {
            redisTemplateDeviceParameter.executePipelined((RedisCallback<DeviceParameterMessage>) redisConnection -> {
                redisConnection.openPipeline();
                final List<DeviceParameterMessage> range = Optional
                        .ofNullable(
                                redisTemplateDeviceParameter.opsForList().range(DEVICE_PARAMETER_BATCH_SAVE, 0, 10000))
                        .orElse(new ArrayList<>());
                saveDeviceParameterMessages(range, false);
                redisTemplateDeviceParameter.opsForList().trim(DEVICE_PARAMETER_BATCH_SAVE, range.size(), -1);
                redisConnection.closePipeline();
                return null;
            });
        } catch (Throwable throwable) {
            // todo 这里应该处理异常回退
            log.error("save DeviceParameter on redis pipeline failure: {}", throwable.getMessage());
        }
    }

    /**
     * 任务批量保存设备产量
     */
    public void taskBatchSaveDeviceProduce() {
        try {
            redisTemplateDeviceProduce.executePipelined((RedisCallback<DeviceProduceMessage>) redisConnection -> {
                redisConnection.openPipeline();
                final List<DeviceProduceMessage> range = Optional
                        .ofNullable(
                                redisTemplateDeviceProduce.opsForList().range(DEVICE_PRODUCE_BATCH_SAVE, 0, 10000))
                        .orElse(new ArrayList<>());
                Map<String, List<DeviceProduceMessage>> collect = range.parallelStream()
                        .collect(Collectors.groupingBy(AbstractMessage::getDeviceCode, Collectors.toList()));
                collect.values().forEach(v -> {
                    final List<DeviceProduceMessage> value = v.stream().sorted(Comparator.comparing(AbstractMessage::getTime))
                            .collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(value)) {
                        Optional.of(value).map(List::getFirst)
                                .ifPresent(dp -> queryDeviceProduce(dp.getDeviceCode(), LocalDateTime.now().minusYears(1), LocalDateTime.now(), 1)
                                        .stream()
                                        .map(DeviceProduceMessage::getDisplay)
                                        .map(i -> dp.getDisplay() - i)
                                        .findFirst()
                                        .ifPresentOrElse(dp::setProduce, () -> dp.setProduce(dp.getDisplay())));
                        saveDeviceProduceMessages(value, false);
                    }
                });
                redisTemplateDeviceProduce.opsForList().trim(DEVICE_PRODUCE_BATCH_SAVE, range.size(), -1);
                redisConnection.closePipeline();
                return null;
            });
        } catch (Throwable throwable) {
            // todo 这里应该处理异常回退
            log.error("save DeviceProduce on redis pipeline failure: {}", throwable.getMessage());
        }
    }

    /**
     * 任务批量保存设备状态
     */
    public void taskBatchSaveDeviceState() {
        try {
            redisTemplateDeviceState.executePipelined((RedisCallback<DeviceStateMessage>) redisConnection -> {
                redisConnection.openPipeline();
                final List<DeviceStateMessage> range = Optional
                        .ofNullable(redisTemplateDeviceState.opsForList().range(DEVICE_STATE_BATCH_SAVE, 0, 10000))
                        .orElse(new ArrayList<>());
                final Map<String, List<DeviceStateMessage>> map = range.parallelStream()
                        .sorted(Comparator.comparing(AbstractMessage::getTime))
                        .collect(Collectors.groupingBy(DeviceStateMessage::getDeviceCode, Collectors.toList()));
                map.keySet().parallelStream().forEach(key -> {
                    final List<DeviceStateMessage> value = map.get(key);
                    if (!CollectionUtils.isEmpty(value)) {
                        final DeviceStateMessage latest = value.parallelStream().reduce(null, (request, request2) -> {
                            if (ObjectUtils.isEmpty(request)) {
                                queryDeviceState(request2.getDeviceCode(), LocalDateTime.now().minusYears(1), LocalDateTime.now(), 1)
                                        .stream()
                                        .map(AbstractMessage::getTime)
                                        .map(i -> Duration.between(i, request2.getTime()).toMillis())
                                        .forEach(request2::setDuration);
                            } else {
                                request2.setDuration(Duration.between(request.getTime(), request2.getTime()).toMillis());
                            }
                            return request2;
                        });
                        saveDeviceStateMessages(value, false);
                    }
                });
                redisTemplateDeviceState.opsForList().trim(DEVICE_STATE_BATCH_SAVE, range.size(), -1);
                redisConnection.closePipeline();
                return null;
            });
        } catch (Throwable throwable) {
            // todo 这里应该处理异常回退
            log.error("save DeviceState on redis pipeline failure: {}", throwable.getMessage());
        }
    }

    @Override
    public void destroy() {
        Optional.of(client).map(AtomicReference::get).ifPresent(InfluxDBClient::close);
    }

    @Override
    public void afterPropertiesSet() {
        log.info("InfluxService init success");
    }
}
