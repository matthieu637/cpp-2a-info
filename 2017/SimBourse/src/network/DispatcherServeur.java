package network;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import core.Config;


public class DispatcherServeur {

	private Map<Integer, Partie> liste_partie;

	public DispatcherServeur() {
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(Config.getInstance().PORT);
			this.liste_partie = new HashMap<Integer, Partie>();
			Console c = new Console(liste_partie);
			c.start();

			while (true) {
				Socket client = ss.accept();
				new Client(client, this);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//synchronized est plus simple pour ce cas :
	//peu de concurrence, objet unique, peu d'appel, ...
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
		new DispatcherServeur();
	}

}
