package com.sunday.remark.service;

import com.sunday.remark.repository.ConsumerRepository;
import com.sunday.remark.repository.entity.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConsumerService {
    @Autowired
    private ConsumerRepository consumerRepository;

    /**
     * 获取用户信息
     * @param consumerId 用户id
     * @return 用户信息对象
     */
    public Consumer getConsumer(long consumerId){
        return consumerRepository.readConsumer(consumerId);
    }


}
