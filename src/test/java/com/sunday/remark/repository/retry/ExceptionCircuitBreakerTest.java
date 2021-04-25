package com.sunday.remark.repository.retry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionCircuitBreakerTest {

    @Test
    public void test(){
        CircuitBreaker circuitBreaker = new ExceptionCircuitBreaker();
        assertTrue(circuitBreaker.canPass());
        circuitBreaker.onFail();
        circuitBreaker.onFail();
        circuitBreaker.onFail();
        assertTrue(circuitBreaker.canPass());
        circuitBreaker.onFail();
        circuitBreaker.onFail();
        circuitBreaker.onFail();
        circuitBreaker.onFail();
        assertFalse(circuitBreaker.canPass());
        circuitBreaker.onSuccess();
        circuitBreaker.onSuccess();
        circuitBreaker.onSuccess();
        circuitBreaker.onSuccess();
        circuitBreaker.onSuccess();
        assertFalse(circuitBreaker.canPass());



    }
}