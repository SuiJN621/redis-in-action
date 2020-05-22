package webusage;

import java.time.Instant;
import java.util.Set;

import com.google.gson.Gson;

import redis.clients.jedis.Tuple;
import util.RedisClient;

import static util.RedisKeyConstant.DELAY_KEY;
import static util.RedisKeyConstant.INVENTORY_PREFIX;
import static util.RedisKeyConstant.SCHEDULE_KEY;

/**
 * @author Sui
 * @date 2019.08.12 14:25
 */
public class CacheService {

    public void pageCache() {
        //request hash后作为缓存key
        //setex(key, value, expire) 设置缓存时间
    }

    public void dataCache(String rowId, Long delay) {
        RedisClient.getInstance().zadd(SCHEDULE_KEY, (double) Instant.now().getEpochSecond(), rowId);
        RedisClient.getInstance().zadd(DELAY_KEY, (double) delay, rowId);
    }

    public void pageAnalyse() {

    }

    /**
     * 缓存数据后台线程
     */
    private static class DataCacheThread extends Thread {

        private volatile boolean quit;

        public DataCacheThread() {
            this.quit = false;
            this.setDaemon(true);
        }

        @Override
        public void run() {
            Gson gson = new Gson();
            while (!quit) {
                //获取第一条任务
                Set<Tuple> range = RedisClient.getInstance().zrangeWithScores(SCHEDULE_KEY, 0, 0);
                Tuple next = range.size() > 0 ? range.iterator().next() : null;
                long now = Instant.now().getEpochSecond();
                //无需要缓存或还未到缓存时间
                if (next == null || next.getScore() > now) {
                    try {
                        sleep(500);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }

                String rowId = next.getElement();
                double delay = RedisClient.getInstance().zscore(DELAY_KEY, rowId);
                //刷新时间小于0视为删除
                if (delay <= 0) {
                    RedisClient.getInstance().zrem(DELAY_KEY, rowId);
                    RedisClient.getInstance().zrem(SCHEDULE_KEY, rowId);
                    RedisClient.getInstance().del(INVENTORY_PREFIX + rowId);
                    continue;
                }

                //刷新缓存
                String data = getData(rowId);
                RedisClient.getInstance().zadd(SCHEDULE_KEY, now + delay, rowId);
                RedisClient.getInstance().set(INVENTORY_PREFIX + rowId, gson.toJson(data));
            }
        }

        public void quit() {
            quit = true;
        }

        private String getData(String rowId) {
            return "data:" + rowId;
        }
    }
}
