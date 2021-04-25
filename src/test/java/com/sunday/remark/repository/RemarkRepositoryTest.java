package com.sunday.remark.repository;

import com.sunday.remark.RemarkApplication;
import com.sunday.remark.component.BaseTest;
import com.sunday.remark.repository.entity.Remark;
import com.sunday.remark.repository.entity.ScoreInfo;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest(classes={RemarkApplication.class})
@RunWith(SpringRunner.class)
class RemarkRepositoryTest extends BaseTest{
    @Autowired
    private RemarkRepository remarkRepository;

    @Autowired
    private JdbcTemplate db;

    @Test
    void removeRemark() {
        //我们删除data.sql现有的记录
        Remark remark = new Remark();
        remark.setConsumerId(1);
        remark.setItemId("1");
        remark.setOrderId("o1");
        remark.setId(1);
        boolean flag = remarkRepository.removeRemark(remark);
        Assert.assertTrue(flag);

        //记录还在不在
        String sql = "select count(*) from remark where id = 1";
        Integer count = db.queryForObject(sql, (resultSet, i) -> resultSet.getInt(1));

        Assert.assertEquals(count, Integer.valueOf(0));
    }

    @Test
    void listRemarks(){
        //查询一条数据
        List<Remark> list = remarkRepository.listRemarks("2", 100, 1);
        Assert.assertTrue(list.size() == 1);
        Assert.assertTrue(list.get(0).getId() == 6);

        //查询多条数据
        list = remarkRepository.listRemarks("2", 6, 10);
        Assert.assertTrue(list.size() == 3);
        Assert.assertTrue(list.get(0).getId() == 6);
        Assert.assertTrue(list.get(1).getId() == 5);

        //查询不到数据
        list = remarkRepository.listRemarks("2", 1, 10);
        Assert.assertTrue(list.size() == 0);
    }

    @Test
    void listScores() {
        List<ScoreInfo> list = remarkRepository.listScores("1");
        Assert.assertTrue(JSONArray.fromObject(list).toString().equals("[{\"count\":1,\"score\":20},{\"count\":1,\"score\":80},{\"count\":1,\"score\":100}]"));
    }
}



