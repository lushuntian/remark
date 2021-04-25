package com.sunday.file.receive;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.shade.com.alibaba.fastjson.JSONObject;
import com.sunday.file.repository.VoteRepository;
import com.sunday.file.repository.entity.Vote;
import org.springframework.stereotype.Component;

/**
 * 投票消息接收器
 */
@Component
public class VoteMessageReceiver implements MessageListener {
    private final VoteRepository voteRepository;

    public VoteMessageReceiver(VoteRepository voteRepository) {
        this.voteRepository = voteRepository;
    }

    @Override
    public Action consume(Message message, ConsumeContext context) {
        try {
            JSONObject object = JSONObject.parseObject(new String(message.getBody()));

            Vote vote = new Vote();
            vote.setVoterId(object.getLongValue("voterId"));
            vote.setContentId(object.getLongValue("contentId"));
            vote.setVoting(object.getIntValue("voting"));
            vote.setVotes(object.getLongValue("votes"));

            try {
                vote.validate();
                voteRepository.addVote(vote);
            } catch (IllegalArgumentException ignored) {
            }

            return Action.CommitMessage;
        }catch (Exception e) {
            e.printStackTrace();
            return Action.ReconsumeLater;
        }
    }
}
