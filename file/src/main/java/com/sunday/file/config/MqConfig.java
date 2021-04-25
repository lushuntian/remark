package com.sunday.file.config;

import com.aliyun.openservices.ons.api.PropertyKeyConst;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class MqConfig {
    private final String accessKey;
    private final String secretKey;
    private final String nameSrvAddr;

    //消费者组
    public final String GROUP_CONSUMER_ID = "GID_FILE";

    //Topic
    public final String TOPIC_ISSUE = "Topic_Vote";

    //Tag
    public final String TAG_ISSUE = "";

    public MqConfig(){
        this.accessKey = "";
        this.secretKey = "";
        this.nameSrvAddr = "";
    }

    public Properties getMqPropertie() {
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.AccessKey, this.accessKey);
        properties.setProperty(PropertyKeyConst.SecretKey, this.secretKey);
        properties.setProperty(PropertyKeyConst.NAMESRV_ADDR, this.nameSrvAddr);
        return properties;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getNameSrvAddr() {
        return nameSrvAddr;
    }
}
