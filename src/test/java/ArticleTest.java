import fr.miage.m2.Article;
import javafx.util.Pair;
import org.junit.Test;
import redis.clients.jedis.Jedis;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
            String[] categories = {"programmation", "design", "web", "Java"};
            Article.definirCategoriePourUnArticle(conn, articleId, categories[(int) Math.floor(Math.random() * 4)]);
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

    @Test
    public void recupererScorePourUnArticleTest(){
        Jedis conn = new Jedis(host, port);

        String articleId = "2";
        double unScore = Article.recupererScorePourUnArticle(conn, articleId);
        double scoreEnBase = conn.zscore("score:article", "article:" + articleId);

        assertEquals(scoreEnBase, unScore, 0);
    }

    @Test
    public void definirCategoriePourUnArticleTest(){
        Jedis conn = new Jedis(host, port);
        cleanUp();

        String titre = "little article by zerolex";
        String utilisateur = "zerolex";
        String lien = "http://www.foo.org/articles/article/1";
        Article.ajoutArticle(conn, titre, utilisateur, lien);

        String articleId = "1";
        Article.definirCategoriePourUnArticle(conn, articleId, "new category");

        // Un article appartient à une seule catégorie
        assertEquals(1, Article.recupererCategoriePourUnArticle(conn, articleId).size());

        // On vérifie qu'il appartient bien à la catégorie qu'on vient de lui assigner
        assertTrue(Article.recupererCategoriePourUnArticle(conn, articleId).contains("new category"));
    }

    @Test
    public void supprimerArticleDeCategorieTest(){
        Jedis conn = new Jedis(host, port);
        cleanUp();

        String titre = "little article by zerolex";
        String utilisateur = "zerolex";
        String lien = "http://www.foo.org/articles/article/1";
        Article.ajoutArticle(conn, titre, utilisateur, lien);

        String articleId = "1";
        String categorie = "new category";
        Article.definirCategoriePourUnArticle(conn, articleId, categorie);

        // On vérifie que l'article 1 appartient à new category
        assertTrue(Article.recupererCategoriePourUnArticle(conn, articleId).contains(categorie));

        // On fait la suppression et on vérifie
        Article.supprimerArticleDeCategorie(conn, articleId, categorie);
        assertFalse(Article.recupererCategoriePourUnArticle(conn, articleId).contains(categorie));
    }

    @Test
    public void recupererArticlesDeCategorieTest(){
        Jedis conn = new Jedis(host, port);
        cleanUp();

        String titre = "little article by zerolex - nb ";
        String utilisateur = "zerolex";
        String lien = "http://www.foo.org/articles/article/";
        Article.ajoutArticle(conn, titre + "1", utilisateur, lien + "1");
        Article.ajoutArticle(conn, titre + "2", utilisateur, lien + "2");

        String categorie = "new category";
        Article.definirCategoriePourUnArticle(conn, "1", categorie);
        Article.definirCategoriePourUnArticle(conn, "2", categorie);

        Set<String> articlesDeNewCategory = Article.recupererArticlesDeCategorie(conn, categorie);
        assertEquals(conn.smembers("categorie:new category"), articlesDeNewCategory);
    }

    @Test
    public void recupererCategoriePourUnArticleTest(){
        Jedis conn = new Jedis(host, port);
        cleanUp();

        String titre = "little article by zerolex";
        String utilisateur = "zerolex";
        String lien = "http://www.foo.org/articles/article/1";
        Article.ajoutArticle(conn, titre, utilisateur, lien);

        String articleId = "1";
        String categorie = "new category";
        Article.definirCategoriePourUnArticle(conn, articleId, categorie);

        // On vérifie qu'on a une seule catégorie pour l'article
        assertEquals(1, Article.recupererCategoriePourUnArticle(conn, "1").size());

        // On vérifie qu'on a bien récupéré la bonne catégorie
        assertTrue(Article.recupererCategoriePourUnArticle(conn, "1").contains("new category"));
    }

    @Test
    public void recupererScoreDesArticlesDeCategorieTest(){
        Jedis conn = new Jedis(host, port);
        cleanUp();

        Set<Pair<String, Double>> articlesEtScores = new HashSet<>();

        String titre = "little article by zerolex - nb ";
        String utilisateur = "zerolex";
        String lien = "http://www.foo.org/articles/article/";
        Article.ajoutArticle(conn, titre + "1", utilisateur, lien + "1");
        Article.ajoutArticle(conn, titre + "2", utilisateur, lien + "2");
        Article.ajoutArticle(conn, titre + "3", utilisateur, lien + "3");

        String categorie = "the category";
        Article.definirCategoriePourUnArticle(conn, "1", categorie);
        Article.definirCategoriePourUnArticle(conn, "2", categorie);
        Article.definirCategoriePourUnArticle(conn, "3", categorie);

        Set<String> articleDeTheCategory = Article.recupererArticlesDeCategorie(conn, "the category");

        for (String article : articleDeTheCategory){
            articlesEtScores.add(new Pair<>(article, conn.zscore("score:article", article)));
        }

        assertEquals(articlesEtScores, Article.recupererScoreDesArticlesDeCategorie(conn, categorie));

    }
}
