package localrank;

import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import redis.clients.jedis.ZParams;
import util.RedisClient;

/**
 * @author Sui
 * @date 2019.08.07 10:29
 */
public class ArticleService {

    private static final int WEEK_SECONDS = 7 * 86400;
    private static final double VOTE_SCORE = 432;

    private static final String ARTICLE_PREFIX = "article:";
    private static final String VOTED_PREFIX = "voted:";
    private static final String DEVOTED_PREFIX = "devoted:";
    private static final String TIME_PREFIX = "time:";
    private static final String SCORE_PREFIX = "score:";
    private static final String USER_PREFIX = "user:";
    private static final String GROUP_PREFIX = "group:";

    /**
     * 创建文章
     *
     * @param userId
     * @param title
     * @param link
     * @return
     */
    public static Long postArticle(String userId, String title, String link) {
        Article article = new Article();
        article.setPoster(USER_PREFIX + userId);
        article.setTime((double) Instant.now().getEpochSecond() + new Random().nextInt(7 * 80000));
        article.setVotes(1);
        article.setLink(link);
        article.setTitle(title);

        //创建文章对象
        Long id = RedisClient.getInstance().incr(ARTICLE_PREFIX);
        RedisClient.putObject(ARTICLE_PREFIX + id, article);

        //增加投票列表
        RedisClient.addToSet(VOTED_PREFIX + id, USER_PREFIX + userId);
        RedisClient.getInstance().expire(VOTED_PREFIX + id, WEEK_SECONDS);

        //增加时间分数
        RedisClient.getInstance().zadd(TIME_PREFIX, article.getTime(), ARTICLE_PREFIX + id);
        //增加投票分数
        RedisClient.getInstance().zadd(SCORE_PREFIX, article.getTime() + VOTE_SCORE, ARTICLE_PREFIX + id);

        return id;
    }

    /**
     * 为文章投票
     *
     * @param userId
     * @param articleId
     */
    public static void vote(String userId, String articleId, boolean agree) {
        String articleKey = ARTICLE_PREFIX + articleId;
        String votesField = "votes";
        String devotesField = "devotes";

        String user = USER_PREFIX + userId;
        String votedKey = VOTED_PREFIX + articleId;
        String devotedKey = DEVOTED_PREFIX + articleId;

        //获取时间分数并检查
        Double timeScore = RedisClient.getInstance().zscore(TIME_PREFIX, articleKey);
        boolean timeCheck = timeScore > Instant.now().getEpochSecond() - WEEK_SECONDS;
        if (timeCheck) {
            boolean replicaVote = false;
            int ratio = agree ? 1 : -1;
            String source = agree ? devotedKey : votedKey;
            String destination = agree ? votedKey : devotedKey;
            //是否投过票
            Boolean haveVoted = RedisClient.getInstance().sismember(source, user);
            if(haveVoted) {
                //改票为有效投票
                RedisClient.getInstance().smove(source, destination, user);
                ratio *= 2;
                //减少票数
                RedisClient.getInstance().hincrBy(articleKey, agree ? devotesField : votesField, -1L);
            } else {
                replicaVote = !RedisClient.addToSet(destination, user);
            }

            //有效投票
            if(!replicaVote){
                //修改分数
                RedisClient.getInstance().zincrby(SCORE_PREFIX, ratio * VOTE_SCORE, articleKey);
                //增加票数
                RedisClient.getInstance().hincrBy(articleKey, agree ? votesField : devotesField, 1L);
            }
        }
    }

    /**
     * 根据排名获取文章
     *
     * @param page
     * @return
     */
    public static List<Article> getArticlesByScore(int page) {
        int pageSize = 25;
        int start = (page - 1) * pageSize;
        int end = start + pageSize - 1;

        Set<String> ids = RedisClient.getInstance().zrevrange(SCORE_PREFIX, start, end);
        List<Article> pageContent = ids.stream().map(id -> RedisClient.getObject(id, Article.class)).collect(Collectors
                .toList());
        return pageContent;
    }

    /**
     * 根据排名获取指定分组文章
     *
     * @param page
     * @return
     */
    public static List<Article> getGroupArticlesByScore(String group, int page) {
        int pageSize = 25;
        int start = (page - 1) * pageSize;
        int end = start + pageSize - 1;

        String groupScoreKey = SCORE_PREFIX + group;
        Boolean exists = RedisClient.getInstance().exists(groupScoreKey);
        if (!exists) {
            ZParams aggregate = new ZParams().aggregate(ZParams.Aggregate.MAX);
            RedisClient.getInstance().zinterstore(groupScoreKey, aggregate, GROUP_PREFIX + group,
                    SCORE_PREFIX);
        }
        Set<String> ids = RedisClient.getInstance().zrevrange(groupScoreKey, start, end);
        List<Article> pageContent = ids.stream().map(id -> RedisClient.getObject(id, Article.class)).collect(Collectors
                .toList());
        return pageContent;
    }

    public static void updateGroup(String articleId, boolean add, List<String> groups) {
        String articleKey = ARTICLE_PREFIX + articleId;
        groups.forEach(group -> {
            if (add) {
                RedisClient.getInstance().sadd(GROUP_PREFIX + group, articleKey);
            } else {
                RedisClient.getInstance().srem(GROUP_PREFIX + group, articleKey);
            }
        });
    }
}
