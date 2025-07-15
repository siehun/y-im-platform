package io.yue.im.platform.common.exception;


import io.yue.im.platform.common.model.enums.HttpCode;
import io.yue.im.platform.common.response.ResponseMessage;
import io.yue.im.platform.common.response.ResponseMessageFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理器
 */
@Slf4j
@ControllerAdvice
public class IMExceptionHandler {
    @ResponseBody
    @ExceptionHandler(IMException.class)
    public ResponseMessage<String> handleIMException(IMException e) {
        return ResponseMessageFactory.getErrorResponseMessage(e.getCode(),e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public ResponseMessage<String> handleException(Exception e) {
        e.printStackTrace();
        return ResponseMessageFactory.getErrorResponseMessage(HttpCode.PROGRAM_ERROR);
    }
}
