package com.sunday.remark.controller.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Vote {
    private long voterId;

    private long contentId;

    private int voting;

    public void validate(){
        Assert.isTrue(voterId > 0, "用户id不能为空");
        Assert.isTrue(contentId > 0, "内容id不能为空");
        Assert.isTrue(voting == 0 || voting == 1, "投票状态错误");
    }
}
