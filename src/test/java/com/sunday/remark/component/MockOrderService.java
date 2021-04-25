package com.sunday.remark.component;

import com.sunday.remark.service.OrderService;

public class MockOrderService extends OrderService {
    @Override
    public String checkAndGetItemId(long consumerId, String orderId) {
        return null;
    }
}
