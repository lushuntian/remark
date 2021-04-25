package com.sunday.remark.service.base;

/**
 * 敏感词校验器
 */
public interface IWordValidator {
    /**
     * 检查是否存在敏感词
     * @param contents 词汇
     * @return 如果存在敏感词，返回true，否则返回false
     */
    boolean checkHasSensitiveWords(String... contents);
}
