#!/usr/bin/env python
# -*- coding: utf-8 -*-

import socket
import time

class Reseau:
	'''
	Fait le lien entre votre programme et le serveur.
	
	Cette documentation a pour but de vous aider à utiliser la classe Reseau (qui fait le lien entre votre programme et le serveur). En POO (programmation orientée objet), une classe est un ensemble de fonctions prédéfinies. Dans notre cas, cette classe permet de communiquer avec le serveur “boursier” écrit en Java afin d’acheter, de vendre ...
	Pour plus d’informations:
	
	U{https://openclassrooms.com/courses/apprenez-a-programmer-en-python/premiere-approche-des-classes}

	Préliminaires
	=============
	
		Avant de pouvoir rejoindre ou créer une partie, il faut tout d’abord importer la class (après l’avoir 
		U{téléchargée<https://raw.githubusercontent.com/matthieu637/cpp-2a-info/master/2017/client.py>} : clic droit, enregistré le lien sous). 
		Pour cela, il faut metre ce fichier dans le même dossier que le fichier courant puis dans le fichier courant, on commence par:
			>>> from client import Reseau
			r=Reseau() #On travaillera par la suite avec la variable r
		
		B{Attention: Le fichier client.py ne doit JAMAIS être modifié !}
	
	Création d’une partie
	=====================
	
		Une fois que la connexion est effectuée, on doit pouvoir choisir entre créer une partie ou en rejoindre une (à faire vous même...).
		Pour créer une partie:
			>>> num_partie=r.creerPartie("monNom") #On crée la partie
			print(num_partie) #On récupère son numéro
		
		On récupère le numéro afin de le transmettre oralement aux autres joueurs qui eux devront rejoindre la partie.
	
		B{Attention: Celui qui crée la partie n’a pas besoin de la rejoindre, il est directement considéré comme un joueur}
		
	Rejoindre la partie
	===================
		
		Une fois la partie créée par l’un des participants, les autres doivent pouvoir la rejoindre:
		Premièrement, on rejoint la partie, en indiquant son numéro et le pseudo que l’on souhaite, par exemple,
	
		>>> r.rejoindrePartie(4488,'MatthieuDevallé')
	
		Puis on doit se synchroniser avec les autres joueurs:
	
		>>> r.top()
	
		On a donc le code suivant pour rejoindre une partie:
			>>> r.rejoindrePartie(numéro_partie,"pseudo")
			r.top()
			
		B{Attention: On ne reprend pas la main tant que le créateur n’a pas lui même tapé C{r.top}()}
	
	Synchronisation pour le départ
	==============================

		Lorsque tous les utilisateurs ont rejoint la partie, il faut se synchroniser pour le top depart.
		Pour cela, ceux qui ont rejoint la partie utiliseront :
			>>> r.top() #ne rend pas la main tant que le createur n'a pas lance
		
		Une fois que tous les joueurs ont rejoint la partie, le créateur entre à son tour qui lance la partie:
		
		A partir de la, createur et utilisateurs ont acces aux memes fonctions du jeu.
			>>> r.solde()
			{'Apple': 100, 'Facebook': 100, 'Google': 100, 'Trydea': 100, 'euros': 1000}
	
		A partir de ce moment, il n’y a plus de différence entre le créateur et les autres.
		
	Informations sur le classement final
	====================================
		
		Il y a plusieurs cas (on note j1 et j2 les joueurs):
			- dans le cas où j1 (ou j2) a vendu les deux types d'actions, il gagne
			- dans le cas où j1 (ou j2) a vendu au moins un type d'action et pas l'autre, il gagne
			- dans le cas où les deux n'ont que de l'argent, le plus riche gagne
			- dans le cas où les deux ont plusieurs actions, on compare leur nombre d'action et leur argent, le plus riche en action gagne sauf si ils ont le même nombre d'actions (le plus riche gagne)
			- dans le meilleur cas, s'ils ont vendu deux types d'actions, on compare leur nombre d'action ainsi que leur argent, le plus riche gagne
			
	'''
	
	def __init__(self, host="matthieu-zimmer.net", port=23456):
		self.connect = False
		self.topbool = False
		self.histoActions={}
		self.tempsFinPartie= 0
		self.sock=socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		self.sock.settimeout(5)
		result = self.sock.connect_ex((host, port))
		if result != 0 and host == "matthieu-zimmer.net":
			result = self.sock.connect_ex(("paris.matthieu-zimmer.net", 80))
		if result != 0:
			raise RuntimeError("Impossible de se connecter a l'host fourni.")
		self.sock.settimeout(None)

	def __estConnect(self):
		if(self.connect):
			raise RuntimeError("Vous etes deja connecte.")

	def __estTop(self):
		if(not self.topbool):
			raise RuntimeError("La partie n'est pas encore commencee.")
	
	def __notEnd(self):#verifie si la partie n'est pas finie
		if(self.fin()['temps']<=0): 
			raise RuntimeError("La partie est finie.")
	
	def __envoyer(self, commande):
		try:
			sent = self.sock.send((commande+"\n").encode())
			if sent == 0:
				raise RuntimeError("Connexion perdu. _1", commande)	
		except (ConnectionRefusedError):
			raise RuntimeError("Connexion perdu. _2")

	def __recevoir(self):
		try:
			length = self.sock.recv(8)
			if length == b'':
				raise RuntimeError("Connexion perdu. _5")
			back = self.sock.recv(int(length.decode()))
			if back == b'':
				raise RuntimeError("Connexion perdu. _3")
			return back.decode()
		except (ConnectionRefusedError):
			raise RuntimeError("Connexion perdu. _4")
			

	def creerPartie(self, nom):
		'''
		Crée la partie et renvoie l’id à communiqur oralement aux autres joueurs.
		Exemple:

		>>> id=r.creerPartie("MatthieuDevallé")
		>>> print(id)
		31416 #id de la partie
		
		@param nom: le nom du joueur qui cree la partie
		@type nom: string
		
		
	
		'''
		self.__estConnect()
		self.__envoyer("CREATE "+nom)
		id_partie = int(self.__recevoir())
		self.connect = True
		return id_partie
	
	def rejoindrePartie(self, id_partie, nom):
		'''
		Renvoie : 
			- 0 si tout c'est bien passe.
			- -1 si le numero de partie n'existe pas
			- -2 si le nom de joueur est déja pris
			- -3 si la partie est déja lancée (top)
			- -4 si les types ne sont pas respectés
		
		@param id_partie: le numero de la partie qui m'a ete communiquer oralement
		@type id_partie: entier
		@param nom: le nom du joueur qui rejoint la partie
		@type nom: string
		'''
		self.__estConnect()
		self.__envoyer("JOIN "+str(id_partie)+" "+nom)
		ok = self.__recevoir()
		self.connect = True
		return int(ok)

	def top(self):
		'''
		Si vous avez rejoint, attend le top depart du createur.
		
		Si vous avez cree, donne le top depart aux autres.
		
		Renvoie toujours 0.
		'''
		if(not self.connect):
			raise RuntimeError("Vous n'etes pas encore connecte.")
		self.__envoyer("TOP")
		r = int(self.__recevoir())
		self.topbool= True
		
		self.__envoyer("FIN") #Pour avoir la duree de la partie
		self.tempsFinPartie=time.time() + int(eval(self.__recevoir())['temps']) #lance le 'chronometre' quand le serveur a lance le top

		for key in self.solde():
			if key!='euros':
				self.histoActions[key]=[] #on ajoute les différentes actions dans le tableau historique du client
		

		return r

	def solde(self):
		'''
		Permet de voir notre porte-feuille d’actions et notre argent disponible
		Renvoie un dictionnaire (string:entier).
		
		Exemple:

		>>> r.solde()
		{'Apple': 100, 'Facebook': 100, 'Google': 100, 'Trydea': 100, 'euros': 1000}
		'''
		self.__estTop()
		self.__notEnd()
		self.__envoyer("SOLDE")
		return eval(self.__recevoir())
	
	def operationsEnCours(self):
		'''
		Retourne une liste d’entiers, qui correspondent aux identifiants des ordres précédemment transmis et qui ne sont pas encore terminés: on peut donc les suivre et les annuler.

		Exemple:

		>>> R.operationsEnCours()
		[62098581, 20555477]

		'''
		self.__estTop()
		self.__notEnd()
		self.__envoyer("OPERATIONS")
		return eval(self.__recevoir())

	def ask(self, action, prix, volume):
		'''
		Passer un ordre d'achat.
		Renvoie :
			- 0 si l'ordre a ete execute directement et que tout son volume a ete ecoule
			- -4 si les types ne sont pas respectes
			- -5 si volume <= 0
			- -6 si prix <= 0
			- -7 si vous n'avez pas assez d'argent pour acheter cette quantite (prix*volume)
			- sinon renvoie l'identifiant de l'ordre (nombre positif)
		
		@param action: le nom de l'action 
		@type action: string
		@param prix: prix unitaire d'achat
		@type prix: flottant
		@param volume: le nombre d'action que vous voulez acheter
		@type volume: entier
		'''
		self.__estTop()
		self.__notEnd()
		self.__envoyer("ASK "+action+" "+str(prix)+" "+str(volume))
		return eval(self.__recevoir())

	def bid(self, action, prix, volume):
		'''
		Passer un ordre de vente.
		Renvoie :
			- 0 si l'ordre a ete execute directement et que tout son volume a ete ecoule
			- -4 si les types ne sont pas respectes
			- -8 si volume <= 0
			- -9 si prix <= 0
			- -10 si vous n'avez pas assez d'action de type action dans votre portefeuille
			- sinon renvoie l'identifiant de l'ordre (nombre positif)
		
		@param action: le nom de l'action 
		@type action: string
		@param prix: prix unitaire de vente
		@type prix: flottant
		@param volume: le nombre d'action que vous voulez vendre
		@type volume: entier
		'''
		self.__estTop()
		self.__notEnd()
		self.__envoyer("BID "+action+" "+str(prix)+" "+str(volume))
		return eval(self.__recevoir())

	def achats(self, action):
		'''
		Liste tous les ordres d’achats pour tous les joueurs sur une action donnée.
		Retourne:
			- -4 si l’action n’existe pas
			- une liste de tuples triée par ordre de prix avantageux sous la forme: C{(nom_acheteur, prix, volume)}
		
		Exemple:

		>>> r.achats("Trydea")
		[('Matthieu', 23,15), ('Ryan',20,10), ('Paul', 17,23)]
		
		@param action: le nom de l'action pour laquelle vous voulez voir les offres d'achats
		@type action: string
		'''
		self.__estTop()
		self.__notEnd()
		self.__envoyer("ACHATS "+action)
		return eval(self.__recevoir())
	
	def ventes(self, action):
		'''
		Liste tous les ordres de ventes ouverts de tous les utilisateurs pour une action donnee.
		Renvoie une liste de tuple (nom_acheteur, prix, volume) triee par le prix le plus avantageux.
		Si l'action n'existe pas renvoie -4;
		
		Retourne:
			- -4 si l’action n’existe pas
			- une liste de tuples triée par ordre de prix avantageux sous la forme: C{(nom_acheteur, prix, volume)}
	
		Exemple:

		>>> r.ventes('Facebook')
		[('Matthieu', 5.0, 5), ('banque', 25.0, 40000)]

		

		@param action: nom de l'action
		@type action: string
		'''
		self.__estTop()
		self.__notEnd()
		self.__envoyer("VENTES "+action)
		return eval(self.__recevoir())

	def historiques(self, action):
		'''
		Permet de lister tous les échanges déjà effectués sur une action.
		Retourne une liste de tuples triée par ordre chronologique. Sous la forme: C{(nom_vendeur, nom_acheteur, prix, volume)}
		
		Exemple:

		>>> r.historiques("Trydea")
		[('Matthieu','Mukhlis',10,10), ('Térence', 'Ryan', 15,20), ('Matthieu', 'Ryan', 20,3)]
		
		@param action: le nom de l'action
		@type action: string
		'''
		action=action.lower()
		action=action.replace(action[0],action[0].upper(),1) #On change (en majuscule) le premier caractère de la chaine
		self.__estTop()
		self.__notEnd()
		self.__envoyer("HISTO "+action+" "+str(len(self.histoActions[action])))
		self.histoActions[action]+=(eval(self.__recevoir()))
		return self.histoActions[action]
		#return eval(self.__recevoir())

	def suivreOperation(self, id_ordre):
		'''
		Permet de voir le volume restant pour un ordre transmis précédemment.
		
		Retourne:
			- 0 si l’ordre n’existe plus ou est terminé
			- 4 si les types ne sont pas respectés
			- sinon le volume restant en achat/vente.
		
		@param id_ordre: idenfiant unique de l'ordre a suivre
		@type id_ordre: entier
		'''
		self.__estTop()
		self.__notEnd()
		self.__envoyer("SUIVRE "+str(id_ordre))
		return eval(self.__recevoir())

	def annulerOperation(self, id_ordre):
		'''
		Annule un ordre transmis précédemment afin de récupérer les fonds provisionnés.
		
		Retourne:
			- 11 si l’ordre n’existe plus ou est termine
			- 4 si les types ne sont pas respectés
			- le volume d’action restant si c’est un ordre de vente
			- les euros dépensés si c’est ordre d’achat
		
		Exemple:
		
		>>> r.annulerOrdre(31416)
		
		@param id_ordre: : id de l’odre (récupérer à partir de la fonction operationsEnCours())
		@type id_ordre: entier
		'''
		self.__estTop()
		self.__notEnd()
		self.__envoyer("ANNULER "+str(id_ordre))
		return eval(self.__recevoir())

	def fin(self):
		'''
		Renvoie un dictionnaire le temps restant (en s) avant la fin de la partie (string:entier). Si la partie est terminée, affiche le classement (string:liste).

		Exemple:

		>>> r.fin()
		{'temps': 10} #Il reste 10 secondes avant la fin de la partie.
		
		OU

		>>> r.fin()
		{'classement': ['Matthieu', 'Eshamuddin','banque'], 'temps': 0} #Le classement de fin de partie.
		'''

			
		self.__estTop()
		tempsRestant=self.tempsFinPartie - time.time() #fin de partie - maintenant
		if(tempsRestant>0): #Dans ce cas, pas besoin de faire une requête au serveur, on affiche simplement le temps restant
			return {'temps': int(tempsRestant)+1}
		#si la partie est finie on fait une requete au serveur pour qu'il donne la liste des vainqueurs
		self.__envoyer("FIN")
		return eval(self.__recevoir())

