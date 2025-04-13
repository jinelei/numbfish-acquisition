package com.jinelei.numbfish.acquisition.client.property;

import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * @Author: jinelei
 * @Description:
 * @Date: 2023/07/24
 * @Version: 1.0.0
 */
@SuppressWarnings("unused")
public class StoreProperty {
    /**
     * 存储路径
     */
    @NotNull(message = "存储路径不合法")
    protected String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        StoreProperty that = (StoreProperty) o;
        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(path);
    }

    @Override
    public String toString() {
        return "StoreProperty{" +
                "path='" + path + '\'' +
                '}';
    }
}