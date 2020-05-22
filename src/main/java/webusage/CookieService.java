package webusage;

import java.time.Instant;
import java.util.Set;

import redis.clients.jedis.ZParams;
import util.RedisClient;

import static util.RedisKeyConstant.CART_PREFIX;
import static util.RedisKeyConstant.LOGIN_TOKEN_KEY;
import static util.RedisKeyConstant.RECENT_LOGIN_KEY;
import static util.RedisKeyConstant.VIEWED_KEY_PREFIX;

/**
 * @author Sui
 * @date 2019.08.12 9:07
 */
public class CookieService {

    private static CleanThread CLEAN_THREAD = new CleanThread();
    private static ItemCleanThread ITEM_CLEAN_THREAD = new ItemCleanThread();

    static {
        CLEAN_THREAD.setDaemon(true);
        CLEAN_THREAD.start();
        ITEM_CLEAN_THREAD.setDaemon(true);
        ITEM_CLEAN_THREAD.start();
    }

    public static void stopCleanThread() {
        if (CLEAN_THREAD.isAlive()) {
            CLEAN_THREAD.quit();
        }
        if (ITEM_CLEAN_THREAD.isAlive()) {
            ITEM_CLEAN_THREAD.quit();
        }
    }

    /**
     * 散列表存放token和用户ID映射
     *
     * @param token
     */
    public String checkToken(String token) {
        return RedisClient.getInstance().hget(LOGIN_TOKEN_KEY, token);
    }

    public void updateToken(String userId, String token, String item) {
        Long now = Instant.now().getEpochSecond();
        //更新token
        RedisClient.getInstance().hset(LOGIN_TOKEN_KEY, token, userId);
        //添加到最近登录用户
        RedisClient.getInstance().zadd(RECENT_LOGIN_KEY, (double) now, token);
        //添加用户最近浏览商品记录
        if (item != null && !"".equals(item.trim())) {
            RedisClient.getInstance().zadd(VIEWED_KEY_PREFIX + token, (double) now, item);
            RedisClient.getInstance().zremrangeByRank(VIEWED_KEY_PREFIX + token, 0, -26);
            //商品被浏览时更新浏览排名
            RedisClient.getInstance().zincrby(VIEWED_KEY_PREFIX, (double) -1, item);
        }
    }

    private static class CleanThread extends Thread {

        private volatile boolean quit = false;
        private int limit = 10000;

        public void quit() {
            quit = true;
        }

        @Override
        public void run() {
            while (!quit) {
                Long tokenCount = RedisClient.getInstance().zcard(RECENT_LOGIN_KEY);
                if (tokenCount >= limit) {
                    int remove = (int) Math.min(tokenCount - limit, 100);
                    //获取待删除token和view信息
                    Set<String> tokens = RedisClient.getInstance().zrange(RECENT_LOGIN_KEY, 0, remove - 1);
                    String[] tokensArray = tokens.toArray(new String[0]);
                    String[] tokenViewedKeys = tokens.stream().map(token -> VIEWED_KEY_PREFIX + token).toArray
                            (String[]::new);
                    String[] tokenCartKeys = tokens.stream().map(token -> CART_PREFIX + token).toArray
                            (String[]::new);
                    //删除
                    RedisClient.getInstance().del(tokenViewedKeys);
                    RedisClient.getInstance().del(tokenCartKeys);
                    RedisClient.getInstance().hdel(LOGIN_TOKEN_KEY, tokensArray);
                    RedisClient.getInstance().zrem(RECENT_LOGIN_KEY, tokensArray);
                } else {
                    try {
                        Thread.sleep(3000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static class ItemCleanThread extends Thread {

        private volatile boolean quit = false;

        public void quit() {
            quit = true;
        }

        @Override
        public void run() {
            while (!quit) {
                RedisClient.getInstance().zremrangeByRank(VIEWED_KEY_PREFIX, 0, -20001);
                ZParams zParams = new ZParams().weightsByDouble(0.5);
                RedisClient.getInstance().zinterstore(VIEWED_KEY_PREFIX, zParams, VIEWED_KEY_PREFIX);
            }
        }
    }
}
