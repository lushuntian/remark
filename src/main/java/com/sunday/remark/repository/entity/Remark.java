package com.sunday.remark.repository.entity;

import com.sunday.remark.service.base.IWordValidator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Remark {
    private long id;

    private long consumerId;

    private String itemId;

    private String orderId;

    private short score;

    private String header;

    private String content;

    private String images;

    private String createTime;

    private String username;

    private String userface;

    /**
     * 校验：发布评价
     * @param validator 敏感词校验器
     */
    public void validateForCreate(IWordValidator validator) {
        Assert.isTrue(consumerId > 0, "用户id不能为空");
        Assert.isTrue(!StringUtils.isEmpty(orderId), "订单id不可为空");
        Assert.isTrue(score >= 20 && score <= 100 && score % 10 == 0, "评分不合规");
        Assert.isTrue(!StringUtils.isEmpty(header), "标题不能为空");
        Assert.isTrue(header.length() <= 15, "标题不能超过15字");
        Assert.isTrue(!StringUtils.isEmpty(content), "内容不能为空");
        Assert.isTrue(content.length() <= 150, "内容不能超过150字");
        Assert.isTrue(content.length() >= 10, "内容不能少于10字");
        Assert.isTrue(images == null || images.split(" ").length <= 3, "图片不能超过3个");

        Assert.isTrue(!validator.checkHasSensitiveWords(header, content), "您发布的评价内容不符合规范，请修改");
    }

    /**
     * 校验：删除评价
     */
    public void validateForDelete() {
        Assert.isTrue(consumerId > 0, "用户id不能为空");
        Assert.isTrue(!StringUtils.isEmpty(orderId), "订单id不可为空");
        Assert.isTrue(!StringUtils.isEmpty(itemId), "商品id不可为空");
        Assert.isTrue(id > 0, "评价id不可为空");
        Assert.isTrue(score >= 20 && score <= 100 && score % 10 == 0, "评分不合规");
    }


}
