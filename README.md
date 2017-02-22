# cpp-2a-info
CPP - Prépa des INP - Nancy | Projet Informatique 2ème année

Documentation officielle : https://matthieu637.github.io/cpp-2a-info/client.Reseau-class.html

[Documentation alternative (non vérifiée)](http://documentation-matthieu-devalle.readthedocs.io) par [MatthieuDEVALLE](https://github.com/matthieuDEVALLE)

### Contributions (la suite ne concernent que les personnes voulant aller plus loin avant la compétition)
Pour les plus motiver, nous vous encourageons à améliorer le projet (il vous faudra un compte sur github, utiliser [git](https://openclassrooms.com/courses/gerer-son-code-avec-git-et-github), [forker le projet et proposer des pull requests](https://help.github.com/articles/creating-a-pull-request-from-a-fork/)).

Attention à garder vos [branches à jour](https://help.github.com/articles/syncing-a-fork/#platform-linux)
avant de pull request au risque que nos branches master divergent (https://github.com/matthieu637/cpp-2a-info/network). Cela vous permettra également de résoudre les conflits avant tout pull request :
```
git remote add upstream https://github.com/matthieu637/cpp-2a-info.git
git pull upstream master
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

#### Contributions possibles (par difficulté)
- [ ] faire un graphique [de ce type](http://bigbrowser.blog.lemonde.fr/files/2014/07/TuOuVous-530x405.jpg) pour expliquer le classement des joueurs
- [ ] [améliorer/correction](http://epydoc.sourceforge.net/manual-epytext.html) de la documentation (client.py) participation de [MatthieuDEVALLE](https://github.com/matthieuDEVALLE)
- [ ] expliquer de façon simple le classement dans la [documentation](http://epydoc.sourceforge.net/manual-epytext.html) (client.py) : information présente dans la fonction core.Joueur::compareTo(Joueur) participation de [MatthieuDEVALLE](https://github.com/matthieuDEVALLE)
- [x] ~~[rendre l'argument action insensible à la casse dans la fonction historiques (client.py) : pouvoir utiliser r.historiques('facebook') sans déclencher d'erreur](#4)~~ terminée par [MatthieuDEVALLE](https://github.com/matthieuDEVALLE)
- [x] ~~[faire un cache pour la fonction fin avec un compteur local pour diminuer les requêtes serveurs (client.py)](https://github.com/matthieu637/cpp-2a-info/pull/1)~~ terminée par [david540](https://github.com/david540)
- [x] ~~[ajouter un code d'erreur pour un appel à fonction alors que la partie est finie (client)](https://github.com/matthieu637/cpp-2a-info/pull/5)~~ terminée par [david540](https://github.com/david540)
- [ ] documenter le code java pour tenter de le comprendre : SHIFT + ALT + J sur le nom d'une méthode dans eclipse (serveur)
- [ ] diminuer la taille des messages réseaux en transformant les ordres réseaux en nombres (client+serveur) : "CREATE" devient "1", "JOIN" devient "2", ...
- [ ] diminuer la taille des messages réseaux en utilisant des nombres à la place de chaîne pour représenter les différents types d'actions (client+serveur)
- [ ] ajouter le type de l'ordre dans les tuples reçus par la fonction historique (serveur)
- [ ] ajouter un argument facultatif pour limiter la taille des listes reçues dans achats et ventes afin de diminuer la taille des messages réseaux (client+serveur)
- [x] ~~[ajouter un cache pour la fonction historique pour réduire la taille de la liste à passer au réseau (client+serveur)](https://github.com/matthieu637/cpp-2a-info/pull/2)~~ ([idée émise](https://github.com/matthieu637/cpp-2a-info/pull/1) et terminée par [david540](https://github.com/david540))

Pour les contributions suivantes (les plus difficiles), il est inutile, pour le moment, d'implémenter uniquement ces fonctions dans le client (avec de multiples requêtes au serveur). L'intérêt étant qu'elles soient justement exécutées au plus vite du côté serveur avant les autres requêtes. Si elles ne sont pas terminées avant la compétition, chaque étudiant pourra alors décider de les implémenter du côté de son client (pas besoin de partager votre solution).
- [ ] ajouter des ordres "meilleurs prix" : écoule (achète/vend) x actions jusqu'à épuisement des x actions (client+serveur)
- [ ] ajouter des [Stop orders](https://en.wikipedia.org/wiki/Order_(exchange)#Stop_orders) (client+serveur) : vend/achète x actions dès que le prix chute/augmente à y.

[Liste des Contributeurs](https://github.com/matthieu637/cpp-2a-info/graphs/contributors)
