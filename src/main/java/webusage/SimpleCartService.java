package webusage;

import util.RedisClient;

import static util.RedisKeyConstant.CART_PREFIX;

/**
 * @author Sui
 * @date 2019.08.12 13:48
 */
public class SimpleCartService {

    public void addItem(String token, String item, Integer count) {
        if (count > 0) {
            RedisClient.getInstance().hset(CART_PREFIX + token, item, String.valueOf(count));
        } else {
            RedisClient.getInstance().hdel(CART_PREFIX + token, item);
        }
    }
}
