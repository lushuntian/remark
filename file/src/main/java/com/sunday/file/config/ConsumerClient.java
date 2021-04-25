package com.sunday.file.config;

import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.bean.ConsumerBean;
import com.aliyun.openservices.ons.api.bean.Subscription;
import com.sunday.file.receive.VoteMessageReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class ConsumerClient {
    @Autowired
    private MqConfig mqConfig;

    @Autowired
    private VoteMessageReceiver receiver;

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ConsumerBean buildConsumer() {
        ConsumerBean consumerBean = new ConsumerBean();
        Properties properties = mqConfig.getMqPropertie();
        properties.setProperty(PropertyKeyConst.GROUP_ID, mqConfig.GROUP_CONSUMER_ID);
        properties.setProperty(PropertyKeyConst.ConsumeThreadNums, "10");
        consumerBean.setProperties(properties);

        Map<Subscription, MessageListener> subscriptionTable = new HashMap<>();
        Subscription subscription = new Subscription();
        subscription.setTopic(mqConfig.TOPIC_ISSUE);
        subscription.setExpression(mqConfig.TAG_ISSUE);
        subscriptionTable.put(subscription, receiver);

        consumerBean.setSubscriptionTable(subscriptionTable);
        return consumerBean;
    }
}
