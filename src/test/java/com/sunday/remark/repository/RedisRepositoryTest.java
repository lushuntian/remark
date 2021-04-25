package com.sunday.remark.repository;

import com.sunday.remark.RemarkApplication;
import com.sunday.remark.component.BaseTest;
import net.sf.json.JSONArray;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@SpringBootTest(classes={RemarkApplication.class})
@RunWith(SpringRunner.class)
class RedisRepositoryTest extends BaseTest{
    @Autowired
    RedisRepository redisRepository;


}