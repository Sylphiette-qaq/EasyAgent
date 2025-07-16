package com.demo.agent.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 处理参数校验异常 */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<?> handleValidationException(Exception ex) {
        String msg;
        if (ex instanceof MethodArgumentNotValidException) {
            msg = ((MethodArgumentNotValidException) ex).getBindingResult().getFieldErrors()
                    .stream().map(FieldError::getDefaultMessage).collect(Collectors.joining(", "));
        } else if (ex instanceof BindException) {
            msg = ((BindException) ex).getBindingResult().getFieldErrors()
                    .stream().map(FieldError::getDefaultMessage).collect(Collectors.joining(", "));
        } else {
            msg = ex.getMessage();
        }
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    /** 处理业务异常 */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException ex) {
        log.info(ex.getMessage());
        return Result.fail(ResultCode.FAIL.getCode(), ex.getMessage());
    }

    /** 处理所有其他异常 */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception ex) {
        log.info(ex.getMessage());
        return Result.fail(ResultCode.FAIL.getCode(), "服务器异常: " + ex.getMessage());
    }
} 