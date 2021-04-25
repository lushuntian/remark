package com.sunday.file.receive;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.Message;
import com.sunday.file.FileApplication;
import com.sunday.file.repository.VoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes={FileApplication.class})
@RunWith(SpringRunner.class)
class VoteMessageReceiverTest {
    @Autowired
    private VoteRepository voteRepository;

    @Test
    void consume() {
        VoteMessageReceiver receiver = new VoteMessageReceiver(voteRepository);

        Message message = new Message();
        message.setBody("""
                {
                  "voterId": 1,
                  "contentId": 1,
                  "voting": 1,
                  "votes": 1
                }
                """.getBytes());

        assertEquals(Action.CommitMessage, receiver.consume(message, null));

        message.setBody("""
                {
                  "voterId": 1,
                  "contentId": 1,
                  "voting": -1,
                  "votes": 1
                }
                """.getBytes());

        assertEquals(Action.CommitMessage, receiver.consume(message, null));

        message.setBody("""
                {
                  "voterId": 1,
                  "contentId": 1,
                  "voting": 0,
                  "votes": 1
                }
                """.getBytes());

        assertEquals(Action.CommitMessage, receiver.consume(message, null));

    }
}