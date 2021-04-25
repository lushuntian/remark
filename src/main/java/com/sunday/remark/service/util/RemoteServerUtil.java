package com.sunday.remark.service.util;

import com.sunday.remark.service.exception.RemoteRequestException;

import java.util.concurrent.ThreadLocalRandom;

public class RemoteServerUtil {
    public static void mockRemoteServerRequest(){
        //模拟远程请求延迟
        try {
            Thread.sleep(10 + ThreadLocalRandom.current().nextInt(10));
        }catch (InterruptedException e){
        }
    }
}
