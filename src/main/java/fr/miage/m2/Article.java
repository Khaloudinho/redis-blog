package fr.miage.m2;

import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Set;

public class Article {

    private static int UNE_SEMAINE = 604800;

    public static String ajoutArticle(Jedis conn, String utilisateur, String titre, String url) {
        String articleId = String.valueOf(conn.incr("article:"));
        String articleSelectionne = "selectionne:" + articleId;
        conn.sadd(articleSelectionne, utilisateur);
        conn.expire(articleSelectionne, UNE_SEMAINE);
        long now = System.currentTimeMillis() / 1000;
        String article = "article:" + articleId;
        HashMap<String,String> donnees = new HashMap<String,String>();
        donnees.put("titre", titre);
        donnees.put("lien", url);
        donnees.put("utilisateur", utilisateur);
        donnees.put("timestamp", String.valueOf(now));
        donnees.put("nbvotes", "1");
        conn.hmset(article, donnees);
        conn.zadd("date:article", now, article);
        conn.zadd("nbvotes:article", 1, article);
        return articleId;
    }

    public static void voterPourArticle(Jedis conn, String utilisateur, String articleId) {
        if (!conn.smembers("selectionne:" + articleId).contains(utilisateur)) {
            conn.sadd("selectionne:" + articleId, utilisateur);
            conn.hincrBy("article:" + articleId, "nbvotes", 1);
            conn.expire("selectionne:" + articleId, UNE_SEMAINE);
            conn.zincrby("nbvotes:", 1, "article:" + articleId);
        }
    }

    public static Set<String> recupererListeArticles(Jedis conn) {
        Set<String> articles = conn.zrange("date:article", 0, -1);
        for (String article : articles) System.out.println(article);
        return articles;
    }

    public static Set<String> recupererDixArticlesLesPlusVotes(Jedis conn){
        Set<String> articles = conn.zrange("nbvotes:article" , 0, 9);
        for (String article : articles) System.out.println(article);
        return articles;
    }

}
