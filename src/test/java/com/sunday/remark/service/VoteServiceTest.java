package com.sunday.remark.service;

import com.sunday.remark.RemarkApplication;
import com.sunday.remark.component.BaseTest;
import com.sunday.remark.component.MockMq;
import com.sunday.remark.component.MockVoteBox;
import com.sunday.remark.service.exception.VoteException;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes={RemarkApplication.class})
@RunWith(SpringRunner.class)
class VoteServiceTest extends BaseTest {
    @Autowired
    private MockVoteBox voteBox;

    private final MockMq mockMq = new MockMq();

    @Test
    void vote() {
        VoteService voteService = new VoteService(voteBox, mockMq);

        //投票
        try {
            int v = voteService.vote(1, 1, true);
            assertEquals(1, v);
            assertTrue(mockMq.isFlag());

            v = voteService.vote(2, 1, true);
            assertEquals(2, v);

            v = voteService.vote(3, 1, true);
            assertEquals(3, v);

            //重复投票，无效
            v = voteService.vote(1, 1, true);
            assertEquals(3, v);

            v = voteService.vote(2, 2, true);
            assertEquals(1, v);

            v = voteService.vote(2, 3, true);
            assertEquals(1, v);
        } catch (VoteException e) {
            e.printStackTrace();
            fail();
        }


    }


}







