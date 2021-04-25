package com.sunday.remark.controller;

import com.sunday.remark.controller.entity.Vote;
import com.sunday.remark.controller.util.APIBody;
import com.sunday.remark.controller.util.RtnCodeEnum;
import com.sunday.remark.service.VoteService;
import com.sunday.remark.service.exception.VoteException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VoteController {
    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    /**
     * 点赞、取消点赞
     */
    @PostMapping("/vote")
    public APIBody vote(@RequestBody Vote vote) {
        vote.validate();

        try {
            int voteCount = voteService.vote(vote.getVoterId(), vote.getContentId(), vote.getVoting() == 1);
            return APIBody.buildSuccess(voteCount);
        } catch (VoteException e) {
            return APIBody.buildError(RtnCodeEnum.SYSTEM_ERROR_B0001);
        }
    }
}
