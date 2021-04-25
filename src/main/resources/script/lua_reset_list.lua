--删除并创建列表
--params    1           2
--KEYS      列表键名      代理键
--ARGV      列表

redis.call('DEL', KEYS[1])
for i= 1, #ARGV do
    redis.call('RPUSH', KEYS[1], ARGV[i])
end

--延长代理锁的过期时间
redis.call('SET', KEYS[2], 1)
redis.call('EXPIRE',KEYS[2], 3600)




