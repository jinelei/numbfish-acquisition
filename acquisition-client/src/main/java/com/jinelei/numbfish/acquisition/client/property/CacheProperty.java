package com.jinelei.numbfish.acquisition.client.property;

import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * @Author: jinelei
 * @Description: 缓存配置
 * @Date: 2024/7/28
 * @Version: 1.0.0
 */
@SuppressWarnings("unused")
public class CacheProperty {
    @NotNull(message = "高频缓存")
    protected Integer high;
    @NotNull(message = "中频缓存")
    protected Integer media;
    @NotNull(message = "低频缓存")
    protected Integer low;

    public Integer getHigh() {
        return high;
    }

    public void setHigh(Integer high) {
        this.high = high;
    }

    public Integer getMedia() {
        return media;
    }

    public void setMedia(Integer media) {
        this.media = media;
    }

    public Integer getLow() {
        return low;
    }

    public void setLow(Integer low) {
        this.low = low;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CacheProperty that = (CacheProperty) o;
        return Objects.equals(high, that.high) && Objects.equals(media, that.media) && Objects.equals(low, that.low);
    }

    @Override
    public int hashCode() {
        return Objects.hash(high, media, low);
    }

    @Override
    public String toString() {
        return "CacheProperty{" +
                "high=" + high +
                ", media=" + media +
                ", low=" + low +
                '}';
    }
}