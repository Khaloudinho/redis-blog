import fr.miage.m2.Article;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ArticleTest {

    private static final String host = "localhost";
    private static final int port = 6379;
    private static final int redis = 2;

    public static void cleanUp() {
        Jedis conn = new Jedis(host, port);
        conn.select(redis);
        if (redis != 0) conn.flushAll();
    }

    @Test
    public void ajouterArticleTest() {
        Jedis conn = new Jedis(host, port);
        cleanUp();
        String utilisateur = "gregory";
        String titre = "my article number ";
        String lien = "http://www.foo.org/articles/article/";

        for (int i = 0; i < 20; i++) {
            String articleId = Article.ajoutArticle(conn, utilisateur, titre + i, lien + i);
            assertEquals(conn.hget("article:" + articleId, "titre"), titre + i);
            assertEquals(conn.hget("article:" + articleId, "lien"), lien + i);
            assertEquals(conn.hget("article:" + articleId, "utilisateur"), utilisateur);
        }
    }

    @Test
    public void voterPourArticleTest() {
        Jedis conn = new Jedis(host, port);
        String utilisateur = "francis";
        String articleId = "1";
        Article.voterPourArticle(conn, utilisateur, articleId);
        assertTrue(conn.smembers("selectionne:" + articleId).contains(utilisateur));
    }

    @Test
    public void recupererListeArticlesTest() {
        Jedis conn = new Jedis(host, port);
        Set<String> articles = Article.recupererListeArticles(conn);
        assertEquals(1, articles.size());
    }

    @Test
    public void recupererDixArticlesLesPlusVotesTest() {
        Jedis conn = new Jedis(host, port);
        Set<String> dixArticlesLesPlusVotes = Article.recupererDixArticlesLesPlusVotes(conn);

        //assertEquals(, dixArticlesLesPlusVotes);
    }
}
