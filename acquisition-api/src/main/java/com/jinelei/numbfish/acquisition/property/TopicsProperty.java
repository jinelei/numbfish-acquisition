package com.jinelei.numbfish.acquisition.property;

import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * @Author: jinelei
 * @Description: 订阅字典配置
 * @Date: 2024/3/22 20:03
 * @Version: 1.0.0
 */
@SuppressWarnings("unused")
public class TopicsProperty {
    /**
     * 设备连接配置
     */
    @NotNull(message = "连接配置不合法")
    protected TopicProperty connect;
    /**
     * 设备状态配置
     */
    @NotNull(message = "状态配置不合法")
    protected TopicProperty state;
    /**
     * 设备参数配置
     */
    @NotNull(message = "参数配置不合法")
    protected TopicProperty parameter;
    /**
     * 设备混合消息配置
     */
    @NotNull(message = "混合配置不合法")
    protected TopicProperty mixin;

    public TopicProperty getConnect() {
        return connect;
    }

    public void setConnect(TopicProperty connect) {
        this.connect = connect;
    }

    public TopicProperty getState() {
        return state;
    }

    public void setState(TopicProperty state) {
        this.state = state;
    }

    public TopicProperty getParameter() {
        return parameter;
    }

    public void setParameter(TopicProperty parameter) {
        this.parameter = parameter;
    }

    public TopicProperty getMixin() {
        return mixin;
    }

    public void setMixin(TopicProperty mixin) {
        this.mixin = mixin;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TopicsProperty that = (TopicsProperty) o;
        return Objects.equals(connect, that.connect) && Objects.equals(state, that.state) && Objects.equals(parameter, that.parameter) && Objects.equals(mixin, that.mixin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connect, state, parameter, mixin);
    }

    @Override
    public String toString() {
        return "TopicsProperty{" +
                "connect=" + connect +
                ", state=" + state +
                ", parameter=" + parameter +
                ", mixin=" + mixin +
                '}';
    }
}