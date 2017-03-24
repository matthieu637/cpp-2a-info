package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import core.Config;

public class Console extends Thread {

	private final Map<Integer, Partie> liste_partie;

	public Console(Map<Integer, Partie> liste_partie) {
		this.liste_partie = liste_partie;
	}

	@Override
	public void run() {
		try {
			String commande;
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

			while ((commande = in.readLine()) != null) {
				if (commande.equalsIgnoreCase("stop")){
					Config.getInstance().ecrireCles();
					System.exit(1);
				}
				else if (commande.equalsIgnoreCase("help")) {
					System.err.println("Commandes :");
					System.err.println("\tstop");
					System.err.println("\thelp");
					System.err.println("\tliste");
					System.err.println("\tthread");
					System.err.println("\tparticipants");
				} else if (commande.equalsIgnoreCase("liste")) {
					for (int id : liste_partie.keySet())
						System.err.println(id + " " + liste_partie.get(id));
					System.err.println();
				} else if (commande.equalsIgnoreCase("thread")) {
					System.err.println(Thread.activeCount());
				} else if (commande.equalsIgnoreCase("participants")) {
					for (int id : liste_partie.keySet())
						System.err.println(id + " " + liste_partie.get(id).getListe_client().size());
					System.err.println();
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
