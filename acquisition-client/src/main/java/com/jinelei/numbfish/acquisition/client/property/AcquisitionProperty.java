package com.jinelei.numbfish.acquisition.client.property;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @Author: jinelei
 * @Description: 综合配置
 * @Date: 2024/3/22 20:03
 * @Version: 1.0.0
 */
@SuppressWarnings("unused")
@Component
@ConfigurationProperties(prefix = "numbfish.acquisition")
public class AcquisitionProperty {
    @NotNull(message = "存储配置不合法")
    protected StoreProperty store;
    @NotNull(message = "MQTT配置不合法")
    protected MqttProperty mqtt;
    @NotNull(message = "influx配置不合法")
    protected Influx2Property influx2;
    @NotNull(message = "别名配置不合法")
    protected AliasProperty alias;
    @NotNull(message = "安全配置不合法")
    protected SecurityProperty security;
    @NotNull(message = "缓存配置不合法")
    protected CacheProperty cache;

    public StoreProperty getStore() {
        return store;
    }

    public void setStore(StoreProperty store) {
        this.store = store;
    }

    public MqttProperty getMqtt() {
        return mqtt;
    }

    public void setMqtt(MqttProperty mqtt) {
        this.mqtt = mqtt;
    }

    public Influx2Property getInflux2() {
        return influx2;
    }

    public void setInflux2(Influx2Property influx2) {
        this.influx2 = influx2;
    }

    public AliasProperty getAlias() {
        return alias;
    }

    public void setAlias(AliasProperty alias) {
        this.alias = alias;
    }

    public SecurityProperty getSecurity() {
        return security;
    }

    public void setSecurity(SecurityProperty security) {
        this.security = security;
    }

    public CacheProperty getCache() {
        return cache;
    }

    public void setCache(CacheProperty cache) {
        this.cache = cache;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AcquisitionProperty that = (AcquisitionProperty) o;
        return Objects.equals(store, that.store) && Objects.equals(mqtt, that.mqtt) && Objects.equals(influx2, that.influx2) && Objects.equals(alias, that.alias) && Objects.equals(security, that.security) && Objects.equals(cache, that.cache);
    }

    @Override
    public int hashCode() {
        return Objects.hash(store, mqtt, influx2, alias, security, cache);
    }

    @Override
    public String toString() {
        return "AcquisitionProperty{" +
                "store=" + store +
                ", mqtt=" + mqtt +
                ", influx2=" + influx2 +
                ", alias=" + alias +
                ", security=" + security +
                ", cache=" + cache +
                '}';
    }
}