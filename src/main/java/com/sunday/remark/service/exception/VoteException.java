package com.sunday.remark.service.exception;

public class VoteException extends Exception{
    public VoteException() {
        super();
    }

    public VoteException(String message) {
        super(message);
    }

    public VoteException(String message, Throwable cause) {
        super(message, cause);
    }
}
