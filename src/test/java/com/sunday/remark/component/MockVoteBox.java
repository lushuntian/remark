package com.sunday.remark.component;

import com.sunday.remark.config.redis.RedisKeyConstants;
import com.sunday.remark.repository.VoteBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.text.MessageFormat;

@Repository
public class MockVoteBox extends VoteBox {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public MockVoteBox(RedisTemplate<String, Object> redisTemplate) {
        super(redisTemplate, null, null);
    }

    @Override
    public Integer vote(long voterId, long contentId) {
        if(redisTemplate.opsForValue().setIfAbsent(MessageFormat.format(RedisKeyConstants.VOTE_USER_PATTERN, voterId, contentId), "1"))
            return redisTemplate.opsForValue().increment(MessageFormat.format(RedisKeyConstants.VOTE_SUM_PATTERN, contentId)).intValue();
        return Integer.valueOf((Integer) redisTemplate.opsForValue().get(MessageFormat.format(RedisKeyConstants.VOTE_SUM_PATTERN, contentId)));
    }

    @Override
    public Integer noVote(long voterId, long contentId) {
        if(redisTemplate.delete(MessageFormat.format(RedisKeyConstants.VOTE_USER_PATTERN, voterId, contentId)))
            return redisTemplate.opsForValue().decrement(MessageFormat.format(RedisKeyConstants.VOTE_SUM_PATTERN, contentId)).intValue();
        return Integer.valueOf((Integer) redisTemplate.opsForValue().get(MessageFormat.format(RedisKeyConstants.VOTE_SUM_PATTERN, contentId)));
    }
}
