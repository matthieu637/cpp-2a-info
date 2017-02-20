# cpp-2a-info
CPP - Prépa des INP - Nancy | Projet Informatique 2ème année

Documentation officielle : https://matthieu637.github.io/cpp-2a-info/client.Reseau-class.html

[Documentation alternative (non vérifiée)](http://documentation-matthieu-devalle.readthedocs.io) par [MatthieuDEVALLE](https://github.com/matthieuDEVALLE)

### Contributions
Pour les plus motiver, nous vous encourageons à améliorer le projet (il vous faudra un compte sur github, utiliser git, [forker le projet et proposer des pull requests](https://help.github.com/articles/creating-a-pull-request-from-a-fork/)).

Pour les contributions à la documentation, il est inutile de modifier les fichiers du répertoire /docs.
Ils sont générés à l'aide du script 2017/docgen et du fichier 2017/client.py.
Il suffit donc de modifier les commentaires présents dans le fichier 2017/client.py, pour avoir un aperçu html de vos modifications vous pouvez appeler le script 2017/docgen (nécessite l'exécutable epydoc).


Pour les contributions au serveur, il est conseillé d'utiliser [l'IDE Eclipse](https://eclipse.org/downloads/) et d'importer le projet SimBourse pour lancer le serveur en local. Il faudra alors préciser au client de se connecter en localhost.
```
r = Reseau('localhost', 23456) #au lieu de de r=Reseau()
```


Exemple de contributions possibles (par difficulté) :
- [ ] [améliorer/correction](http://epydoc.sourceforge.net/manual-epytext.html) de la documentation (client.py)
- [x] [faire un cache pour la fonction fin avec un compteur local pour diminuer les requêtes serveurs (client.py)](https://github.com/matthieu637/cpp-2a-info/pull/1) [terminé par david540](https://github.com/david540)
- [x] [ajouter un code d'erreur pour un appel à fonction alors que la partie est finie (client+serveur)](https://github.com/matthieu637/cpp-2a-info/pull/1) [terminé par david540](https://github.com/david540)
- [ ] ajouter le type de l'ordre dans les tuples reçus par la fonction historique (serveur)
- [ ] ajouter un argument pour limiter la taille des listes reçues dans historiques, achats et ventes pour diminuer la taille des messages réseaux (client+serveur)
- [ ] ajouter un cache pour la fonction historique pour réduire la taille de la liste à passer au réseau

[Liste des Contributeurs](https://github.com/matthieu637/cpp-2a-info/graphs/contributors)
