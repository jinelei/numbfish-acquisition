package com.jinelei.numbfish.acquisition.property;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * @Author: jinelei
 * @Description:
 * @Date: 2024/8/1 17:24
 * @Version: 1.0.0
 */
@SuppressWarnings("unused")
public class TopicProperty {
    /**
     * 是否启用
     */
    @NotNull(message = "不合法")
    protected Boolean enabled;
    /**
     * topic名称
     */
    @NotNull(message = "不合法")
    protected String name;
    /**
     * qos
     */
    @NotNull(message = "不合法")
    @Max(value = 2, message = "不能大于2")
    @Min(value = 0, message = "不能小于0")
    protected Integer qos;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQos() {
        return qos;
    }

    public void setQos(Integer qos) {
        this.qos = qos;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TopicProperty that = (TopicProperty) o;
        return Objects.equals(enabled, that.enabled) && Objects.equals(name, that.name) && Objects.equals(qos, that.qos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, name, qos);
    }

    @Override
    public String toString() {
        return "TopicProperty{" +
                "enabled=" + enabled +
                ", name='" + name + '\'' +
                ", qos=" + qos +
                '}';
    }
}