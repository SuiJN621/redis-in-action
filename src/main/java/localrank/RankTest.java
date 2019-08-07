package localrank;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Test;

/**
 * @author Sui
 * @date 2019.08.06 16:42
 */
public class RankTest {

    @Test
    public void testPostArticle() {
        Random random = new Random();

        for (int i = 0; i < 100; i++) {
            String title = "title" + random.nextInt(100);
            Long aLong = ArticleService.postArticle(String.valueOf(random.nextInt(100)),
                    title, "http://articles/" + title);
            System.out.println("================文章" + aLong + "创建==============");
        }
    }

    @Test
    public void testGetArticles(){
        List<Article> articlesByScore = ArticleService.getArticlesByScore(1);
    }

    @Test
    public void testVotes(){
        ArticleService.vote("1", "68", false);
    }

    @Test
    public void testUpdateGroup(){
        ArticleService.updateGroup("8", true, Collections.singletonList("tiyu"));
        ArticleService.updateGroup("31", true, Collections.singletonList("tiyu"));
        ArticleService.updateGroup("1", true, Collections.singletonList("tiyu"));
    }

    @Test
    public void testGetGroupArticles(){
        List<Article> manhua = ArticleService.getGroupArticlesByScore("manhua", 1);
        List<Article> tiyu = ArticleService.getGroupArticlesByScore("tiyu", 1);
    }
}
