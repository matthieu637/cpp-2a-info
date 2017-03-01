# cpp-2a-info
CPP - Prépa des INP - Nancy | Projet Informatique 2ème année

Documentation officielle : https://matthieu637.github.io/cpp-2a-info/client.Reseau-class.html

Log (historique de toutes les parties en temps réel) : https://matthieu-zimmer.net/~matthieu/courses/simbourse.log

### Contributions (la suite ne concernent que les personnes voulant aller plus loin avant la compétition)
Pour les plus motiver, nous vous encourageons à améliorer le projet (il vous faudra un compte sur github, utiliser [git](https://openclassrooms.com/courses/gerer-son-code-avec-git-et-github), [forker le projet et proposer des pull requests](https://help.github.com/articles/creating-a-pull-request-from-a-fork/)).

Attention à garder vos [branches à jour](https://help.github.com/articles/syncing-a-fork/#platform-linux)
avant de pull request au risque que nos branches master divergent (https://github.com/matthieu637/cpp-2a-info/network). Cela vous permettra également de résoudre les conflits avant tout pull request :
```
#ajoute le projet officiel à votre fork
git remote add upstream https://github.com/matthieu637/cpp-2a-info.git
#récupère les modifications du projet officiel (qui peuvent également provenir d'autres étudiants)
git pull upstream master
#envoyer la fusion à votre fork github
git push origin master
```

Pour reporter des bugs, l'interface dédiée se trouve ici : https://github.com/matthieu637/cpp-2a-info/issues (différent du pull request)

#### Documentation
Pour les contributions à la documentation, il est inutile de modifier les fichiers du répertoire /docs.
Ils sont générés à l'aide du script 2017/docgen et du fichier 2017/client.py.
Il suffit donc de modifier les commentaires présents dans le fichier 2017/client.py, pour avoir un aperçu html de vos modifications vous pouvez appeler le script 2017/docgen (nécessite l'exécutable epydoc).

Pour la documentation du serveur, vous pouvez utiliser SHIFT + ALT + J sur le nom d'une méthode dans eclipse.
Contrairement à python, la documentation en java se place avant la fonction (exemple ici : https://github.com/matthieu637/SMA/blob/master/project/src/java/modele/percepts/Interpreteur.java)

#### Serveur
Pour les contributions au serveur, il est conseillé d'utiliser [l'IDE Eclipse](https://eclipse.org/downloads/) et d'[importer le projet SimBourse](http://stackoverflow.com/questions/6760115/importing-a-github-project-into-eclipse) pour lancer le serveur en local. Il faudra alors préciser au client de se connecter en localhost :
```
r = Reseau('localhost', 23456) #au lieu de de r=Reseau()
```
Puisque les nouvelles commandes que vous avez ajoutées ne seront pas directement disponibles sur le serveur officiel, cela vous permettra d'effectuer des tests en local. Après la validation de votre pull request, le serveur officiel sera relancé avec les nouveaux changements, vous permettant alors de réutiliser r=Reseau().

#### Contributions possibles (triées par difficulté, * : prioritaire ) [[Liste des Contributeurs](https://github.com/matthieu637/cpp-2a-info/graphs/contributors)]
- [ ] faire un graphique [de ce type](http://bigbrowser.blog.lemonde.fr/files/2014/07/TuOuVous-530x405.jpg) pour expliquer le classement des joueurs
- [ ] [améliorer/correction](http://epydoc.sourceforge.net/manual-epytext.html) de la documentation (client.py) participation de [MatthieuDEVALLE](https://github.com/matthieuDEVALLE)
- [ ] expliquer de façon simple le classement dans la [documentation](http://epydoc.sourceforge.net/manual-epytext.html) (client.py) : information présente dans la fonction core.Joueur::compareTo(Joueur) participation de [MatthieuDEVALLE](https://github.com/matthieuDEVALLE)
- [ ] définir un ensemble de [tests unitaires](https://openclassrooms.com/courses/apprenez-a-programmer-en-python/les-tests-unitaires-avec-unittest) pour empêcher l'introduction de bug lors des contributions (client.py)
- [x] [rendre l'argument action insensible à la casse dans la fonction historiques (client.py) : pouvoir utiliser r.historiques('facebook') sans déclencher d'erreur](https://github.com/matthieu637/cpp-2a-info/pull/4) terminée par [MatthieuDEVALLE](https://github.com/matthieuDEVALLE)
- [x] [rendre l'argument action insensible à la casse dans toutes les fonctions (client.py)](https://github.com/matthieu637/cpp-2a-info/pull/8) terminée par [MatthieuDEVALLE](https://github.com/matthieuDEVALLE)
- [x] [faire un cache pour la fonction fin avec un compteur local pour diminuer les requêtes serveurs (client.py)](https://github.com/matthieu637/cpp-2a-info/pull/1) terminée par [david540](https://github.com/david540)
- [x] [ajouter un code d'erreur pour un appel à fonction alors que la partie est finie (client)](https://github.com/matthieu637/cpp-2a-info/pull/5) terminée par [david540](https://github.com/david540)
- [x] [corriger le bug #9](https://github.com/matthieu637/cpp-2a-info/issues/9) (client.py) terminée par [david540](https://github.com/david540)
- [ ] documenter le code java pour tenter de le comprendre : SHIFT + ALT + J sur le nom d'une méthode dans eclipse (serveur)
- [ ] vérifier que le créateur de partie ne peut pas s'appeller 'banque' (serveur)
- [ ] vérifier que les caractères envoyés pour les noms des joueurs ne contient que des lettres et pas de caractères spéciaux (serveur)
- [ ] définir un ensemble de [tests unitaires](https://openclassrooms.com/courses/les-tests-unitaires-en-java) pour empêcher l'introduction de bug lors des contributions (serveur)
- [ ] optimiser l'ordre de traitement des requêtes sur le serveur en modifiant le positionnement dans les if/else if de network.Client::run() (serveur) : la création de partie, rejoindre, top et fin sont des appels rares qui devraient être en fin de séquence de test pour éviter de les tester à chaque fois
- [x] [diminuer la taille des messages réseaux en transformant les ordres réseaux en nombres (client+serveur) : "CREATE" devient "1", "JOIN" devient "2", ...](https://github.com/matthieu637/cpp-2a-info/pull/6) terminée par [david540](https://github.com/david540)
- [x] [diminuer la taille des messages réseaux en utilisant des nombres à la place de chaîne pour représenter les différents types d'actions (client+serveur)](https://github.com/matthieu637/cpp-2a-info/pull/6) terminée par [david540](https://github.com/david540)
- [ ] ajouter le type de l'ordre dans les tuples reçus par la fonction historique (serveur)
- [x] * * * [changer le retour de la fonction top() pour le créateur de la partie. Au lieu de renvoyer 0, cela retourne la liste des personnes qui ont rejoint la partie pour qu'il puisse vérifier qu'il n'y ait pas de fraudes. Par exemple un élève qui rejoindrait 2 fois la partie pour tricher. (client+serveur)](https://github.com/matthieu637/cpp-2a-info/pull/6) terminée par [david540](https://github.com/david540)
- [x] * * [ajouter un premier message envoyé par le serveur représentant la version du serveur. Si le client n'a pas la même version, alors une erreur est levée pour prévenir l'étudiant que son fichier client.py n'est plus à jour (client+serveur)](https://github.com/matthieu637/cpp-2a-info/pull/10) terminée par [david540](https://github.com/david540)
- [x] * [ajouter une fonction listeDesCoups() ne pouvant être appellé qu'une fois la partie terminée. Elle listera l'ensemble des coups (achat, ventes, annulation) que tous les joueurs ont envoyés durant une partie. Cela permettra d'apprendre de ses erreurs et potentiellement d'appliquer des techniques d'apprentissages statistiques sur ces données.](https://github.com/matthieu637/cpp-2a-info/pull/7) (client+serveur) terminée par [david540](https://github.com/david540)
- [x] * [faire écrire au serveur la liste des coups de l'ensemble des parties dans un fichier qui pourra être ensuite distribué aux étudiants (serveur)](https://github.com/matthieu637/cpp-2a-info/pull/12) terminée par [david540](https://github.com/david540)
- [ ] ne logger que les parties qui contiennent plus d'un joueur, ajouter le classement de fin de partie aux logs. Ajouter un champ "temps depuis debut de la partie" à la classe core.Operation et aux logs. Format : {'coups': ListeDesCoups, 'classement' : ListeClassement}. (serveur)
- [ ] faire des statistiques sur le nombre de requête reçu par minute sur les parties avec leur type. Vérifier qu'un utilisateur ne puisse pas saturer et monopoliser le serveur pour lui-même. Enregistrer le tout dans un fichier. (serveur)
  - [ ] définir un nombre limite de requête par seconde pour chaque utilisateur (serveur)
- [x] [ajouter un argument facultatif pour limiter la taille des listes reçues dans achats et ventes afin de diminuer la taille des messages réseaux. Exemple : r.ventes('Facebook', 5)](https://github.com/matthieu637/cpp-2a-info/pull/14) (client+serveur) terminée par [david540](https://github.com/david540)
- [x] [ajouter un cache pour la fonction historique pour réduire la taille de la liste à passer au réseau (client+serveur)](https://github.com/matthieu637/cpp-2a-info/pull/2) ([idée émise](https://github.com/matthieu637/cpp-2a-info/pull/1) et terminée par [david540](https://github.com/david540))
- [x] [correction du bug #13](https://github.com/matthieu637/cpp-2a-info/issues/13) trouvé et terminée par [david540](https://github.com/david540)
- [ ] * * * ajouter une fonction avantTop() pour le créateur de la partie, il peut lister les joueurs connectés et leur nombre avant d'appeller top() (client+serveur) 
- [ ] réduire la taille des messages réseaux en codant le nom des joueurs par des nombres. La première fois qu'un nom est envoyé sur le réseau, il est écrit en clair, les prochains fois, seul un nombre le représentera. (client+serveur)
- [ ] optimiser le protocol réseau d'envoie des données du serveur vers le client. Actuellement 8 caractères sont envoyés en début de requête pour prévenir de la taille de la requête (maximum 99999999). Utiliser des bytes pour représenter la taille de la requête à la place de chaînes peut faire gagner en temps. (client+serveur)
- [ ] rendre le traitement des requêtes réseaux optimal. Passage de O(n) à O(1) en remplacant la succession de comparaison de chaîne par un accès direct dans un tableau : idée similaire à l'optimisation qui a eu lieu pour les actions. Exemple : Create est dans la case 0 du tableau, JOIN est la case 1, ... Il faut créer autant de classe qu'il y a de requêtes différentes, utiliser le polymorphisme, ... (serveur)

Pour les contributions suivantes (les plus difficiles), il est inutile, pour le moment, d'implémenter uniquement ces fonctions dans le client (avec de multiples requêtes au serveur). L'intérêt étant qu'elles soient justement exécutées au plus vite du côté serveur avant les autres requêtes. Si elles ne sont pas terminées avant la compétition, chaque étudiant pourra alors décider de les implémenter du côté de son client (pas besoin de partager votre solution).
- [ ] ajouter des ordres "meilleurs prix" : écoule (achète/vend) x actions jusqu'à épuisement des x actions (client+serveur)
- [ ] ajouter des [Stop orders](https://en.wikipedia.org/wiki/Order_(exchange)#Stop_orders) (client+serveur) : vend/achète x actions dès que le prix chute/augmente à y.

