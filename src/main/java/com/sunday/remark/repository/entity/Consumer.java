package com.sunday.remark.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Consumer {
    private long id;

    private String mobile;

    private String nickname;

    private String face;

    private String email;

    private String extension;
}