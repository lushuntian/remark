package com.sunday.file.repository.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Vote {
    private long voterId;

    private long contentId;

    private int voting;

    private long votes;

    public void validate(){
        Assert.isTrue(voterId > 0, "用户id不能为空");
        Assert.isTrue(contentId > 0, "内容id不能为空");
        Assert.isTrue(voting == 0 || voting == 1, "投票状态错误");
        Assert.isTrue(votes >= 0, "投票总数错误");
    }
}