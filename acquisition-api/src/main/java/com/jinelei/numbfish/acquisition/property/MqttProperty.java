package com.jinelei.numbfish.acquisition.property;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * @Author: jinelei
 * @Description:
 * @Date: 2024/8/1 17:16
 * @Version: 1.0.0
 */
@SuppressWarnings("unused")
public class MqttProperty {
    /**
     * 是否启用
     */
    @NotNull(message = "是否启用不合法")
    protected Boolean enabled;
    /**
     * 用户名称
     */
    @NotNull(message = "用户名不合法")
    protected String username;
    /**
     * 密码
     */
    @NotNull(message = "密码不合法")
    protected String password;
    /**
     * 连接地址
     */
    @NotNull(message = "连接地址不合法")
    protected String url;
    /**
     * 客户端id
     */
    @NotNull(message = "客户端id不合法")
    protected String clientId;
    /**
     * qos
     */
    @NotNull(message = "qos不存在")
    @Max(value = 2, message = "qos不能大于2")
    @Min(value = 0, message = "qos不能小于0")
    protected Integer qos;
    /**
     * 订阅的topic列表
     */
    @NotNull(message = "订阅配置不合法")
    protected TopicsProperty topics;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Integer getQos() {
        return qos;
    }

    public void setQos(Integer qos) {
        this.qos = qos;
    }

    public TopicsProperty getTopics() {
        return topics;
    }

    public void setTopics(TopicsProperty topics) {
        this.topics = topics;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MqttProperty that = (MqttProperty) o;
        return Objects.equals(enabled, that.enabled) && Objects.equals(username, that.username) && Objects.equals(password, that.password) && Objects.equals(url, that.url) && Objects.equals(clientId, that.clientId) && Objects.equals(qos, that.qos) && Objects.equals(topics, that.topics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, username, password, url, clientId, qos, topics);
    }

    @Override
    public String toString() {
        return "MqttProperty{" +
                "enabled=" + enabled +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", url='" + url + '\'' +
                ", clientId='" + clientId + '\'' +
                ", qos=" + qos +
                ", topics=" + topics +
                '}';
    }
}