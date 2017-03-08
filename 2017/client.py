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
		
			Dans un premier groupe seront d’abord classés les gens qui terminent la partie avec une somme d’argent supérieure ou égale à 90% de la somme d’argent initiale.

			Dans un second groupe seront ensuite classés les gens qui terminent la partie avec une somme d’argent inférieur à 90% de la somme d’argent initiale.

			Dans chacun des groupes, les étudiants seront classés simplement par nombre d’actions possédées à l’issue de la partie, en ne prenant en compte que les 2 types d’actions possédées en plus grand nombre. (par exemple quelqu’un à qui il reste 712 actions Google, 14 actions Facebook, 1500 actions Google et 1500 actions Trydea aura un score de 1500+1500 =3000 actions. Les 712 actions Google et 14 actions Facebook ne compterons pas dans le score final, elles seront d’une certaine manière perdue). Dans le cas d’une égalité de ce score, les étudiants seront départagés avec l’argent restant. Dans le cas d’une nouvelle égalité, les étudiants seront classés égalité et auront la même note.

	'''	
	def __init__(self, host="193.54.21.49", port=23456):
		#membre publique
		self.nomAction=[]
		#membres privés
		#On définit un dictionnaire qui permettra la communication client/serveur avec des messages très courts		
		self.__message={"TOP":"1","SOLDE":"2","OPERATIONS":"3","ACHATS":"4 ","VENTES":"5 ","HISTO":"6 ","ASK":"7 ","BID":"8 ",
		"SUIVRE":"9 ","ANNULER":"A ","FIN":"B","CREATE":"C ","JOIN":"D ","LISTECOUPS":"E","AVANTTOP":"F"}
		self.__connect = False
		self.__topbool = False
		self.__histoActions={}
		self.__tempsFinPartie= 0
		self.__versionClient="1.9"
		self.__sock=socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		#connexion
		self.__sock.settimeout(5)
		result = self.__sock.connect_ex((host, port))
		if result != 0 and host == "193.54.21.49":
			result = self.__sock.connect_ex(("matthieu-zimmer.net", 80))
		if result != 0:
			raise RuntimeError("Impossible de se connecter a l'host fourni.")
		self.__sock.settimeout(300) #windows bug?
		self.__sock.settimeout(None)
		self.__sock.setsockopt(socket.IPPROTO_TCP, socket.TCP_NODELAY, 1)
		if self.__recevoir()!=self.__versionClient:
			raise RuntimeError("Votre client n'est plus à jour.\n Télécharger le nouveau client.py https://raw.githubusercontent.com/matthieu637/cpp-2a-info/master/2017/client.py")
	def __del__(self):
		self.__sock.shutdown(socket.SHUT_RDWR)
		self.__sock.close()

	def __estConnect(self):
		if(self.__connect):
			raise RuntimeError("Vous etes deja connecte.")

	def __estTop(self):
		if(not self.__topbool):
			raise RuntimeError("La partie n'est pas encore commencee.")
	
	def __notEnd(self):#verifie si la partie n'est pas finie
		if(self.fin()['temps']<=0): 
			raise RuntimeError("La partie est finie.")
	
	def __envoyer(self, commande):
		try:
			sent = self.__sock.send((commande+"\n").encode())
			if sent == 0:
				raise RuntimeError("Connexion perdu. _1", commande)	
		except (ConnectionRefusedError):
			raise RuntimeError("Connexion perdu. _2")

	def __recevoir(self):
		try:
			length = self.__sock.recv(8)
			if length == b'':
				raise RuntimeError("Connexion perdu. _5")
			length=int(length.decode())
			result=''
			while len(result) < length:
				back = self.__sock.recv(length-len(result))
				if back == b'':
			    		raise RuntimeError("Connexion perdu. _3")
				result += back.decode()
	
			return result
		except (ConnectionRefusedError):
			raise RuntimeError("Connexion perdu. _4")
			
	def __chercherNumAction(self,action):
		numAction=-1
		#recherche du numero de l'action (triee dans l'ordre alphabetique)
		for i in range(len(self.nomAction)):
			if action==self.nomAction[i]:
				numAction =i 
				break
		return numAction
		
	def creerPartie(self, nom):
		'''
		Crée la partie et renvoie l’id à communiqur oralement aux autres joueurs.
		
		Exemple:

		>>> id=r.creerPartie("MatthieuDevallé")
		print(id)
		31416 #id de la partie
		En cas d'erreur(Exemple pseudo avec des espaces):
		Renvoie -4
		
		@param nom: le nom du joueur qui cree la partie
		@type nom: string
		'''
		self.__estConnect()
		self.__envoyer(self.__message["CREATE"]+nom)
		id_partie = int(self.__recevoir())
		if id_partie>=0:
			self.__connect = True
		return id_partie
	
	def rejoindrePartie(self, id_partie, nom):
		'''
		Renvoie : 
			- 0 si tout s'est bien passé.
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
		self.__envoyer(self.__message["JOIN"]+str(id_partie)+" "+nom)
		ok = int(self.__recevoir())
		if ok==0:
			self.__connect = True
		return ok

	def avantTop(self):
		'''
		Pour le créateur: Renvoie la liste des noms des joueurs présents dans la partie avant le top
		'''
		if(not self.__connect):
			raise RuntimeError("Vous n'êtes pas encore connecté.")
		if(self.__topbool):
			raise RuntimeError("La partie est déjà lancée.")
		self.__envoyer(self.__message["AVANTTOP"])
		return eval(self.__recevoir())
	
	def top(self):
		'''
		Si vous avez rejoint, attend le top depart du createur.
		
		Si vous avez cree, donne le top depart aux autres.
		
		Renvoie 0 pour ceux qui rejoignent et la liste des noms des joueurs pour le créateur
		'''
		if(not self.__connect):
			raise RuntimeError("Vous n'etes pas encore connecte.")
		self.__envoyer(self.__message["TOP"]) 
		r = (self.__recevoir())
		self.__topbool= True
		self.__envoyer(self.__message["FIN"]) # Pour avoir la duree de la partie
		self.__tempsFinPartie=time.time() + int(eval(self.__recevoir())['temps']) #lance le 'chronometre' quand le serveur a lance le top

		for key in self.solde():
			if key!='euros':
				key=key.lower()
				self.nomAction.append(key)
				self.__histoActions[key]=[] #on ajoute les différentes actions dans le tableau historique du client
		self.nomAction.sort()

		return r

	def solde(self):
		'''
		Permet de voir notre porte-feuille d’actions et notre argent disponible
		Renvoie un dictionnaire (string:entier).
		
		Exemple:

		>>> r.solde()
		{'Apple': 1000, 'Facebook': 1000, 'Google': 1000, 'Trydea': 1000, 'euros': 1000}
		'''
		self.__estTop()
		self.__envoyer(self.__message["SOLDE"])
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
		self.__envoyer(self.__message["OPERATIONS"])
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
		action=action.lower()
		self.__estTop()
		self.__notEnd()
		#recherche du numero de l'action (triee dans l'ordre alphabetique)
		numAction=self.__chercherNumAction(action)
		if numAction==-1: #si le nom de l'action n'est pas valide on retourne -4
			return -4
		#on envoie le numero de l'action
		self.__envoyer(self.__message["ASK"]+str(numAction)+" "+str(prix)+" "+str(volume))
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
		action=action.lower()
		self.__estTop()
		self.__notEnd()
		#recherche du numero de l'action (triee dans l'ordre alphabetique)
		numAction=self.__chercherNumAction(action)
		if numAction==-1: #si le nom de l'action n'est pas valide on retourne -4
			return -4
		#on envoie le numero de l'action
		self.__envoyer(self.__message["BID"]+str(numAction)+" "+str(prix)+" "+str(volume))
		return eval(self.__recevoir())

	def achats(self, action, nbMaxElemListe=0):
		'''
		Liste tous les ordres d’achats avec nbMaxElemListe element de liste pour tous les joueurs sur une action donnée.
		Pour pas de limite d'éléments de liste, mettre nbMaxElemListe=0
		Retourne:
			- -4 si l’action n’existe pas
			- une liste de tuples triée par ordre de prix avantageux sous la forme: C{(nom_acheteur, prix, volume)}
		
		Exemple:

		>>> r.achats("Trydea")
		[('Matthieu', 23,15), ('Ryan',20,10), ('Paul', 17,23)]
		r.achats("Trydea", 1)
		[('Matthieu', 23,15)]
		
		@param action: le nom de l'action pour laquelle vous voulez voir les offres d'achats
		@type action: string
		@param nbMaxElemListe : argument facultatif
		@type nbMaxElemListe: entier
		'''
		action=action.lower()
		self.__estTop()
		self.__notEnd()
		#recherche du numero de l'action (triee dans l'ordre alphabetique)
		numAction=self.__chercherNumAction(action)
		if numAction==-1 or nbMaxElemListe <0: #si le nom de l'action n'est pas valide on retourne -4
			return -4
		#on envoie le numero de l'action
		if nbMaxElemListe==0:
			self.__envoyer(self.__message["ACHATS"]+str(numAction))
		else:
			self.__envoyer(self.__message["ACHATS"]+str(numAction)+" "+str(nbMaxElemListe))
		return eval(self.__recevoir())
	
	def ventes(self, action, nbMaxElemListe=0):
		'''
		Liste tous les ordres de ventes ouverts de tous les utilisateurs pour une action donnee.
		Pour pas de limite d'éléments de liste, mettre nbMaxElemListe=0
		Renvoie une liste de tuple (nom_acheteur, prix, volume) triee par le prix le plus avantageux.
		Si l'action n'existe pas renvoie -4;
		
		Retourne:
			- -4 si l’action n’existe pas
			- une liste de tuples triée par ordre de prix avantageux sous la forme: C{(nom_acheteur, prix, volume)}
	
		Exemple:

		>>> r.ventes('Facebook')
		[('Matthieu', 5.0, 5), ('banque', 25.0, 40000)]
		r.ventes('Facebook', 1)
		[('Matthieu', 5.0, 5)]
		
		@param action: nom de l'action
		@type action: string
		@param nbMaxElemListe : argument facultatif
		@type nbMaxElemListe: entier
		'''
		action=action.lower()
		self.__estTop()
		self.__notEnd()
		#recherche du numero de l'action (triee dans l'ordre alphabetique)
		numAction=self.__chercherNumAction(action)
		if numAction==-1 or nbMaxElemListe <0: #si le nom de l'action n'est pas valide on retourne -4
			return -4
		#on envoie le numero de l'action
		if nbMaxElemListe==0:
			self.__envoyer(self.__message["VENTES"]+str(numAction))
		else:
			self.__envoyer(self.__message["VENTES"]+str(numAction)+" "+str(nbMaxElemListe))
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
		self.__estTop()
		#recherche du numero de l'action (triee dans l'ordre alphabetique)
		numAction=self.__chercherNumAction(action)
		if numAction==-1: #si le nom de l'action n'est pas valide on retourne -4
			return -4
		#on envoie le numero de l'action
		self.__envoyer(self.__message["HISTO"]+str(numAction)+" "+str(len(self.__histoActions[action])))
		self.__histoActions[action]+=(eval(self.__recevoir()))
		return self.__histoActions[action]
		#return eval(self.__recevoir())

	def suivreOperation(self, id_ordre):
		'''
		Permet de voir le volume restant pour un ordre transmis précédemment.
		
		Retourne:
			- 0 si l’ordre n’existe plus ou est terminé
			- -4 si les types ne sont pas respectés
			- sinon le volume restant en achat/vente.
		
		@param id_ordre: idenfiant unique de l'ordre a suivre
		@type id_ordre: entier
		'''
		self.__estTop()
		self.__notEnd()
		self.__envoyer(self.__message["SUIVRE"]+str(id_ordre))
		return eval(self.__recevoir())

	def annulerOperation(self, id_ordre):
		'''
		Annule un ordre transmis précédemment afin de récupérer les fonds provisionnés.
		
		Retourne:
			- -11 si l’ordre n’existe plus ou est termine
			- -4 si les types ne sont pas respectés
			- le volume d’action restant si c’est un ordre de vente
			- les euros dépensés si c’est ordre d’achat
		
		Exemple:
		
		>>> r.annulerOrdre(31416)
		
		@param id_ordre: : id de l’odre (récupérer à partir de la fonction operationsEnCours())
		@type id_ordre: entier
		'''
		self.__estTop()
		self.__notEnd()
		self.__envoyer(self.__message["ANNULER"]+str(id_ordre))
		return eval(self.__recevoir())

	def listeDesCoups(self):
		'''
		Retourne une liste qui contient les operations effectuées par tous les joueurs de la partie sous la forme :
		['Achat', 'David', 'Facebook', 8.0, 10] pour un achat de 10 Facebook à 8.0 euros
		['Vente', 'David', 'Facebook', 10.0, 4] pour une vente de 4 Facebook à 10.0 euros
		['AnnulationAchat', 'David', 'Trydea', 4.0, 10] pour une annulation d'achat de 10 Trydea à 4.0 euros
		['AnnulationVente', 'David', 'Trydea', 4.0, 10] pour une annulation de vente de 10 Trydea à 4.0 euros
		'''
		self.__estTop()
		if(self.fin()['temps']>0):
			print("La partie n'est pas finie")
			return 
		self.__envoyer(self.__message["LISTECOUPS"])
		return eval(self.__recevoir())
	
	def fin(self):
		'''
		Renvoie un dictionnaire le temps restant (en s) avant la fin de la partie (string:entier). Si la partie est terminée, affiche le classement (string:liste).
		
		Exemple:

		>>> r.fin()
		{'temps': 10} #Il reste 10 secondes avant la fin de la partie.
		
		ou

		>>> r.fin()
		{'classement': ['Matthieu', 'Eshamuddin','banque'], 'temps': 0} #Le classement de fin de partie.
		'''
		self.__estTop()
		tempsRestant=self.__tempsFinPartie - time.time() #fin de partie - maintenant
		if(tempsRestant>0): #Dans ce cas, pas besoin de faire une requête au serveur, on affiche simplement le temps restant
			return {'temps': int(tempsRestant)+1}
		#si la partie est finie on fait une requete au serveur pour qu'il donne la liste des vainqueurs
		self.__envoyer(self.__message["FIN"])
		return eval(self.__recevoir())
