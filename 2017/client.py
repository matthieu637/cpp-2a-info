import socket

class Reseau:
	def __init__(self, host="matthieu-zimmer.net", port=23456):
		self.connect = False
		self.topbool = False
		self.sock=socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		self.sock.settimeout(5)
		result = self.sock.connect_ex((host, port))
		if result != 0 and host == "matthieu-zimmer.net":
			result = self.sock.connect_ex(("paris.matthieu-zimmer.net", 80))
		if result != 0:
			raise RuntimeError("Impossible de se connecter a l'host fourni.")
		self.sock.settimeout(30)

	def __estConnect(self):
		if(self.connect):
			raise RuntimeError("Vous etes deja connecte.")

	def __estTop(self):
		if(not self.topbool):
			raise RuntimeError("La partie n'est pas encore commencee.")
	
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
		self.__estConnect()
		self.__envoyer("CREATE "+nom)
		id_partie = int(self.__recevoir())
		self.connect = True
		return id_partie

	def top(self):
		if(not self.connect):
			raise RuntimeError("Vous n'etes pas encore connecte.")
		self.__envoyer("TOP")
		r = int(self.__recevoir())
		self.topbool= True
		return r
	
	def rejoindrePartie(self, id_partie, nom):
		self.__estConnect()
		self.__envoyer("JOIN "+str(id_partie)+" "+nom)
		ok = self.__recevoir()
		if ok == "-1":
			raise RuntimeError("La partie", str(id_partie) ,"n'existe pas.")
		if ok == "-2":
			raise RuntimeError("Le nom", nom ,"est deja pris.")
		if ok == "-3":
			raise RuntimeError("La partie", str(id_partie) ,"est deja lance.")
		self.connect = True
		return True

	def solde(self):
		self.__estTop()
		self.__envoyer("SOLDE")
		return eval(self.__recevoir())
	
	def operationsEnCours(self):
		self.__estTop()
		self.__envoyer("OPERATIONS")
		return eval(self.__recevoir())

	def ask(self, action, prix, volume):
		self.__estTop()
		self.__envoyer("ASK "+action+" "+str(prix)+" "+str(volume))
		return eval(self.__recevoir())

	def bid(self, action, prix, volume):
		self.__estTop()
		self.__envoyer("BID "+action+" "+str(prix)+" "+str(volume))
		return eval(self.__recevoir())

	def achats(self, action):
		self.__estTop()
		self.__envoyer("ACHATS "+action)
		return eval(self.__recevoir())
	
	def ventes(self, action):
		self.__estTop()
		self.__envoyer("VENTES "+action)
		return eval(self.__recevoir())

	def historiques(self, action):
		self.__estTop()
		self.__envoyer("HISTO "+action)
		return eval(self.__recevoir())

	def suivreOperation(self, id_partie):
		self.__estTop()
		self.__envoyer("SUIVRE "+str(id_partie))
		return eval(self.__recevoir())

	def annulerOperation(self, id_ordre):
		self.__estTop()
		self.__envoyer("ANNULER "+str(id_ordre))
		return eval(self.__recevoir())

	def fin(self):
		self.__estTop()
		self.__envoyer("FIN")
		return eval(self.__recevoir())

