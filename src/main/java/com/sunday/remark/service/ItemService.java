package com.sunday.remark.service;

import com.sunday.remark.service.util.RemoteServerUtil;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class ItemService {
    /**
     * 检查商品是否存在
     * @param itemId 商品id
     * @return 如果存在商品，返回true，否则返回false
     */
    public boolean checkItemExist(String itemId){
        //TODO 这里需要调用远程服务，但是我们尚未对接远程服务，因此设定一个假想值。
        RemoteServerUtil.mockRemoteServerRequest();
        return ThreadLocalRandom.current().nextDouble() < 0.8;
    }


}












