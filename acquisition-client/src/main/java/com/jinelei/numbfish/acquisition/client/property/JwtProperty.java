package com.jinelei.numbfish.acquisition.client.property;

import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * @Author: jinelei
 * @Description:
 * @Date: 2024/8/1 17:20
 * @Version: 1.0.0
 */
@SuppressWarnings("unused")
public class JwtProperty {
    /**
     * jwt的key
     */
    @NotNull(message = "key配置不合法")
    protected String key;
    /**
     * jwt验证使用的header名称
     */
    @NotNull(message = "头信息不合法")
    protected String header;
    /**
     * jwt登录地址
     */
    @NotNull(message = "登陆url不合法")
    protected String url;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        JwtProperty that = (JwtProperty) o;
        return Objects.equals(key, that.key) && Objects.equals(header, that.header) && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, header, url);
    }

    @Override
    public String toString() {
        return "JwtProperty{" +
                "key='" + key + '\'' +
                ", header='" + header + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}