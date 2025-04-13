package com.jinelei.numbfish.acquisition.client.property;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * @Author: jinelei
 * @Description:
 * @Date: 2024/8/1 17:18
 * @Version: 1.0.0
 */
@SuppressWarnings("unused")
public class SecurityProperty {
    /**
     * jwt相关
     */
    @NotNull(message = "jwt配置不合法")
    protected JwtProperty jwt;
    /**
     * 忽略白名单，必须是ant风格
     */
    @NotNull(message = "忽略url不合法")
    protected List<String> ignore;

    public JwtProperty getJwt() {
        return jwt;
    }

    public void setJwt(JwtProperty jwt) {
        this.jwt = jwt;
    }

    public List<String> getIgnore() {
        return ignore;
    }

    public void setIgnore(List<String> ignore) {
        this.ignore = ignore;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SecurityProperty that = (SecurityProperty) o;
        return Objects.equals(jwt, that.jwt) && Objects.equals(ignore, that.ignore);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jwt, ignore);
    }

    @Override
    public String toString() {
        return "SecurityProperty{" +
                "jwt=" + jwt +
                ", ignore=" + ignore +
                '}';
    }
}