package com.sunday.remark.repository;

import com.sunday.remark.repository.entity.Remark;
import com.sunday.remark.repository.entity.ScoreInfo;
import net.sf.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Repository
public class RemarkRepository {
    @Autowired
    private JdbcTemplate db;

    /**
     * 添加评价
     * @param remark 评价对象
     * @return 如果插入成功，返回插入后的自动主键，否则返回-1
     */
    public long addRemark(/*valid*/ Remark remark) {
        final String sql = "insert into remark(consumer_id,item_id,order_id,score,header,content,images,user_name,user_face) values(?,?,?,?,?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        db.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setLong(1, remark.getConsumerId());
            ps.setString(2, remark.getItemId());
            ps.setString(3, remark.getOrderId());
            ps.setShort(4, remark.getScore());
            ps.setString(5, remark.getHeader());
            ps.setString(6, remark.getContent());
            ps.setString(7, remark.getImages());
            ps.setString(8, remark.getUsername());
            ps.setString(9, remark.getUserface());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() == null){
            return -1;
        }

        return keyHolder.getKey().intValue();
    }

    /**
     * 删除评价
     * @param remark 评价对象
     * @return 如果插入成功，返回true，否则返回false
     */
    public boolean removeRemark(/*valid*/ Remark remark){
        //联合多个字段查询，确保删除的评价确实是consumerId所发布的，利用数据库本身的特性进行归属逻辑判断，
        //从根本上避免越权删除别人的评价
        String sql = "delete from remark where id = ? and order_id = ? and consumer_id = ? and item_id = ?";
        return db.update(sql, remark.getId(), remark.getOrderId(), remark.getConsumerId(), remark.getItemId()) > 0;
    }

    /**
     * 查询商品关联的离curId最近的若干条评价
     * @param itemId 商品id
     * @param curId 查询的主键id不超过maxId
     * @param expectCount 期望数量
     * @return 返回离curId最新的expectCount条评价，如果小于这个数量，返回对应元素数量的列表，如果评价为空，返回长度=0的列表
     */
    public List<Remark> listRemarks(/*not null*/ String itemId, long curId, int expectCount){
        if (expectCount <= 0)
            return new ArrayList<>();

        Assert.isTrue(expectCount <= MAX_LIST_SIZE, "不允许一次性查询过多内容");

        String sql = "select id,consumer_id,order_id,score,header,content,images,user_name,user_face,gmt_create from remark where item_id = ? and status = '1' and id <= ? order by id desc limit ?";
        return db.query(sql, (resultSet, i) -> {
            Remark remark = new Remark();
            remark.setId(resultSet.getLong(1));
            remark.setConsumerId(resultSet.getLong(2));
            remark.setOrderId(resultSet.getString(3));
            remark.setItemId(itemId);
            remark.setScore(resultSet.getShort(4));
            remark.setHeader(resultSet.getString(5));
            remark.setContent(resultSet.getString(6));
            remark.setImages(resultSet.getString(7));
            remark.setUsername(resultSet.getString(8));
            remark.setUserface(resultSet.getString(9));
            remark.setCreateTime(resultSet.getString(10));
            return remark;
        }, itemId, curId, expectCount);
    }

    /**
     * 查询商品的评价列表
     * @param itemId 商品id
     * @return 评价列表，如果商品不存在或者查询为空，返回size=0的列表
     */
    public List<ScoreInfo> listScores(/*not null*/ String itemId){
        String sql = "select score,count(*) from remark where item_id = ? and status = '1' group by score order by score";
        return db.query(sql, (resultSet, i) -> {
            ScoreInfo scoreInfo = new ScoreInfo();
            scoreInfo.setScore(resultSet.getShort(1));
            scoreInfo.setCount(resultSet.getLong(2));
            return scoreInfo;
        }, itemId);
    }

    /**
     * 查询商品的推荐评价
     * @param itemId 商品id
     * @param expectCount 期望数量
     * @return 返回点赞数最高的expectCount条评价，如果小于这个数量，返回对应元素数量的列表，如果评价为空，返回长度=0的列表
     */
    public List<Remark> listRecommendRemarks(/*not null*/ String itemId, int expectCount){
        if (expectCount <= 0)
            return new ArrayList<>();

        Assert.isTrue(expectCount <= MAX_LIST_SIZE, "不允许一次性查询过多内容");

        String sql = "select id,consumer_id,order_id,score,header,content,images,user_name,user_face,gmt_create from remark where item_id = ? and status = '1' order by votes desc limit ?";
        return db.query(sql, (resultSet, i) -> {
            Remark remark = new Remark();
            remark.setId(resultSet.getLong(1));
            remark.setConsumerId(resultSet.getLong(2));
            remark.setOrderId(resultSet.getString(3));
            remark.setItemId(itemId);
            remark.setScore(resultSet.getShort(4));
            remark.setHeader(resultSet.getString(5));
            remark.setContent(resultSet.getString(6));
            remark.setImages(resultSet.getString(7));
            remark.setUsername(resultSet.getString(8));
            remark.setUserface(resultSet.getString(9));
            remark.setCreateTime(resultSet.getString(10));
            return remark;
        }, itemId, expectCount);
    }

    //最大允许查询数量
    private static final int MAX_LIST_SIZE = 1000;


}







