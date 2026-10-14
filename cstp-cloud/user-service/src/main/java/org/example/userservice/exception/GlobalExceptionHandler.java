package org.example.userservice.exception;

import org.example.userservice.entity.Result;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    //捕获业务异常
    @ExceptionHandler(BusinessException.class)
    public Result handleBusinessException(BusinessException e){
        return Result.error(e.getCode(), e.getMessage());
    }
    //捕获其他异常
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e){
        e.printStackTrace();
        return Result.error(500, "系统错误，请联系管理员");
    }
    //捕获参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.error(400, msg);
    }

}
