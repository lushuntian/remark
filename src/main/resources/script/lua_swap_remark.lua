--重置评价内容并释放评价分页锁
--params    1            2              3
--KEYS      评价键名       代理键名        锁键名
--ARGV      代理键有效期    锁签名          评价内容数组

--只有当签名一致时（是自己施加的锁），才执行释放锁的操作
if redis.call('GET', KEYS[3]) == ARGV[2]
then
    local remarkJson = cjson.decode(ARGV[3])
    --删除并重建列表（否则只会在原列表上增加，会产生内存泄露）
    redis.call('DEL', KEYS[1])
    for i= 1, #remarkJson do
        redis.call('RPUSH', KEYS[1], cjson.encode(remarkJson[i]) .. "")
    end

    --重置代理键
    redis.call('SET', KEYS[2], 1)
    redis.call('EXPIRE', KEYS[2], ARGV[1])

    --释放分页锁
    redis.call('DEL', KEYS[3])
end



