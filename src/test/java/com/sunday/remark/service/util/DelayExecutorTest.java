package com.sunday.remark.service.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class DelayExecutorTest {
    private List<String> messages = Collections.synchronizedList(new ArrayList<>());

    @Test
    void executeLater() {
        DelayExecutor.executeLater(0, () -> messages.add("m1"));
        assertTrue(messages.contains("m1"));

        DelayExecutor.executeLater(100, () -> messages.add("m2"));
        assertFalse(messages.contains("m2"));

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertTrue(messages.contains("m2"));


    }

//    @Test
    void submit(){
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                int a = 0 / 0;
            }
        });

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("!!!");
            }
        });

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("@@@");
            }
        });

        executorService.shutdown();
    }

}