package com.jinelei.numbfish.acquisition.enumeration;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * @Author: jinelei
 * @Description: 设备运行状态
 * @Date: 2023/04/10
 * @Version: 1.0.0
 */
@SuppressWarnings("unused")
public enum RunningState {
    ALL(0, "所有状态", value -> true),
    STOP(1, "停止态", v -> "stop".equalsIgnoreCase(Optional.ofNullable(v).map(Object::toString).orElse(""))),
    RUN(2, "运行态", v -> "run".equalsIgnoreCase(Optional.ofNullable(v).map(Object::toString).orElse(""))),
    IDLE(3, "空闲态", v -> "idle".equalsIgnoreCase(Optional.ofNullable(v).map(Object::toString).orElse(""))),
    OFFLINE(4, "离线态", v -> "offline".equalsIgnoreCase(Optional.ofNullable(v).map(Object::toString).orElse(""))),
    DEBUG(5, "调试态", v -> "debug".equalsIgnoreCase(Optional.ofNullable(v).map(Object::toString).orElse("")));

    private final int value;
    private final String name;
    private final Predicate<Object> matcher;

    RunningState(int value, String name, Predicate<Object> matcher) {
        this.value = value;
        this.name = name;
        this.matcher = matcher;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public Predicate<Object> getMatcher() {
        return matcher;
    }

    public static RunningState parseFrom(Object value) {
        RunningState[] values = RunningState.values();
        for (int i = 0; i < values.length; i++) {
            RunningState v = values[values.length - 1 - i];
            if (v.matcher.test(value)) {
                return v;
            }
        }
        return ALL;
    }

}