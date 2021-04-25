package com.sunday.remark.repository;

import com.sunday.remark.RemarkApplication;
import com.sunday.remark.component.BaseTest;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes={RemarkApplication.class})
@RunWith(SpringRunner.class)
class VoteBoxTest extends BaseTest {

    @Autowired
    private VoteBox voteBox;

    @Autowired
    private RedisRepository redisRepository;

    @Test
    void vote() {

    }
}