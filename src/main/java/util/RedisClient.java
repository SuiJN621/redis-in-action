package util;

import java.lang.reflect.Field;
import java.util.Map;

import javax.print.attribute.standard.Fidelity;

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
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            try {
                String value = String.valueOf(field.get(object));
                String name = field.getName();
                jedis.hset(objectKey, name, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            field.setAccessible(false);
        }
    }

    public static boolean addToSet(String setKey, String value) {
        return 1 == jedis.sadd(setKey, value);
    }
}
