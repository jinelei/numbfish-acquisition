package com.jinelei.numbfish.acquisition.property;

import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * @Author: jinelei
 * @Description:
 * @Date: 2024/7/28
 * @Version: 1.0.0
 */
@SuppressWarnings("unused")
public class Influx2Property {
    /**
     * 是否启用
     */
    @NotNull(message = "是否启用不合法")
    protected Boolean enabled;
    /**
     * 连接地址
     */
    @NotNull(message = "url不合法")
    protected String url;
    /**
     * influx token
     */
    @NotNull(message = "token不合法")
    protected String token;
    /**
     * 数据库org
     */
    @NotNull(message = "数据库org不合法")
    protected String org;
    /**
     * 数据库bucket
     */
    @NotNull(message = "数据库bucket不合法")
    protected String bucket;
    /**
     * 数据库定义
     */
    @NotNull(message = "数据库定义不合法")
    protected MeasurementProperty measurements;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public MeasurementProperty getMeasurements() {
        return measurements;
    }

    public void setMeasurements(MeasurementProperty measurements) {
        this.measurements = measurements;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Influx2Property that = (Influx2Property) o;
        return Objects.equals(enabled, that.enabled) && Objects.equals(url, that.url) && Objects.equals(token, that.token) && Objects.equals(org, that.org) && Objects.equals(bucket, that.bucket) && Objects.equals(measurements, that.measurements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, url, token, org, bucket, measurements);
    }

    @Override
    public String toString() {
        return "Influx2Property{" +
                "enabled=" + enabled +
                ", url='" + url + '\'' +
                ", token='" + token + '\'' +
                ", org='" + org + '\'' +
                ", bucket='" + bucket + '\'' +
                ", measurements=" + measurements +
                '}';
    }
}