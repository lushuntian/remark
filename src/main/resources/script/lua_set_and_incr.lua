
--点赞操作，写入并自增，如果写入失败则不自增，【原子性、幂等性】
if redis.call('SETNX',KEYS[1],1) == 1
then
    redis.call('EXPIRE',KEYS[1],864000)
    redis.call('INCR',KEYS[2])
end
return redis.call('GET',KEYS[2])



