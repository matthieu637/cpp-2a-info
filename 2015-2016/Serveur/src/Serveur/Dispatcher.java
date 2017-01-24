package Serveur;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Dispatcher {

	final static int PORT = 80;
	private Map<Integer, Partie> liste_partie;

	public Dispatcher() {
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(PORT);
			this.liste_partie = new HashMap<Integer, Partie>();

			while (true) {
				Socket client = ss.accept();
				new Client(client, this);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	synchronized public boolean partieExiste(int cle) {
		return liste_partie.containsKey(cle);
	}

	synchronized public Partie getListepartie(int valeur) {
		return liste_partie.get(valeur);
	}

	synchronized public void ajouterPartie(int valeur, Partie existe) {
		this.liste_partie.put(valeur, existe);
	}

	synchronized public void retirerPartie(int numero_partie) {
		this.liste_partie.remove(numero_partie);
	}

	public static void main(String[] args) {
		new Dispatcher();

	}

}
