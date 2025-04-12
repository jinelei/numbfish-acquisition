package com.jinelei.numbfish.acquisition.client.mqtt.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

/**
 * @Author: jinelei
 * @Description: 异常消息处理器<br />
 *               目前只是简单的打印日志，以后需要扩展请重写handleMessage方法
 * @Date: 2023/08/18
 * @Version: 1.0.0
 */
@Slf4j
@Component
public class ExceptionHandler implements MessageHandler {
  @Override
  public void handleMessage(@NonNull Message<?> message) throws MessagingException {
    MessagingException exception = (MessagingException) message.getPayload();
    log.error("handle error message: reason: {}, origin message: {}", exception.getMessage(),
        exception.getFailedMessage());
  }
}
