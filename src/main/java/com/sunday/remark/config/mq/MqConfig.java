package com.sunday.remark.config.mq;

import com.aliyun.openservices.ons.api.PropertyKeyConst;
import org.springframework.context.annotation.Configuration;
import java.util.Properties;

@Configuration
public class MqConfig {
    private final String accessKey;
    private final String secretKey;
    private final String nameSrvAddr;

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
}
