package com.jinelei.numbfish.acquisition.client.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("unused")
@Component
@ConfigurationProperties(prefix = "numbfish.authorization")
public class AcquisitionProperty {

    public static final String AUTHORIZATION = "Authorization";
    public static final String SIGNATURE = "Signature";
    public static final String TIMESTAMP = "Timestamp";
    public static final String ACCESS_KEY = "AccessKey";
    public static final String USER = "User";
    public static final String CLIENT = "Client";
    public static final String AUTHORIZE_TYPE = "Authorize-Type";
    public static final String SLASH_STRING = "/";
    public static final String USER_LOGIN_URL_STRING = "/user/login";
    public static final String USER_LOGOUT_URL_STRING = "/user/logout";
    public static final String EMPTY_STRING = "";
    public static final String ALL_STRING = "**";
    public static final Function<String, String> REMOVE_SUFFIX = s -> s.endsWith(SLASH_STRING)
            ? s.substring(0, s.length() - 1)
            : s;
    public static final Function<String, String> REMOVE_PREFIX = s -> s.startsWith(SLASH_STRING) ? s.substring(1) : s;
    protected LoginProperty login;
    protected LogoutProperty logout;
    protected AdminProperty admin;
    protected IgnoreProperty ignore;
    protected PlaceholderProperty placeHolder;
    protected ContextProperty context;

    public String getContextUrl() {
        return Optional.ofNullable(this.context)
                .map(ContextProperty::getUrl)
                .map(REMOVE_PREFIX)
                .orElse(ALL_STRING);
    }

    public String getLoginUrl() {
        return Optional.ofNullable(this.login)
                .map(LoginProperty::getUrl)
                .orElse(USER_LOGIN_URL_STRING);
    }

    public String getLogoutUrl() {
        return Optional.ofNullable(this.logout)
                .map(LogoutProperty::getUrl)
                .orElse(USER_LOGOUT_URL_STRING);
    }

    public List<String> getIgnoreUrls() {
        return Optional.ofNullable(this.ignore)
                .map(IgnoreProperty::getUrls)
                .orElse(new ArrayList<>());
    }

    public String getTokenHeader() {
        return Optional.ofNullable(this.placeHolder)
                .map(PlaceholderProperty::getHeader)
                .orElse(AUTHORIZATION);
    }

    public String getSignatureHeader() {
        return Optional.ofNullable(this.placeHolder)
                .map(PlaceholderProperty::getSignature)
                .orElse(SIGNATURE);
    }

    public String getTimestampHeader() {
        return Optional.ofNullable(this.placeHolder)
                .map(PlaceholderProperty::getTimestamp)
                .orElse(TIMESTAMP);
    }

    public String getAccessKeyHeader() {
        return Optional.ofNullable(this.placeHolder)
                .map(PlaceholderProperty::getAccessKey)
                .orElse(ACCESS_KEY);
    }

    public String getTokenPlaceholderUser() {
        return Optional.ofNullable(this.placeHolder)
                .map(PlaceholderProperty::getUser)
                .orElse(USER);
    }

    public String getTokenPlaceholderClient() {
        return Optional.ofNullable(this.placeHolder)
                .map(PlaceholderProperty::getClient)
                .orElse(CLIENT);
    }

    public String getTokenPlaceholderAuthorizeType() {
        return Optional.ofNullable(this.placeHolder)
                .map(PlaceholderProperty::getAccessKey)
                .orElse(AUTHORIZE_TYPE);
    }

    public LoginProperty getLogin() {
        return login;
    }

    public void setLogin(LoginProperty login) {
        this.login = login;
    }

    public LogoutProperty getLogout() {
        return logout;
    }

    public void setLogout(LogoutProperty logout) {
        this.logout = logout;
    }

    public AdminProperty getAdmin() {
        return admin;
    }

    public void setAdmin(AdminProperty adminProperty) {
        this.admin = adminProperty;
    }

    public IgnoreProperty getIgnore() {
        return ignore;
    }

    public void setIgnore(IgnoreProperty ignore) {
        this.ignore = ignore;
    }

    public PlaceholderProperty getPlaceHolder() {
        return placeHolder;
    }

    public void setPlaceHolder(PlaceholderProperty placeHolder) {
        this.placeHolder = placeHolder;
    }

    public ContextProperty getContext() {
        return context;
    }

    public void setContext(ContextProperty context) {
        this.context = context;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthorizationProperty that = (AuthorizationProperty) o;
        return Objects.equals(login, that.login) && Objects.equals(logout, that.logout)
                && Objects.equals(admin, that.admin) && Objects.equals(ignore, that.ignore)
                && Objects.equals(placeHolder, that.placeHolder) && Objects.equals(context, that.context);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, logout, admin, ignore, placeHolder, context);
    }

    @Override
    public String toString() {
        return "AuthorizationProperty{"
                + "login=" + login
                + ", logout=" + logout
                + ", admin=" + admin
                + ", ignore=" + ignore
                + ", header=" + placeHolder
                + ", context=" + context
                + '}';
    }
}
