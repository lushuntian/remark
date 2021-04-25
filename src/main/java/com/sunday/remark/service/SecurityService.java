package com.sunday.remark.service;

import com.sunday.remark.service.base.IWordValidator;
import org.springframework.stereotype.Service;

/**
 * 安全服务
 */
@Service
public class SecurityService implements IWordValidator {
    /**
     * 检查是否存在敏感词
     * @param contents 词汇
     * @return 如果存在敏感词，返回true，否则返回false
     */
    @Override
    public boolean checkHasSensitiveWords(String... contents){
        return false;
    }
}
