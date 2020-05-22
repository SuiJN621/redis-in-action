package util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import redis.clients.jedis.Jedis;

/**
 * @author Sui
 * @date 2019.08.06 10:20
 */
public class RedisClient {

    private static Jedis jedis;

    static {
        jedis = new Jedis("192.168.112.130");
        jedis.connect();
        boolean connected = jedis.isConnected();
        System.out.println(connected);
    }

    public static Jedis getInstance() {
        return jedis;
    }

    public static <T> T getObject(String objectKey, Class<T> tClass) {
        Map<String, String> params = jedis.hgetAll(objectKey);
        return ModelMapperUtils.map(params, tClass);
    }

    public static void putObject(String objectKey, Object object) {
        Class<?> clazz = object.getClass();
        List<Field> declaredFields = Arrays.asList(clazz.getDeclaredFields());
        Map<String, String> map = declaredFields.stream().collect(Collectors.toMap(Field::getName, f -> {
            f.setAccessible(true);
            String value = null;
            try {
                value = String.valueOf(f.get(object));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            f.setAccessible(false);
            return value;
        }));
        jedis.hmset(objectKey, map);
    }

    public static boolean addToSet(String setKey, String value) {
        return 1 == jedis.sadd(setKey, value);
    }
}
