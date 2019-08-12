## redis-in-action

### redis 特点
内存存储, 持久化, 远程连接(同时多个客户端连接), 可扩展(主从复制, 分片)

### redis 基本操作
#### 连接操作
select db_num: 选择数据库(默认0 通过db_number值修改)  

#### 服务器操作
client pause timeout: 阻塞客户端响应timeout毫秒  
flushdb: 清空数据库  
flushall: 清空所有数据库  
dbsize: 返回当前数据库key数量  
save: 所有数据快照以RDB文件保存到硬盘  
lastsave: 最后一次save时间戳  
config get p: 查询p配置参数的值   
config set p v: 设置参数p为v, 无需重启  
slaveof host port: 将机器作为制定服务器的从属服务器(当前为其他从属时, 旧数据丢弃, 同步新数据);
                   slaveof no one 会使当前服务器变成主服务器, 之前同步数据不丢弃(灾备使用)  
cluster slots: 返回集群实例详情  
role: 返回当前实例在集群中的角色(master/slave/sentinel)  

#### 事务操作
mulit: 开启事务
exec: 执行事务内的命令
discard: 取消事务

#### key 操作
**del key**: 删除指定key      
**expire key seconds**: 设置key超时时间(秒)    
expire key unix_timestamp: 设置超时时间(时间戳)    
**exists key**: 检查key是否存在      
keys pattern: 返回匹配pattern的key值      
**ttl key**: 返回key值剩余生存时间(秒); -1表示不存在(2.8之后为-2)/无超时时间    
**persist key**: 移除key超时时间      
type key: 返回key类型(none 不存在)      

#### String 字符串/整数/浮点数
##### 修改
**set key value**: 设置字符串(无视类型)    
mset(msetnx) key1 value1 key2 value2 ...: 同时设置多个    
setnx key value: 当key不存在时设置value    
setex key time value: 同时设置过期时间和值   
decr/incr key: 返回数值减/加1(不存在先初始化为0)   
decrby/incrby key amount: 减去/加上指定数量  
append key tail: tail追加到字符串尾部  
**getset key value**: 设置并返回原值  
##### 获取
**get key**: 不存在返回nil, 其他类型报错  
mget key1 key2 ...: 获取多个  
getrange key start end: 截取子字符串  
strlen key: 字符串长度  
 
#### List 链表, 每个节点都是String
##### 获取
lindex key index: 获取索引位置    
*lrange key start end*: 获取索引范围元素  
llen key: 列表长度  
##### 修改
*rpush/lpush key value1 value2 ...*: 多个值加入链表尾部(头部)     
lpop/rpop key: 弹出第一个(最后一个)元素  
blpop/brpop key1 key2 ... timeout: 弹出第一个(最后一个元素), 没有元素时阻塞到超时(秒), 返回弹出元素的key和value  
lrem key count value: count不等0(从左/右搜索最多count个等于value的删除) count等于0全部删除   
lset key index value: 设置指定位置  
   
#### Set 无序散列表(只有键)
##### 修改
sunion key1 key2 ...: 返回source的并集(不存在视为空集)  
sinter key1 key2 ...: 返回source的交集(不存在视为空集)  
sdiff key1 key2 ...: 返回差集(不存在视为空集)  
sunionstore/sinterstore/sdiffstore destination key1 key2 ...: 操作后的集合保存到destination中  
sadd key m1 m2 ...: 增加多个成员(2.4之前只能单个)  
srem key m1 m2 ...: 移除多个成员(2.4之前只能单个)  
smove source destination member: 原子操作, 移动元素, source不存在时, 不移动返回0;  
spop key: 移除并返回随机成员  
##### 获取
scard key: 获取成员数  
sismember key member: 指定成员是否存在        
smembers key: 返回所有成员       
     
#### Hash 键值对的无序散列表
##### 获取
hget key field: 获取指定字段的值  
hmget key f1 f2 ...: 获取多个字段的值  
hgetall key: 获取全部字段的f和v  
hexists key f: 指定字段是否存在    
hlen key: 获取字段数量  
hvals key: 获取所有值
hkeys key: 获取所有f
##### 修改
hset key field value: 设置指定字段     
hmset key f1 v1 f2 v2: 设置多个  
hincryby key f a: 指定字段增加a(不存在初始化0)  
hdel key f1 f2 ...: 删除字段  
 
#### ZSet 有序散列表(键为字符串, 值为score, 根据score排序)
##### 获取
zcark key: 获取成员数量
zscore key member: 获取成员分数
zcount key low_score high_score: 计算指定分数范围内成员数量
zrank/zrevrank key member: 获取指定成员的排名(按分支升序/降序)  
zrange/zrevrange key start end [withscores]: 获取指定排名范围的成员  
zrangebyscore/zrevrangebyscore key low_score high_score [withscores]: 获取指定分数范围内成员  
zinterstore/zunionstore destination set_nums set... [weight w...] [aggregate max|min|sum]: 
根据权重计算分值(最大值/最小值/和) 返回交集/并集存储到destination  
##### 修改
zadd key s1 m1 s2 m2 ...: 增加多个成员(2.4支持多个)
zremrangebyrank key start end: 移除指定排名内成员  
zremrangebyscore key lscore hscore: 移除指定分数内成员  
zrem key m1 m2...: 移除指定成员(2.4之后多个)
zincryby key amount member: 指定成员增加amount分数(key/member不存在时, 先初始化并分值赋值为0) 
   

### redis 在Web应用