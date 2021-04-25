package com.sunday.remark.component;

import com.sunday.remark.repository.MessagePoster;

public class MockMq extends MessagePoster {
    public MockMq() {
        super(null);
    }

    public boolean isFlag() {
        return flag;
    }

    private boolean flag = false;

    @Override
    public void sendMessage(String topic, String tag, String content) {
        flag = true;
    }
}
