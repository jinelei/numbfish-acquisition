package com.jinelei.numbfish.acquisition.client.influx;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.jinelei.numbfish.acquisition.client.influx.bean.AbstractMessage;
import com.jinelei.numbfish.acquisition.client.influx.bean.DeviceParameter;
import com.jinelei.numbfish.acquisition.client.influx.bean.DeviceProduce;
import com.jinelei.numbfish.acquisition.client.influx.bean.DeviceState;
import com.jinelei.numbfish.acquisition.client.property.AcquisitionProperty;
import com.jinelei.numbfish.common.exception.InvalidArgsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author: jinelei
 * @Description:
 * @Date: 2023/02/10
 * @Version: 1.0.0
 */
@SuppressWarnings("unused")
@Service
@ConditionalOnProperty(value = "numbfish.acquisition.influx2.enabled", havingValue = "true")
public class InfluxService implements DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(InfluxService.class);
    private AcquisitionProperty property;
    private RedisTemplate<String, DeviceState> redisTemplateDeviceState;
    private RedisTemplate<String, DeviceParameter> redisTemplateDeviceParameter;
    private RedisTemplate<String, DeviceProduce> redisTemplateDeviceProduce;
    private final AtomicReference<InfluxDBClient> client = new AtomicReference<>();
    private final Function<Class<?>, String> getCacheKey = k -> {
        Class<?> componentType;
        if (ObjectUtils.isEmpty(k)) {
            throw new InvalidArgsException("The type cannot be empty");
        } else if (k.isArray()) {
            componentType = k.getComponentType();
        } else {
            componentType = k;
        }
        if (DeviceState.class.isAssignableFrom(componentType)) {
            return "DEVICE_STATE_BATCH_SAVE";
        } else if (DeviceParameter.class.isAssignableFrom(componentType)) {
            return "DEVICE_PARAMETER_BATCH_SAVE";
        } else if (DeviceProduce.class.isAssignableFrom(componentType)) {
            return "DEVICE_PRODUCE_BATCH_SAVE";
        } else {
            throw new InvalidArgsException("Unsupported types cache key");
        }
    };
    @SuppressWarnings("rawtypes")
    private final Function<Object, RedisTemplate> getCacheRedis = k -> {
        Class<?> componentType;
        if (ObjectUtils.isEmpty(k)) {
            throw new InvalidArgsException("The type cannot be empty");
        } else if (k.getClass().isArray()) {
            componentType = k.getClass().getComponentType();
        } else {
            componentType = k.getClass();
        }
        if (DeviceState.class.isAssignableFrom(componentType)) {
            return this.redisTemplateDeviceState;
        } else if (DeviceParameter.class.isAssignableFrom(componentType)) {
            return this.redisTemplateDeviceParameter;
        } else if (DeviceProduce.class.isAssignableFrom(componentType)) {
            return this.redisTemplateDeviceProduce;
        } else {
            throw new InvalidArgsException("Unsupported types cache");
        }
    };

    public List<DeviceParameter> queryDeviceParameter(final String deviceCode, final String parameterCode,
                                                      final LocalDateTime startTime, final LocalDateTime stopTime, final Integer limit) {
        final String query = "from(bucket: \"%s\") |> range(start: %s, stop: %s) |> filter(fn: (r) => r[\"_measurement\"] == \"%s_%s\") |> sort(columns: [\"_time\"], desc: true) |> limit(n:%d)"
                .formatted(
                        property.getInflux2().getMeasurements().getDeviceParameter(),
                        startTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        stopTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        deviceCode,
                        parameterCode,
                        Optional.ofNullable(limit).orElse(10));
        return Optional.ofNullable(getClient()).map(InfluxDBClient::getQueryApi)
                .map(q -> q.query(query, property.getInflux2().getOrg(), DeviceParameter.class)).orElse(new ArrayList<>());
    }

    public List<DeviceParameter> queryDeviceParameter(final String deviceCode, final String parameterCode,
                                                      final LocalDateTime startTime, final LocalDateTime stopTime) {
        final String query = "from(bucket: \"%s\") |> range(start: %s, stop: %s) |> filter(fn: (r) => r[\"_measurement\"] == \"%s_%s\")"
                .formatted(
                        property.getInflux2().getMeasurements().getDeviceParameter(),
                        startTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        stopTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        deviceCode,
                        parameterCode);
        return Optional.ofNullable(getClient()).map(InfluxDBClient::getQueryApi)
                .map(q -> q.query(query, property.getInflux2().getOrg(), DeviceParameter.class)).orElse(new ArrayList<>());
    }

    public List<DeviceState> queryDeviceState(final String deviceCode, final LocalDateTime startTime,
                                              final LocalDateTime stopTime) {
        final String query = "from(bucket: \"%s\") |> range(start: %s, stop: %s) |> filter(fn: (r) => r[\"_measurement\"] == \"%s\")"
                .formatted(
                        property.getInflux2().getMeasurements().getDeviceState(),
                        startTime.atOffset(ZoneOffset.ofHours(8)).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        stopTime.atOffset(ZoneOffset.ofHours(8)).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        deviceCode);
        final List<FluxTable> result = getClient().getQueryApi().query(query, property.getInflux2().getOrg());
        final List<DeviceState> collect = new ArrayList<>();
        if (result.size() == 2) {
            for (int j = 0; j < result.get(0).getRecords().size(); j++) {
                final FluxRecord first = result.get(0).getRecords().get(j);
                final FluxRecord second = result.get(1).getRecords().get(j);
                collect.add(new DeviceState().parse(first.getValues(), second.getValues()));
            }
        }
        return collect;
    }

    public Optional<DeviceState> queryLatestDeviceState(final String deviceCode, final LocalDateTime startTime,
                                                        final LocalDateTime stopTime) {
        final String query = "from(bucket: \"%s\") |> range(start: %s, stop: %s) |> filter(fn: (r) => r[\"_measurement\"] == \"%s\") |> sort(columns: [\"_time\"], desc: true) |> limit(n:%d)"
                .formatted(
                        property.getInflux2().getMeasurements().getDeviceState(),
                        startTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        stopTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        deviceCode,
                        1);
        final List<FluxTable> result = getClient().getQueryApi().query(query, property.getInflux2().getOrg());
        final List<DeviceState> collect = new ArrayList<>();
        if (result.size() == 2) {
            for (int j = 0; j < result.get(0).getRecords().size(); j++) {
                final FluxRecord first = result.get(0).getRecords().get(j);
                final FluxRecord second = result.get(1).getRecords().get(j);
                collect.add(new DeviceState().parse(first.getValues(), second.getValues()));
            }
        }
        return collect.stream().findFirst();
    }

    public List<DeviceProduce> queryDeviceProduce(final String deviceCode, final LocalDateTime startTime,
                                                  final LocalDateTime stopTime) {
        final String query = "from(bucket: \"%s\") |> range(start: %s, stop: %s) |> filter(fn: (r) => r[\"_measurement\"] == \"%s\")"
                .formatted(
                        property.getInflux2().getMeasurements().getDeviceProduce(),
                        startTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        stopTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        deviceCode);
        final List<FluxTable> result = getClient().getQueryApi().query(query, property.getInflux2().getOrg());
        final List<DeviceProduce> collect = new ArrayList<>();
        if (result.size() == 2) {
            for (int j = 0; j < result.get(0).getRecords().size(); j++) {
                final FluxRecord first = result.get(0).getRecords().get(j);
                final FluxRecord second = result.get(1).getRecords().get(j);
                collect.add(new DeviceProduce().parse(first.getValues(), second.getValues()));
            }
        }
        return collect;
    }

    public Optional<DeviceProduce> queryLatestDeviceProduce(final String deviceCode, final LocalDateTime startTime,
                                                            final LocalDateTime stopTime) {
        final String query = "from(bucket: \"%s\") |> range(start: %s, stop: %s) |> filter(fn: (r) => r[\"_measurement\"] == \"%s\") |> sort(columns: [\"_time\"], desc: true) |> limit(n:%d)"
                .formatted(
                        property.getInflux2().getMeasurements().getDeviceProduce(),
                        startTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        stopTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        deviceCode,
                        1);
        final List<FluxTable> result = getClient().getQueryApi().query(query, property.getInflux2().getOrg());
        final List<DeviceProduce> collect = new ArrayList<>();
        if (result.size() == 2) {
            for (int j = 0; j < result.get(0).getRecords().size(); j++) {
                final FluxRecord first = result.get(0).getRecords().get(j);
                final FluxRecord second = result.get(1).getRecords().get(j);
                collect.add(new DeviceProduce().parse(first.getValues(), second.getValues()));
            }
        }
        return collect.stream().findFirst();
    }

    public Map<String, Object> queryLatestDeviceParameters(final String deviceCode, final Set<String> variableNames) {
        final Map<String, Object> result = new HashMap<>();
        variableNames.parallelStream().forEach(key -> {
            try {
                final String query = "from(bucket: \"%s\") |> range(start: %s, stop: %s) |> filter(fn: (r) => r[\"_measurement\"] == \"%s_%s\") |> sort(columns: [\"_time\"], desc: true) |> limit(n:%d)"
                        .formatted(
                                property.getInflux2().getMeasurements().getDeviceParameter(),
                                LocalDate.now().atTime(LocalTime.MIN).minusDays(7).atOffset(ZoneOffset.UTC)
                                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                                LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                                deviceCode,
                                key,
                                1);
                final List<FluxTable> table = getClient().getQueryApi().query(query, property.getInflux2().getOrg());
                if (!table.isEmpty()) {
                    table.get(0).getRecords().forEach(fluxRecord -> {
                        final DeviceParameter deviceParameter = new DeviceParameter().parse(fluxRecord.getValues());
                        result.put(deviceParameter.getName(), deviceParameter.getValue());
                    });
                }
            } catch (Throwable throwable) {
                log.error("queryLastDeviceParameters: {}", throwable.getMessage());
            }
        });
        return result;
    }

    public Long queryMeasurementCount(final String bucket, final LocalDateTime startTime, final LocalDateTime stopTime) {
        String query = String.format("from(bucket: \"%s\") |> range(start: %s, stop: %s) |> group() |> count()",
                bucket,
                startTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                stopTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        final List<FluxTable> result = getClient().getQueryApi().query(query, property.getInflux2().getOrg());
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
     * 异步保存测量点
     *
     * @param messages 消息列表
     * @param <T>      类型
     */
    @SuppressWarnings("unchecked")
    @SafeVarargs
    public final <T extends AbstractMessage> void savePointsAsync(T... messages) {
        List<T> list = Arrays.stream(messages).toList();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Optional.ofNullable(getCacheRedis.apply(messages))
                .ifPresent(c -> c.opsForList().rightPushAll(getCacheKey.apply(messages.getClass()), list));
    }

    /**
     * 保存测量点
     *
     * @param messages 测量点
     * @param <T>      类型
     */
    @SafeVarargs
    public final <T extends AbstractMessage> void savePoints(T... messages) {
        List<T> list = Stream.of(messages).toList();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        final Map<String, ? extends List<? extends AbstractMessage>> mapping = list.parallelStream()
                .filter(i -> !ObjectUtils.isEmpty(i.bucket())).collect(Collectors.groupingBy(AbstractMessage::bucket));
        mapping.entrySet().parallelStream().filter(e -> !CollectionUtils.isEmpty(e.getValue())).forEach(entry -> {
            try (WriteApi writeApi = getClient().makeWriteApi()) {
                List<Point> collect = entry.getValue().parallelStream().map(ds -> Point.measurement(ds.measurement())
                        .time(ds.getTime(), WritePrecision.MS)
                        .addTags(ds.tags())
                        .addFields(ds.fields())).collect(Collectors.toList());
                writeApi.writePoints(entry.getKey(), property.getInflux2().getOrg(), collect);
            } catch (Throwable throwable) {
                log.error("Write point batch error: {}, {}", entry, throwable.getMessage());
            }
        });
    }

    public InfluxDBClient getClient() {
        client.compareAndSet(null,
                InfluxDBClientFactory.create(this.property.getInflux2().getUrl(),
                        this.property.getInflux2().getToken().toCharArray()));
        return client.get();
    }

    public void asyncSaveDeviceParameterBatch() {
        try {
            redisTemplateDeviceParameter.executePipelined((RedisCallback<DeviceParameter>) redisConnection -> {
                redisConnection.openPipeline();
                final List<DeviceParameter> range = Optional
                        .ofNullable(
                                redisTemplateDeviceParameter.opsForList().range(getCacheKey.apply(DeviceParameter.class), 0, 10000))
                        .orElse(new ArrayList<>());
                savePoints(range.toArray(DeviceParameter[]::new));
                redisTemplateDeviceParameter.opsForList().trim(getCacheKey.apply(DeviceParameter.class), range.size(), -1);
                redisConnection.closePipeline();
                return null;
            });
        } catch (Throwable throwable) {
            // todo 这里应该处理异常回退
            log.error("save DeviceParameter on redis pipeline failure: {}", throwable.getMessage());
        }
    }

    public void asyncSaveDeviceProduceBatch() {
        try {
            redisTemplateDeviceProduce.executePipelined((RedisCallback<DeviceProduce>) redisConnection -> {
                redisConnection.openPipeline();
                final List<DeviceProduce> range = Optional
                        .ofNullable(
                                redisTemplateDeviceProduce.opsForList().range(getCacheKey.apply(DeviceProduce.class), 0, 10000))
                        .orElse(new ArrayList<>());
                Map<String, List<DeviceProduce>> collect = range.parallelStream()
                        .collect(Collectors.groupingBy(AbstractMessage::getDeviceCode, Collectors.toList()));
                collect.values().forEach(v -> {
                    final List<DeviceProduce> value = v.stream().sorted(Comparator.comparing(AbstractMessage::getTime))
                            .collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(value)) {
                        Optional.of(value).map(it -> it.get(0))
                                .ifPresent(dp -> queryLatestDeviceProduce(dp.getDeviceCode(), LocalDateTime.now().minusYears(1),
                                        LocalDateTime.now()).ifPresent(
                                        produce -> Optional.ofNullable(produce.getDisplay())
                                                .map(i -> dp.getDisplay() - i)
                                                .ifPresentOrElse(dp::setProduce,
                                                        () -> dp.setProduce(dp.getDisplay()))));
                        savePoints(value.toArray(DeviceProduce[]::new));
                    }
                });
                redisTemplateDeviceProduce.opsForList().trim(getCacheKey.apply(DeviceProduce.class), range.size(), -1);
                redisConnection.closePipeline();
                return null;
            });
        } catch (Throwable throwable) {
            // todo 这里应该处理异常回退
            log.error("save DeviceProduce on redis pipeline failure: {}", throwable.getMessage());
        }
    }

    public void asyncSaveDeviceStateBatch() {
        try {
            redisTemplateDeviceState.executePipelined((RedisCallback<DeviceState>) redisConnection -> {
                redisConnection.openPipeline();
                final List<DeviceState> range = Optional
                        .ofNullable(redisTemplateDeviceState.opsForList().range(getCacheKey.apply(DeviceState.class), 0, 10000))
                        .orElse(new ArrayList<>());
                final Map<String, List<DeviceState>> map = range.parallelStream()
                        .sorted(Comparator.comparing(AbstractMessage::getTime))
                        .collect(Collectors.groupingBy(DeviceState::getDeviceCode, Collectors.toList()));
                map.keySet().parallelStream().forEach(key -> {
                    final List<DeviceState> value = map.get(key);
                    if (!CollectionUtils.isEmpty(value)) {
                        final DeviceState latest = value.parallelStream().reduce(null, (request, request2) -> {
                            if (ObjectUtils.isEmpty(request)) {
                                queryLatestDeviceState(request2.getDeviceCode(), LocalDateTime.now().minusYears(1),
                                        LocalDateTime.now())
                                        .flatMap(state -> Optional.ofNullable(state.getTime())
                                                .map(i -> Duration.between(i, request2.getTime()).toMillis())
                                                .filter(i -> i >= 0))
                                        .ifPresent(request2::setDuration);
                            } else {
                                request2.setDuration(Duration.between(request.getTime(), request2.getTime()).toMillis());
                            }
                            return request2;
                        });
                        savePoints(value.toArray(DeviceState[]::new));
                    }
                });
                redisTemplateDeviceState.opsForList().trim(getCacheKey.apply(DeviceState.class), range.size(), -1);
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
        Optional.ofNullable(client).map(AtomicReference::get).ifPresent(InfluxDBClient::close);
    }

}
