import fr.miage.m2.Article;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.*;
import java.util.stream.Collectors;

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
        assertEquals(20, articles.size());
    }

    @Test
    public void recupererDixArticlesLesPlusVotesTest() {
        Jedis conn = new Jedis(host, port);
        Set<String> dixArticlesLesPlusVotes = Article.recupererDixArticlesLesPlusVotes(conn);
        List<Double> scoresUn = new ArrayList<>(), scoresDeux = new ArrayList<>();

        for (int i = 1; i < 21; i++) {
            String articleId = String.valueOf(i);
            Article.voterPourArticle(conn, "joe", articleId);
            Article.voterPourArticle(conn, "john", articleId);
            Article.voterPourArticle(conn, "brooklyn", articleId);
            scoresUn.add(conn.zscore("nbvotes:article", "article:" + articleId));
        }

        for (String article : dixArticlesLesPlusVotes){
            scoresDeux.add(conn.zscore("nbvotes:article", article));
        }

        Collections.sort(scoresUn, Collections.reverseOrder());

        assertEquals(scoresUn.subList(0, 10), scoresDeux);
    }
}
