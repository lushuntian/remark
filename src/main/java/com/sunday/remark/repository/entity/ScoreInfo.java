package com.sunday.remark.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评分信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreInfo {
    private short score;

    private long count;
}
