package com.sunday.remark.repository;

import com.sunday.remark.repository.entity.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ConsumerRepository {
    @Autowired
    private JdbcTemplate db;

    /**
     * 获取用户信息
     * @param id 用户id
     * @return 用户信息对象
     */
    public Consumer readConsumer(long id){
        String sql = "select mobile,nickname,face,email,extension from consumer where id = ?";
        return db.queryForObject(sql, (resultSet, i) -> {
            Consumer consumer = new Consumer();
            consumer.setId(id);
            consumer.setMobile(resultSet.getString(1));
            consumer.setNickname(resultSet.getString(2));
            consumer.setFace(resultSet.getString(3));
            consumer.setEmail(resultSet.getString(4));
            consumer.setExtension(resultSet.getString(5));
            return consumer;
        }, id);
    }

}
