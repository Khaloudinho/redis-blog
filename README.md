# Nom du projet
- redis-blog

# Auteur
- Khaled Bouguettoucha

# Description
- TP pour l'université afin de prendre en main l'outil Redis (et Jedis sous Java)

# Questions
- question a) Quelle structure de Redis vous paraît être la plus appropriée ?
- réponse a) Un HashSet est la structure idéale pour le problème posé de nos articles de blog. 
D'une part parce que l'article est unique et d'autre part la liste clef valeur est idéale pour stocker chacune des propriétés d'un article.

- question b) On veut également pouvoir accéder aux articles de manière séquentielle (une timeline des articles) et également en fonction de leur score. 
Quelles structures de Redis vous paraissent les plus adaptées pour ces deux besoins ?
- réponse b) Une SortedSed est la structure idéale pour trier selon la date pour la timeline d'article (date plus récente en premier) et également selon les scores (selon le besoin). 
On a un identifiant unique d'un article avec son timestamp (article:1 -> 1511780179) et la même chose dans un autre SortedSed pour le score (article:1 -> 1511780636)

- question c) On veut aussi connaître les utilisateurs qui ont voté pour un article. Même question que précédemment concernant la structure.
réponse c) Un HashSet est la structure à préconiser pour savoir quel utilisateur a voté pour quel article. 
En effet, chaque utilisateur peut voter qu'une seule fois pour un article donc le HashSet est idéal car il ne contient pas de doublons.
Notre HashSet contiendra par exemple (selectionne:1 -> gregory) qui signifie que l'utilisateur "gregory" a voté pour l'article 1.

# Fonctionnalités implémentées
- ajouter un article : void ajoutArticle(Jedis conn, String utilisateur, String titre, String url)
- voter pour un article : void voterPourArticle(Jedis conn, String utilisateur, String articleId)
- récupérer tous les articles : Set<String> recupererListeArticles(Jedis conn)
- récupérer les dix articles ayant reçu le plus de votes : Set<String> recupererDixArticlesLesPlusVotes(Jedis conn)
- récupérer le score d'un article : double recupererScorePourUnArticle(Jedis conn, String articleId) 
- associer un article à une catégorie : void definirCategoriePourUnArticle(Jedis conn, String articleId, String categorie)
- supprimer un article d'une catégorie à laquelle il appartient : void supprimerArticleDeCategorie(Jedis conn, String articleId, String categorie)
- récupérer tous les articles d'une catégorie : Set<String> recupererArticlesDeCategorie(Jedis conn, String categorie)
- récupérer la catégorie d'un article : Set<String> recupererCategoriePourUnArticle(Jedis conn, String articleId)
- récupérer les scores de tous les articles d'une catégorie donnée : Set<Pair<String, Double>> recupererScoreDesArticlesDeCategorie(Jedis conn, String categorie)

# Test réalisés
- ajouter un article : void ajoutArticleTest()
- voter pour un article : void voterPourArticleTest()
- récupérer tous les articles : void recupererListeArticlesTest()
- récupérer les dix articles ayant reçu le plus de votes : void recupererDixArticlesLesPlusVotesTest()
- récupérer le score d'un article : void recupererScorePourUnArticleTest() 
- associer un article à une catégorie : void definirCategoriePourUnArticleTest()
- supprimer un article d'une catégorie à laquelle il appartient : void supprimerArticleDeCategorieTest()
- récupérer tous les articles d'une catégorie : void recupererArticlesDeCategorieTest()
- récupérer la catégorie d'un article : void recupererCategoriePourUnArticleTest()
- récupérer les scores de tous les articles d'une catégorie donnée : void recupererScoreDesArticlesDeCategorieTest()

# Outils utilisés
- Jedis v2.9.0
- JUnit v4.12