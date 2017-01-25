package network;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import core.Config;
import core.Marche;


public class Dispatcher {

	private Map<Integer, Partie> liste_partie;

	public Dispatcher() {
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(Config.getInstance().PORT);
			this.liste_partie = new HashMap<Integer, Partie>();
			Marche m = new Marche(); //un seul marché pour le moment

			while (true) {
				Socket client = ss.accept();
				new Client(client, this, m);
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
