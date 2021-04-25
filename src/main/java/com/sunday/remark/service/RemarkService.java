package com.sunday.remark.service;

import com.sunday.remark.config.redis.RedisKeyConstants;
import com.sunday.remark.config.SystemConstants;
import com.sunday.remark.repository.RemarkRedis;
import com.sunday.remark.repository.RemarkRepository;
import com.sunday.remark.repository.entity.Remark;
import com.sunday.remark.repository.entity.ScoreInfo;
import com.sunday.remark.service.exception.ExceedAuthorizedAccessException;
import com.sunday.remark.service.util.DelayExecutor;
import com.sunday.remark.service.util.RemarkQueue;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;

/**
 * 评价服务
 */
@Service
@Slf4j
public class RemarkService {
    private final RemarkRepository remarkRepository;
    private final RemarkRedis remarkRedis;
    private final OrderService orderService;
    private final RemarkQueue remarkQueue = new RemarkQueue(3);

    public RemarkService(RemarkRepository remarkRepository, RemarkRedis remarkRedis, OrderService orderService) {
        this.remarkRepository = remarkRepository;
        this.remarkRedis = remarkRedis;
        this.orderService = orderService;
    }

    /**
     * [CREATE] 发布评价
     *
     * @param remark 评价对象
     * @return 如果新增成功，返回true，否则，返回false或抛出异常。
     * @throws ExceedAuthorizedAccessException 越权访问异常
     */
    public boolean createRemark(/*valid*/ Remark remark) throws ExceedAuthorizedAccessException {
        //用户订单校验
        String itemId = orderService.checkAndGetItemId(remark.getConsumerId(), remark.getOrderId());
        if (itemId == null) {
            throw new ExceedAuthorizedAccessException("用户不存在有效订单");
        }

        remark.setItemId(itemId);
        try {
            //直接插入，利用数据库的唯一约束做幂等性校验。
            long remarkId = remarkRepository.addRemark(remark);

            if (remarkId > 0) {
                //如果插入成功，则同步到redis列表中
                remark.setId(remarkId);
                asyncAddRemark(remark);
                return true;
            }
        } catch (Exception e) {
            log.warn("function RemarkService.createRemark make exception:{} by:{}", e.getMessage(), remark);
        }

        return false;
    }


    /**
     * [Delete] 删除评价
     *
     * @param remark 评价对象
     * @return 删除成功返回true，否则返回false或抛出异常
     */
    public boolean deleteRemark(/*valid*/ Remark remark) {
        try {
            if (!remarkRepository.removeRemark(remark)) {
                //因参数不一致而出错
                return false;
            }
        } catch (Exception e) {
            log.warn("function RemarkService.deleteRemark make exception:{} by:{}", e.getMessage(), remark);
            return false;
        }

        remarkRedis.refresh(remark.getItemId(), remark.getScore());
        return true;
    }

    /**
     * 查询商品关联的评价,按时间倒叙排序
     *
     * @param itemId 商品id
     * @param start  开始索引
     * @param stop   中止索引
     * @return 返回从curIndex开始，一个小于等于期望值数量的列表。如果无内容，返回长度为0的数组。
     */
    @Cacheable(value = "remark")
    public JSONArray listRemarks(/*not null*/ String itemId, int start, int stop) {
        try {
            //尝试施加读锁
            Long code = remarkRedis.readAndLock(itemId);
            if (code != null && code >= 0) {
                //加锁成功，需要加载数据库中的评价内容到redis
                remarkQueue.push(itemId, () -> reloadRemarks(itemId, code));
            }

            return appendVoteInfo(remarkRedis.queryRange(itemId, start, stop));
        } catch (Exception e) {
            log.error("function RemarkService.listRemarks make exception:{} by:{},{},{}", e.getMessage(), itemId, start, stop);
            return SystemConstants.EMPTY_ARRAY;
        }
    }

    /**
     * 查询评分列表
     *
     * @param itemId 商品id
     */
    @Cacheable(value = "score")
    public JSONArray listScores(/*not null*/ String itemId) {
        try {
            Map<Object, Object> map = remarkRedis.readScoreData(itemId);

            if (map == null || map.isEmpty()) {
                //冷启动时加载
                if (remarkRedis.lockScoreData(itemId)) {
                    List<ScoreInfo> list = remarkRepository.listScores(itemId);
                    list.add(new ScoreInfo((short) 0, 0));
                    map = new HashMap<>();
                    for (ScoreInfo scoreInfo : list) {
                        map.put(String.valueOf(scoreInfo.getScore()), scoreInfo.getCount());
                    }

                    remarkRedis.saveScoreData(itemId, map);
                    return JSONArray.fromObject(list);
                } else {
                    //阻塞等待内容加载完成
                    int count = 10;
                    while (count-- > 0) {
                        Thread.sleep(1000);
                        map = remarkRedis.readScoreData(itemId);
                        if (map != null && !map.isEmpty())
                            break;
                    }

                    if (map == null || map.isEmpty())
                        return SystemConstants.EMPTY_ARRAY;
                }
            }

            JSONArray array = new JSONArray();
            map.forEach((o, o2) -> {
                JSONObject object = new JSONObject();
                object.put("score", o);
                object.put("count", o2);
                array.add(object);
            });

            return array;
        } catch (Exception e) {
            log.error("function RemarkService.listScores make exception:{} by:{}", e.getMessage(), itemId);
            return SystemConstants.EMPTY_ARRAY;
        }
    }

    /**
     * 查询商品推荐评价列表
     *
     * @param itemId 商品id
     * @param start  开始索引
     * @param stop   中止索引
     * @return 返回从curIndex开始，一个小于等于期望值数量的列表。如果无内容，返回长度为0的数组。
     */
    @Cacheable(value = "recommend")
    public JSONArray listRecommendRemarks(/*not null*/ String itemId, int start, int stop) {
        try {
            if (remarkRedis.shouldUpdateRecommend(itemId)) {
                //加锁成功，需要加载数据库中的评价内容到redis
                remarkQueue.push(itemId, () -> reloadRecommendRemarks(itemId));
            }

            return appendVoteInfo(remarkRedis.readRecommendRange(itemId, start, stop));
        } catch (Exception e) {
            log.error("function RemarkService.listRecommendRemarks make exception:{} by:{},{},{}", e.getMessage(), itemId, start, stop);
            return SystemConstants.EMPTY_ARRAY;
        }
    }

    /**
     * 给评价列表添加用户点赞信息，在现有列表数据上修改
     *
     * @param remarks    评价列表
     * @param consumerId 用户id
     * @return 修改后的评价列表
     */
    public JSONArray appendDynamicVoteInfo(JSONArray remarks, Integer consumerId) {
        if (remarks == null || remarks.size() == 0 || consumerId == null) {
            return remarks;
        }

        //获取评价id列表
        List<Object> idList = new ArrayList<>();
        for (int i = 0; i < remarks.size(); i++) {
            idList.add(remarks.getJSONObject(i).getString("id"));
        }

        //获取并添加个人点赞状态
        List<String> votesKeys = new ArrayList<>();
        for (Object s : idList) {
            votesKeys.add(MessageFormat.format(RedisKeyConstants.VOTE_USER_PATTERN, consumerId, s));
        }
        List<Object> votingValues = remarkRedis.readAll(votesKeys);
        for (int i = 0; i < remarks.size(); i++) {
            remarks.getJSONObject(i).put("voting", votingValues.get(i) == null ? 0 : 1);
        }

        return remarks;
    }

    /**
     * 给评价列表添加点赞信息，在现有列表数据上修改
     *
     * @param remarks 评价列表
     * @return 修改后的评价列表
     */
    public JSONArray appendVoteInfo(JSONArray remarks) {
        if (remarks == null || remarks.size() == 0) {
            return remarks;
        }

        //获取评价id列表
        List<Object> idList = new ArrayList<>();
        for (int i = 0; i < remarks.size(); i++) {
            idList.add(remarks.getJSONObject(i).getString("id"));
        }

        //获取并添加点赞总数
        List<String> voteKeys = new ArrayList<>();
        for (Object s : idList) {
            voteKeys.add(MessageFormat.format(RedisKeyConstants.VOTE_SUM_PATTERN, s));
        }
        List<Object> voteValues = remarkRedis.readAll(voteKeys);
        for (int i = 0; i < remarks.size(); i++) {
            remarks.getJSONObject(i).put("votes", voteValues.get(i) == null ? 0 : voteValues.get(i));
        }

        return remarks;
    }

    /**
     * 将用户发布的评价内容异步写入到redis列表中
     */
    private void asyncAddRemark(Remark remark) {
        remarkQueue.push(remark.getItemId(), () -> {
            try {
                switch (remarkRedis.pushAndLock(remark)) {
                    //加锁成功，需要加载数据库中的评价内容到redis
                    case LOCK_OK -> reloadRemarks(remark.getItemId(), remark.getId());

                    //加锁失败，等待片刻后重新加入队列
                    case REDO -> DelayExecutor.executeLater(1000, () -> asyncAddRemark(remark));
                }
            } catch (Exception e) {
                e.printStackTrace();
                //出现异常，丢弃任务，在一段时间内不一致，但是redis恢复后，并且key过期后，会最终达成一致
                log.warn("asyncRedisList Exception:{}, by:{}", e.getMessage(), remark);
            }
        });
    }

    //加载评价内容
    private void reloadRemarks(String itemId, long code) {
        List<Remark> list = remarkRepository.listRemarks(itemId, code == 0 ? Integer.MAX_VALUE : code, SystemConstants.REMARK_MAX_DISPLAY_LENGTH);
        remarkRedis.swapAndRelease(itemId, code, list);
    }

    //加载评价推荐内容
    private void reloadRecommendRemarks(String itemId) {
        List<Remark> list = remarkRepository.listRecommendRemarks(itemId, SystemConstants.REMARK_MAX_RECOMMEND_LENGTH);
        remarkRedis.saveRecommendData(itemId, list);
    }
}




















