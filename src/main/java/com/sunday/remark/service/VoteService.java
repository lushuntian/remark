package com.sunday.remark.service;

import com.sunday.remark.config.SystemConstants;
import com.sunday.remark.repository.MessagePoster;
import com.sunday.remark.repository.VoteBox;
import com.sunday.remark.repository.retry.*;
import com.sunday.remark.service.exception.VoteException;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * 投票服务
 */
@Service
@Slf4j
public class VoteService {
    private final VoteBox voteBox;
    private final MessagePoster mq;
    private final RetryQueue retryQueue = new RetryQueue(new SegmentRetryTaskFactory());

    public VoteService(VoteBox voteBox, MessagePoster mq) {
        this.voteBox = voteBox;
        this.mq = mq;
    }

    /**
     * 给评价投票(点赞)
     *
     * @param voterId   投票人
     * @param contentId 投票目标内容id
     * @param voting    是否进行点赞（true：点赞  false：取消点赞）
     * @return 当前内容点赞后的总数，如果点赞失败，抛出异常
     * @throws VoteException 投票异常
     */
    public int vote(long voterId, long contentId, boolean voting) throws VoteException {
        /*
         * 第零种情况：用户请求没有发送到服务器，用户可以适时重试。
         * 第一种情况：执行1失败，最终点赞失败，记录日志，加入重试队列池，用户也可以适时重试。
         * 第二种情况：执行1成功，但返回时网络异常，最终点赞失败，记录日志，加入重试队列池，用户也可能适时重试，该方法是幂等的。
         * 第三种情况：执行1成功，但并未增加点赞总数，因为这次是重复提交。仍然执行之后的逻辑，该方法是幂等的。
         * 第四种情况：执行1成功，但执行2失败，记录日志，把发送mq加入重试队列池，返回成功。
         * 第五种情况：执行方法成功，但返回过程网络异常，用户未收到响应，用户稍后可以查询出点赞结果，用户也可以适时重试
         */

        List<Repeatable<Integer>> list = new ArrayList<>();

        //1.先在redis中投票
        list.add(new Computable<>() {
            @Override
            public Integer compute(int repeatTimes) {
                return voting ? voteBox.vote(voterId, contentId) : voteBox.noVote(voterId, contentId);
            }

            @Override
            public void recordFail(int attemptTimes, Exception e) {
                //只记录第一次错误
                if (attemptTimes == 0)
                    log.warn("function VoteService.vote.redis make exception:{} by:{},{},{}", e.getMessage(), voterId, contentId, voting);
            }

            @Override
            public void recordEnd(Exception e) {
                //放弃重试.当然，日志会记录下来，或者通过其他机制将失败记录到中央存储库中，最终还是可以恢复。
                log.warn("function VoteService.vote.redis quit:{} by:{},{},{}", e.getMessage(), voterId, contentId, voting);
            }
        });

        //2.再通知mq
        list.add(new Executable<>() {
            @Override
            public void execute(int repeatTimes, Integer receiveValue) {
                JSONObject object = new JSONObject();
                object.put("voterId", voterId);
                object.put("contentId", contentId);
                object.put("voting", voting ? 1 : 0);
                object.put("votes", receiveValue);
                mq.sendMessage(SystemConstants.VOTE_TOPIC, object.toString());
            }

            @Override
            public void recordFail(int attemptTimes, Exception e) {
                if (attemptTimes == 0)
                    log.warn("function VoteService.vote.mq make exception:{} by:{},{},{}", e.getMessage(), voterId, contentId, voting);
            }

            @Override
            public void recordEnd(Exception e) {
                log.trace("function VoteService.vote.mq quit:{} by:{},{},{}", e.getMessage(), voterId, contentId, voting);
            }
        });

        Integer value = null;
        try {
            //系统可能因为mq或者redis自身的过载等问题导致点赞失败，我们想珍惜用户的一次点赞，所以选择为他重试。
            value = retryQueue.submit(list);
        } catch (RetryRefuseException e) {
            log.error("function VoteService.vote.refuse make exception:{} by:{},{},{}", e.getMessage(), voterId, contentId, voting);
        }

        if (value == null){
            //当前无法获得投票总数，意味着点赞操作失败，虽然我们会稍后重试，但仍将这个信息告知用户，他们可以进行更理智的选择。
            throw new VoteException("投票失败，请稍后再试");
        }

        return value;
    }

    private static class SegmentRetryTaskFactory implements IRetryTaskFactory {
        private final static IRetryStrategy waitStrategy = new SmartRetryStrategy(new int[]{10,100,100,1000,10000});

        @Override
        public <V> IRetryTask<V> createRetryTask(List<Repeatable<V>> segments) {
            return new SegmentRetryTask<>(waitStrategy, 5, segments);
        }
    }
}















