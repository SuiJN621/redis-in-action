## redis-in-action

### redis 特点
内存存储, 持久化, 远程连接(同时多个客户端连接), 可扩展(主从复制, 分片)

### redis 基本操作
#### String 字符串/整数/浮点数
set/get/del  
#### List 链表, 每个节点都是String
rpush/lpush key value    
rpop/lpop key    
lrange key start end(0代表第一个, -1代表最后一个元素)    
#### Set 无序散列表(只有键)
sadd/srem key value    
sismember key value    
smembers key   
smove source destination member     
#### Hash 键值对的无序散列表
hset key field value    
hget key field    
hgetall key    
hdel key field    
#### ZSet 有序散列表(键为字符串, 值为score, 根据score排序)
zadd key score member  
zrange/zrevrange(根据分值升序/降序) key start end [withscores]  
zrangebyscore key lowScore highScore [withscores]  
zrem key member  
zinterstore destination setnums set... [weight w...] [aggregate max|min|sum]  

### redis 在Web应用