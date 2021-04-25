package com.sunday.remark.repository.retry;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SegmentRetryTaskTest {
    private final List<String> messages = new ArrayList<>();

    @Test
    void doTry() {
        List<Repeatable<String>> list = new ArrayList<>();
        list.add(new Computable<>(){
            @Override
            public String compute(int repeatTimes) throws Exception {
                if (repeatTimes < 2)
                    throw new Exception();
                if (repeatTimes < 4)
                    return null;
                messages.add("result:good");
                return "good";
            }

            @Override
            public void recordFail(int attemptTimes, Exception e) {
                messages.add("fail:" + attemptTimes);
            }

            @Override
            public void recordFail(int attemptTimes) {
                messages.add("fail:" + attemptTimes);
            }

            @Override
            public void recordEnd(Exception e) {
                messages.add("end");
            }
        });

        list.add(new Executable<>() {
            @Override
            public void execute(int repeatTimes, String receiveValue) throws Exception {
                messages.add("receive:" + receiveValue);
                throw new Exception("exc");
            }

            @Override
            public void recordEnd(Exception e) {
                messages.add("end:" + e.getMessage());
            }
        });

        IRetryTask<String> retryTask = new SegmentRetryTask<>(new DefinedRetryStrategy(0), 5, list);

        //重试未开始
        assertFalse(retryTask.shouldClose());

        //重试直到成功
        assertFalse(retryTask.tryOnce());
        assertFalse(retryTask.shouldClose());
        assertFalse(retryTask.tryOnce());
        assertFalse(retryTask.tryOnce());
        assertFalse(retryTask.tryOnce());
        assertFalse(retryTask.tryOnce());
        assertFalse(retryTask.tryOnce());
        assertTrue(retryTask.shouldClose());

        assertTrue(messages.contains("result:good"));
        assertTrue(messages.contains("fail:1"));
        assertTrue(messages.contains("fail:2"));
        assertTrue(messages.contains("fail:3"));
        assertFalse(messages.contains("end"));
        assertTrue(messages.contains("receive:good"));
        assertTrue(messages.contains("end:exc"));
        assertEquals("good", retryTask.getResult());

    }
}