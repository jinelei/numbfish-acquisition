package com.jinelei.numbfish.acquisition.enumeration;

/**
 * @Author: jinelei
 * @Description: 设备运行状态
 * @Date: 2023/04/10
 * @Version: 1.0.0
 */
@SuppressWarnings("unused")
public enum RunningState {
    ALL(0, "所有状态"),
    STOP(1, "停止态"),
    RUN(2, "运行态"),
    IDLE(3, "空闲态"),
    OFFLINE(4, "离线态"),
    DEBUG(5, "调试态");
    private final int value;
    private final String name;

    RunningState(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

}