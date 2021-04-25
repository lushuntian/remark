package com.sunday.remark.controller;

import com.sunday.remark.RemarkApplication;
import com.sunday.remark.component.BaseTest;
import com.sunday.remark.controller.util.RtnCodeEnum;
import com.sunday.remark.util.MockUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes={RemarkApplication.class})
@RunWith(SpringRunner.class)
class RemarkControllerTest extends BaseTest{
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JdbcTemplate db;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void deleteRemark() throws Exception{
        //我们删除data.sql现有的记录
        String responseString;

        responseString = MockUtil.performDeleteExpectOK(mockMvc, "/remark",
                """
                        {
                          "id":2,
                          "consumerId": 2,
                          "orderId": "o2",
                          "itemId":"1"
                        }
                        """);
        assertEquals(MockUtil.getCode(responseString), RtnCodeEnum.SUCCESS.getCode());

        //记录还在不在
        String sql = "select count(*) from remark where id = 2";
        Integer count = db.queryForObject(sql, (resultSet, i) -> resultSet.getInt(1));

        assertEquals(count, Integer.valueOf(0));

        //删除不是用户自己的评价
        responseString = MockUtil.performDeleteExpectOK(mockMvc, "/remark",
                """
                        {
                          "id":4,
                          "consumerId": 3,
                          "orderId": "o4",
                          "itemId":"2"
                        }
                        """);
        assertEquals(MockUtil.getCode(responseString), RtnCodeEnum.SYSTEM_ERROR_B0001.getCode());

        //记录还在不在
        sql = "select count(*) from remark where id = 4";
        count = db.queryForObject(sql, (resultSet, i) -> resultSet.getInt(1));

        assertEquals(count, Integer.valueOf(1));
    }
}






