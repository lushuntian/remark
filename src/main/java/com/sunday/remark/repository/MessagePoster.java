package com.sunday.remark.repository;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.Producer;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;

/**
 * 消息生产者，消息投递仓库
 */
@Repository
public class MessagePoster {
    private final Producer producer;

    public MessagePoster(Producer producer) {
        this.producer = producer;
    }

    public void sendMessage(String topic, String tag, String content){
        Message message = new Message();
        message.setTopic(topic);
        message.setTag(tag);
        message.setBody(content.getBytes(StandardCharsets.UTF_8));
        producer.send(message);
    }

    public void sendMessage(String topic, String content){
        sendMessage(topic, "", content);
    }
}
