package util;

/**
 * @author Sui
 * @date 2019.08.12 13:58
 */
public class RedisKeyConstant {

    /**
     * local rank
     */
    public static final String ARTICLE_PREFIX = "article:";
    public static final String VOTED_PREFIX = "voted:";
    public static final String DEVOTED_PREFIX = "devoted:";
    public static final String TIME_PREFIX = "time:";
    public static final String SCORE_PREFIX = "score:";
    public static final String USER_PREFIX = "user:";
    public static final String GROUP_PREFIX = "group:";

    /**
     * web usage
     */
    public final static String LOGIN_TOKEN_KEY = "login:";
    public final static String RECENT_LOGIN_KEY = "recent:";
    public final static String VIEWED_KEY_PREFIX = "viewed:";
    public final static String CART_PREFIX = "cart:";

    /**
     * cache
     */
    public final static String SCHEDULE_KEY = "schedule:";
    public final static String DELAY_KEY = "delay:";
    public final static String INVENTORY_PREFIX = "inventory:";
}
