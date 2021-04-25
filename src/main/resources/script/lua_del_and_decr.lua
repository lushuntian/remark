
--取消点赞操作，删除并递减，如果删除失败则不递减，【原子性、幂等性】
if redis.call('DEL',KEYS[1]) == 1
then
    redis.call('DECR',KEYS[2])
end
return redis.call('GET',KEYS[2])



