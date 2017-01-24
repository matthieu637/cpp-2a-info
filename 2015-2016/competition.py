import socket
from time import time

#variable globale
est_connecte=False
appel_attente=False
est_premier=False
my_socket=None
my_spend_time=0
last_save_time=0

#constante
PORT1=80
HOST1="paris.matthieu-zimmer.net"

PORT2=23456
HOST2="matthieu-zimmer.net"

#determine le bon host
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.settimeout(5)
result = sock.connect_ex((HOST1,PORT1))
if result == 0:
	PORT=PORT1
	HOST=HOST1
else:	
	PORT=PORT2
	HOST=HOST2
sock.close()
del sock
del result

def creerPartie():
	global est_connecte, my_socket, est_premier
	if est_connecte or my_socket != None:
		raise RuntimeError("Deja connecte ! Relancer le script.")
	try:
		my_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		my_socket.connect((HOST, PORT))
		sent = my_socket.send("CREATE\n".encode())
		if sent == 0:
			my_socket = None
			raise RuntimeError("Erreur deconnexion. _1")
		id_partie = my_socket.recv(256)
		if id_partie == b'':
			my_socket = None
			raise RuntimeError("Erreur deconnexion. _2")
		print("Nouvelle partie : ", id_partie.decode() )
		est_connecte=True
		est_premier=True
		return int(id_partie.decode())
	except (ConnectionRefusedError):
		my_socket = None
		raise RuntimeError("Impossible de se connecter au serveur. _1")
	
def rejoindrePartie(id):
	global est_connecte, my_socket, est_premier
	if est_connecte or my_socket != None:
		raise RuntimeError("Deja connecte ! Relancer le script.")
	try:
		my_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		my_socket.connect((HOST, PORT))
		sent = my_socket.send(("JOIN "+str(id)+"\n").encode())
		if sent == 0:
			my_socket = None
			raise RuntimeError("Erreur deconnexion. _3")
		ok = my_socket.recv(256)
		if ok == b'':
			my_socket = None
			raise RuntimeError("Erreur deconnexion. _4")
		if ok.decode() != "YES":
			print("La partie", id ,"n'existe pas ou est deja pleine.")
			return False
		est_connecte=True
		est_premier=False
		return True
	except (ConnectionRefusedError):
		my_socket = None
		raise RuntimeError("Impossible de se connecter au serveur. _2")
		return False

def jouer(colonne):
	global est_connecte, my_socket, est_premier, appel_attente, my_spend_time, last_save_time
	if not est_connecte or my_socket == None:
		raise RuntimeError("Erreur deconnexion. _1")
	if not est_premier and not appel_attente:
		raise RuntimeError("Vous avez rejoint la partie. Vous devez attendre le premier coup.")
	if(last_save_time == 0):
		last_save_time = time()
	my_spend_time = my_spend_time + (time() - last_save_time)
	sent = my_socket.send(("PLAY "+str(colonne)+"\n").encode())
	if sent == 0:
		est_connecte=False
		raise RuntimeError("Erreur deconnexion. _5")
	colonne_adverse = my_socket.recv(256)
	if colonne_adverse == b'':
		est_connecte=False
		raise RuntimeError("Erreur deconnexion. _6")
	last_save_time = time()
	return int(colonne_adverse.decode())

def attentePremierCoup():
	global est_connecte, my_socket, est_premier, appel_attente, last_save_time
	if not est_connecte or my_socket == None:
		raise RuntimeError("Erreur deconnexion. _1")
	if est_premier:
		raise RuntimeError("Vous avez creee la partie. Vous devez commencer a jouer.")
	if appel_attente:
		raise RuntimeError("Vous avez deja attendu, il faut jouer.")
	sent = my_socket.send(("RECEIVE\n").encode())
	if sent == 0:
		est_connecte=False
		raise RuntimeError("Erreur deconnexion. _7")
	colonne_adverse = my_socket.recv(256)
	if colonne_adverse == b'':
		est_connecte=False
		raise RuntimeError("Erreur deconnexion. _8")
	appel_attente=True
	last_save_time = time()
	return int(colonne_adverse.decode())

def fin(colonne):
	global est_connecte, my_socket, my_spend_time
	if not est_connecte or my_socket == None:
		raise RuntimeError("Erreur deconnexion. _9")
	sent = my_socket.send(("PLAY "+str(colonne)+"\n").encode())
	if sent == 0:
		est_connecte=False
		raise RuntimeError("Erreur deconnexion. _10")
	return int(my_spend_time)
	
