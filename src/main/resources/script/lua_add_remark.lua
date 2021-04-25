--添加评价的预置脚本
--params    1            2             3         4
--KEYS      代理键名       评价键名       锁键名      评分键
--ARGV      评价内容       评价id        锁有效期     评分
--return    0:成功添加评价  1:加锁失败，遗弃   2:加锁失败，需要重试   3:加锁成功

--代理键是否过期
if redis.call('EXISTS', KEYS[1]) == 1 then
    --未过期，添加评价
    redis.call('LPUSH', KEYS[2], ARGV[1])
    redis.call('LTRIM', KEYS[2], 0, 50)
    
    --当且仅当评分键存在时修改评分，避免覆盖冷启动
    if redis.call('EXISTS', KEYS[4]) == 1 then
        redis.call('HINCRBY', KEYS[4], ARGV[4], 1)
    end
    
    return 0
else
    local v = redis.call('GET', KEYS[3])
    if v then
        --锁未过期，加锁失败
        if ARGV[2] > v then
            --待添加的评价的id大于当前id，说明是新内容，需要客户端重试
            return 2
        else
            --否则需要遗弃，因为加锁成功的客户端会帮它加载到redis
            return 1
        end
    else
        --加锁成功
        redis.call('SET', KEYS[3], ARGV[2])
        redis.call('EXPIRE',KEYS[3], ARGV[3])
        return 3
    end
end




