package fr.miage.m2;

import javafx.util.Pair;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Article {

    private static final int UNE_SEMAINE = 604800;
    private static final int SCORE_VOTE = 457;

    public static String ajoutArticle(Jedis conn, String utilisateur, String titre, String url) {
        String articleId = String.valueOf(conn.incr("article:"));
        String articleSelectionne = "selectionne:" + articleId;
        conn.sadd(articleSelectionne, utilisateur);
        conn.expire(articleSelectionne, UNE_SEMAINE);
        long now = System.currentTimeMillis() / 1000;
        String article = "article:" + articleId;
        HashMap<String,String> donnees = new HashMap<>();
        donnees.put("titre", titre);
        donnees.put("lien", url);
        donnees.put("utilisateur", utilisateur);
        donnees.put("timestamp", String.valueOf(now));
        donnees.put("nbvotes", "1");
        donnees.put("score", String.valueOf(now + SCORE_VOTE));
        conn.hmset(article, donnees);
        conn.zadd("time:article", now, article);
        conn.zadd("nbvotes:article", 0, article);
        conn.zadd("score:article", now + SCORE_VOTE, article);
        return articleId;
    }

    public static void voterPourArticle(Jedis conn, String utilisateur, String articleId) {
        if (!conn.smembers("selectionne:" + articleId).contains(utilisateur)) {
            conn.sadd("selectionne:" + articleId, utilisateur);
            conn.hincrBy("article:" + articleId, "nbvotes", 1);
            conn.expire("selectionne:" + articleId, UNE_SEMAINE);
            conn.zincrby("nbvotes:article", 1, "article:" + articleId);
            conn.zincrby("score:article", SCORE_VOTE, "article:" + articleId);
        }
    }

    public static Set<String> recupererListeArticles(Jedis conn) {
        Set<String> articles = conn.zrange("time:article", 0, -1);
        System.out.println("\n********** La liste de tous les articles sont ci-dessous : ");
        for (String article : articles) System.out.println(article);
        System.out.println("**********************************************************");
        return articles;
    }

    public static Set<String> recupererDixArticlesLesPlusVotes(Jedis conn) {
        Set<String> articles = conn.zrevrange("nbvotes:article" , 0, 9);
        System.out.println("\n********** Les dix articles ayant reçu le plus de votes sont ci-dessous : ");
        for (String article : articles) System.out.println(article);
        System.out.println("*************************************************************************");
        return articles;
    }

    public static double recupererScorePourUnArticle(Jedis conn, String articleId) {
        return conn.zscore("score:article", "article:" + articleId);
    }

    public static void definirCategoriePourUnArticle(Jedis conn, String articleId, String categorie) {
        conn.sadd("categorie:article:" + articleId, categorie);
        conn.sadd("categorie:" + categorie, "article:" + articleId);
    }

    public static void supprimerArticleDeCategorie(Jedis conn, String articleId, String categorie) {
        conn.srem("categorie:article:" + articleId, categorie);
        conn.srem("categorie:" + categorie, "article:" + articleId);
    }

    public static Set<String> recupererArticlesDeCategorie(Jedis conn, String categorie) {
        return conn.smembers("categorie:" + categorie);
    }

    public static Set<String> recupererCategoriePourUnArticle(Jedis conn, String articleId) {
        return conn.smembers("categorie:article:" + articleId);
    }

    public static Set<Pair<String, Double>> recupererScoreDesArticlesDeCategorie(Jedis conn, String categorie) {

        Set<String> articlesDeCategorie = recupererArticlesDeCategorie(conn, categorie);

        Set<Pair<String, Double>> articlesAvecScores = new HashSet<>();

        for (String article : articlesDeCategorie) {
            // Petite opération à faire pour juste récupérer le n° de l'article d'ou le substring
            articlesAvecScores.add(new Pair<>(article, recupererScorePourUnArticle(conn, article.substring(8))));
        }

        return articlesAvecScores;
    }
}
