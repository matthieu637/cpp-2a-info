import socket
import time

class Reseau:
	'''
	Fait le lien entre votre programme et le serveur.
	
	Pour utiliser ces fonctions, il faut commencer par le bloc suivant (en s'assurant 
	que le fichier client.py se trouve dans le meme repertoire):
		>>> from client import Reseau
		r=Reseau()
		#on travaille ensuite sur la variable r
		
	Une fois la connexion effectue, il faut decider si vous serez le createur de la partie
	ou si vous allez la rejoindre. Celui qui cree la partie n'a pas besoin de la rejoindre,
	il est automatiquement dedans.
	
	Pour la creation :
		>>> r.creerPartie('monNom')
		48858
		
	Pour rejoindre (le nombre 48858 est donne a titre d'exemple, on utilisera celui du createur) :
		>>> r.rejoindrePartie(48858, 'monNom')
		
	Lorsque tous les utilisateurs ont rejoint la partie, il faut se synchroniser pour le top depart.
	Pour cela, ceux qui ont rejoint la partie utiliseront :
		>>> r.top() #ne rend pas la main tant que le createur n'a pas lance
	
	Le createur utilise a son tour :
		>>> r.top() #libere les autres utilisateurs
		
	qui declenche le debut de la partie pour 10 minutes et debloque les autres utilisateurs qui etaient
	en attente.
	
	A partir de la, createur et utilisateurs ont acces aux memes fonctions du jeu.
		>>> r.solde()
		{'Apple': 100, 'Facebook': 100, 'Google': 100, 'Trydea': 100, 'euros': 1000}

	A vous de jouer.
	'''
	
	def __init__(self, host="matthieu-zimmer.net", port=23456):
		self.connect = False
		self.topbool = False
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
		Renvoie un numero de partie unique a communiquer oralement avec vos concurrents
		
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
			- -2 si le nom de joueur est deja pris
			- -3 si la partie est deja lance (top)
			- -4 si les types ne sont pas respecte
		
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
		return r

	def solde(self):
		'''
		Retourne un dictionnaire (string:entier) avec vos actions et vos euros.
		'''
		self.__estTop()
		self.__notEnd()
		self.__envoyer("SOLDE")
		return eval(self.__recevoir())
	
	def operationsEnCours(self):
		'''
		Retourne une liste d'entier qui correspondent aux identifiants des ordres
		que vous avez soumis predecemment et qui sont toujours ouverts.
		Cela permet de pouvoir les suivre ou les annuler.
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
		Liste tous les ordres d'achats ouverts de tous les utilisateurs pour une action donnee.
		Renvoie une liste de tuple (nom_acheteur, prix, volume) triee par le prix le plus avantageux.
		Si l'action n'existe pas renvoie -4;
		
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
		
		@param action: le nom de l'action pour laquelle vous voulez voir les offres de ventes
		@type action: string
		'''
		self.__estTop()
		self.__notEnd()
		self.__envoyer("VENTES "+action)
		return eval(self.__recevoir())

	def historiques(self, action):
		'''
		Liste tous les echanges qui ont deja ete effectues sur une action.
		Renvoie une liste de tuple (nom_vendeur, nom_acheteur, prix, volume) triee par ordre chronologique.
		
		@param action: le nom de l'action
		@type action: string
		'''
		self.__estTop()
		self.__notEnd()
		self.__envoyer("HISTO "+action)
		return eval(self.__recevoir())

	def suivreOperation(self, id_ordre):
		'''
		Permet de voir le volume restant dans un ordre precedemment soumis.
		Renvoie :
			- 0 si l'ordre n'existe plus ou est termine
			- -4 si les types ne sont pas respectes
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
		Permer d'annuler un ordre precedemment soumis pour recuperer les fonds provisionnes.
		Renvoie :
			- -11 si l'ordre n'existe plus ou est termine
			- -4 si les types ne sont pas respectes
			- le volume restant si ordre de vente
			- les euros recuperes si ordre d'achat
			
		@param id_ordre: idenfiant unique de l'ordre a suivre
		@type id_ordre: entier
		'''
		self.__estTop()
		self.__notEnd()
		self.__envoyer("ANNULER "+str(id_ordre))
		return eval(self.__recevoir())

	def fin(self):
		'''
		Renvoie un dictionnaire contenant le temps restant (en seconde) pour la partie en cours (string:entier).
		
		Lorsque la partie est terminee, un classement des joueurs est ajoute dans le dictionnaire (string:liste).
		'''

			
		self.__estTop()
		tempsRestant=self.tempsFinPartie - time.time() #fin de partie - maintenant
		if(tempsRestant>0): #Dans ce cas, pas besoin de faire une requête au serveur, on affiche simplement le temps restant
			return {'temps': int(tempsRestant)+1}
		#si la partie est finie on fait une requete au serveur pour qu'il donne la liste des vainqueurs
		self.__envoyer("FIN")
		return eval(self.__recevoir())

