import socket

class Reseau:
	def __init__(self, h="192.168.0.45", p=3080):
		self.host=h
		self.port=p
		self.sock=socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		self.sock.connect_ex((self.host, self.port))
		self.connect = False
		self.topbool= False

	def __estConnect(self):
		if(self.connect):
			raise RuntimeError("Vous etes deja connecte.")

	def __estTop(self):
		if(not self.topbool):
			raise RuntimeError("La partie n'est pas encore commencee.")

	def creerPartie(self, nom):
		self.__estConnect()
		sent = self.sock.send("CREATE "+nom+"\n".encode())
		if sent == 0:
			raise RuntimeError("Erreur deconnexion. _1")
		id_partie = self.sock.recv(256)
		if id_partie == b'':
			raise RuntimeError("Erreur deconnexion. _2")
		#print("Nouvelle partie : ", id_partie.decode() )
		self.connect = True
		return int(id_partie.decode())

	def top(self):
		if(not self.connect):
			raise RuntimeError("Vous n'etes pas encore connecte.")
		self.sock.send("TOP\n".encode())
		r = int(self.sock.recv(256).decode())
		self.topbool= True
		return r
	
	def rejoindrePartie(self, id_partie, nom):
		self.__estConnect()
		sent = self.sock.send("JOIN "+str(id_partie)+" "+nom+"\n".encode())
		if sent == 0:
			raise RuntimeError("Erreur deconnexion. _3")
		ok = self.sock.recv(256)
		if ok == b'':
			raise RuntimeError("Erreur deconnexion. _4")
		if ok.decode() == "-1":
			raise RuntimeError("La partie", str(id_partie) ,"n'existe pas.")
		if ok.decode() == "-2":
			raise RuntimeError("Le nom", nom ,"est deja pris.")
		if ok.decode() == "-3":
			raise RuntimeError("La partie", str(id_partie) ,"est deja lance.")
		#print (ok.decode())
		self.connect = True
		return True

	def solde(self):
		self.__estTop()
		self.sock.send("SOLDE\n".encode())
		return eval(self.sock.recv(256).decode())
	
	def operationsEnCours(self):
		self.__estTop()
		self.sock.send("OPERATIONS\n".encode())
		return eval(self.sock.recv(256).decode())

	def ask(self, action, prix, volum):
		self.__estTop()
		self.sock.send("ASK "+action+" "+str(prix)+" "+str(volum)+"\n".encode())
		return int(self.sock.recv(256).decode())

	def bid(self, action, prix, volum):
		self.__estTop()
		self.sock.send("BID "+action+" "+str(prix)+" "+str(volum)+"\n".encode())
		return int(self.sock.recv(256).decode())

	def achats(self, action):
		self.__estTop()
		self.sock.send("ACHATS "+action+"\n".encode())
		return eval(self.sock.recv(256).decode())
	
	def ventes(self, action):
		self.__estTop()
		self.sock.send("VENTES "+action+"\n".encode())
		return eval(self.sock.recv(256).decode())

	def historiques(self, action):
		self.__estTop()
		self.sock.send("HISTO "+action+"\n".encode())
		return eval(self.sock.recv(256).decode())

	def suivreOperation(self, id_partie):
		self.__estTop()
		self.sock.send("SUIVRE "+str(id_partie)+"\n".encode())
		return eval(self.sock.recv(256).decode())

	def annulerOperation(self, id_partie):
		self.__estTop()
		self.sock.send("ANNULER "+str(id_partie)+"\n".encode())
		return eval(self.sock.recv(256).decode())




		
