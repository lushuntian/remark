--查询评价的预置脚本
--params    0            1              2
--KEYS      代理键名       评价键名       锁键名
--ARGV      锁过期时间

--代理键是否过期
if redis.call('EXISTS', KEYS[1]) == 1 then
    --未过期
    return -1
else
    --获取头部内容的id
    local content = redis.call('LINDEX', KEYS[2], 0)
    local v = 0
    if content then
        local remarkJson = cjson.decode(content)
        v = cjson.decode(remarkJson)["id"]
    end
    if redis.call('SETNX', KEYS[3], v) == 1 then
        redis.call('EXPIRE', KEYS[3], ARGV[1])
        return v
    else
        --加锁失败，正常返回内容
        return -1
    end
end




