package com.sunday.remark.service.exception;

/**
 * 越权访问异常
 */
public class ExceedAuthorizedAccessException extends Exception{
    public ExceedAuthorizedAccessException(){
        super();
    }

    public ExceedAuthorizedAccessException(String message){
        super(message);

    }

    public ExceedAuthorizedAccessException(String message, Throwable cause){
        super(message,cause);
    }

    public ExceedAuthorizedAccessException(Throwable cause) {
        super(cause);
    }
}
