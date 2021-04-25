package com.sunday.file.repository;

import com.sunday.file.Sunday;
import com.sunday.file.repository.entity.Vote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class VoteRepository {
    @Autowired
    private JdbcTemplate db;

    /**
     * 添加点赞
     * @param vote 点赞对象
     * @return 如果插入成功，返回true，否则返回false
     */
    public boolean addVote(/*valid*/ Vote vote) {
        String sql = "insert into vote_document(voter_id,content_id,voting,votes,create_date) values(?,?,?,?,?)";
        return db.update(sql, vote.getVoterId(), vote.getContentId(), vote.getVoting(), vote.getVotes(), Sunday.getDate()) > 0;
    }
}







