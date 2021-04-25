package com.sunday.remark.controller.aspect;

import com.sunday.remark.controller.util.APIBody;
import com.sunday.remark.controller.util.RtnCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

/**
 * 捕获全局非受检异常
 */
@RestControllerAdvice(basePackages = "com.sunday.remark.controller")
@Slf4j
public class CommonControllerAdvice {
    //assert断言的非法参数异常
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<APIBody> handleAssertExceptions(
            IllegalArgumentException ex) {
        log.info("assert error:{}", ex.getMessage());
        APIBody body = APIBody.buildError(RtnCodeEnum.USER_ERROR_A0400, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    //sql异常
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<APIBody> handleDatabaseExceptions(
            SQLException ex) {
        log.info("sql error:{} code:{}", ex.getMessage(), ex.getErrorCode());
        APIBody body = APIBody.buildError(RtnCodeEnum.SERVICE_ERROR_C0300);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    //空指针异常
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<APIBody> handleNPEExceptions(
            NullPointerException ex) {
        log.error("NPE error:{}", ex.getMessage());
        APIBody body = APIBody.buildError(RtnCodeEnum.SYSTEM_ERROR_B0001);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    //全局异常处理
    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIBody> handleCommonExceptions(
            Exception ex) {
        log.error("unkown error:{}", ex.getMessage());
        APIBody body = APIBody.buildError(RtnCodeEnum.SYSTEM_ERROR_B0001);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}

