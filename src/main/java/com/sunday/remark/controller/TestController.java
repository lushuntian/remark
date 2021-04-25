package com.sunday.remark.controller;

import com.sunday.remark.config.SystemConstants;
import com.sunday.remark.controller.util.APIBody;
import com.sunday.remark.controller.util.RtnCodeEnum;
import com.sunday.remark.repository.RedisRepository;
import com.sunday.remark.repository.RemarkRedis;
import com.sunday.remark.repository.RemarkRepository;
import com.sunday.remark.repository.TestConfigRepository;
import com.sunday.remark.repository.entity.Remark;
import com.sunday.remark.service.RemarkService;
import com.sunday.remark.service.SecurityService;
import com.sunday.remark.service.exception.ExceedAuthorizedAccessException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
public final class TestController {
    @Autowired
    private RemarkRedis remarkRedis;

    @Autowired
    private TestConfigRepository configRepository;


    @GetMapping("/test")
    public String test(){
        return configRepository.getStr();
    }


}