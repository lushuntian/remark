
--优先级锁，如果设置的值大于目标值，返回成功，否则返回失败
local v = redis.call('GET', KEYS[1])
if(v)
then
    if ARGV[1] > v
    then
        redis.call('SET', KEYS[1], ARGV[1])
        redis.call('EXPIRE',KEYS[1], ARGV[2])
        return true
    else
        return false
    end
else
    redis.call('SET', KEYS[1], ARGV[1])
    redis.call('EXPIRE',KEYS[1], ARGV[2])
    return true
end



