package com.jinelei.numbfish.acquisition.client.influx.bean;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * @Author: jinelei
 * @Description:
 * @Date: 2024/8/1 22:07
 * @Version: 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractMessage implements Serializable {
  @NotNull(message = "设备编码不能为空")
  protected String deviceCode;
  @NotNull(message = "时间不能为空")
  protected Instant time;

  public abstract @NotNull(message = "存储桶不能为空") String bucket();

  public abstract @NotNull(message = "测量点不能为空") String measurement();

  public abstract @NotNull(message = "tags不能为空") Map<String, String> tags();

  public abstract @NotNull(message = "fields不能为空") Map<String, Object> fields();
}