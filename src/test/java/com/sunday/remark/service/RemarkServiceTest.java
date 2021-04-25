package com.sunday.remark.service;

import com.sunday.remark.RemarkApplication;
import com.sunday.remark.component.BaseTest;
import com.sunday.remark.component.MockOrderService;
import com.sunday.remark.repository.RedisRepository;
import com.sunday.remark.repository.RemarkRepository;
import com.sunday.remark.repository.entity.Remark;
import com.sunday.remark.service.exception.ExceedAuthorizedAccessException;
import com.sunday.remark.util.RemarkRepositoryCounterWrapper;
import net.sf.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(classes={RemarkApplication.class})
@RunWith(SpringRunner.class)
class RemarkServiceTest extends BaseTest {
    @Autowired
    private RemarkService remarkService;

    @Autowired
    private JdbcTemplate db;

    @Autowired
    private RemarkRepository remarkRepository;


}













