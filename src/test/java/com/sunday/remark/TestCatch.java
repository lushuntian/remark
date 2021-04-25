package com.sunday.remark;

import org.springframework.util.StopWatch;

import java.util.concurrent.ThreadLocalRandom;

public class TestCatch {
    private final static Exception EXCEPTION = new Exception();
    public static void main(String[] args) {
        //分别代表不适用try-catch，使用但不抛出异常，使用但小概率抛出异常，使用但大概率抛出异常，一定抛出异常
        double[] rateList = new double[]{-1, 0, 0.00001, 0.1, 1};

        //预热
        doSomething(0.001);

        for (int i = 0; i < rateList.length; i++) {
            StopWatch watch = new StopWatch();
            watch.start();
            doSomething(rateList[i]);
            watch.stop();
            System.out.println(watch.getTotalTimeMillis());
        }
    }

    public static void doSomething(double exceptionRate) {
        if (exceptionRate < 0) {
            for (int i = 0; i < 100000000; i++) {
                Math.cos(ThreadLocalRandom.current().nextDouble());
            }
        } else {
            for (int i = 0; i < 100000000; i++) {
                try {
                    double v = ThreadLocalRandom.current().nextDouble();
                    Math.cos(v);
                    if (v < exceptionRate) throw EXCEPTION;
                } catch (Exception e) {
                }
            }
        }
    }
}
