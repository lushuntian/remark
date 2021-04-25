package com.sunday.remark.service;

import com.sunday.remark.service.util.RemoteServerUtil;
import org.springframework.stereotype.Service;

@Service
/**
 * 订单服务
 */
public class OrderService {
    /**
     * 检查是否已经产生订单关系，并返回订单关联的商品id
     * @param consumerId 用户id
     * @param orderId 订单id
     * @return 如果订单有效，返回商品id，否则返回null
     */
    public String checkAndGetItemId(long consumerId, String orderId){
        //TODO 这里需要调用远程服务，但是我们尚未对接远程服务，因此设定一个假想值。
        RemoteServerUtil.mockRemoteServerRequest();
        return "item-1";
    }

}












