package network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import core.Action;
import core.Joueur;

public class Client extends Thread {
	private Socket client;
	private DispatcherServeur serveur;

	public Client(Socket client, DispatcherServeur serveur) {
		super();
		this.client = client;
		this.serveur = serveur;
		start();
	}

	public void run() {
		System.out.println("Client connecté");

		Partie current = null;
		Joueur joueur = null;
		int numero_partie = -1;
		boolean create = false;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

			String userInput;
			boolean join = false;

			while ((userInput = in.readLine()) != null) {
				System.out.println(userInput + "\n");
				String[] arguments = userInput.split(" ");
				boolean peut_jouer = (create || join) && current.getMarche().est_ouvert() && !current.getMarche().est_fini();

				// Utilisateur n'ayant ni créé ni rejoint peut créer
				if (userInput.startsWith("CREATE ") && arguments.length == 2 && !create && !join) {
					String nom = arguments[1];
					numero_partie = (int) (Math.random() * 100000);
					out.write(String.valueOf(numero_partie));
					out.flush();
					current = new Partie();
					joueur = current.ajouter_client(client, nom);
					serveur.ajouterPartie(numero_partie, current);
					create = true;
				} else if (userInput.startsWith("JOIN ") && arguments.length == 3 && StringUtils.isNumeric(arguments[1]) && !create
						&& !join) {
					numero_partie = Integer.parseInt(arguments[1]);
					String nom = arguments[2];

					if (!serveur.partieExiste(numero_partie)) {
						out.write("-1");
						out.flush();
						continue;
					}

					if (!serveur.getListepartie(numero_partie).getMarche().nom_possible(nom)) {
						out.write("-2");
						out.flush();
						continue;
					}

					if (serveur.getListepartie(numero_partie).getMarche().est_ouvert()) {
						out.write("-3");
						out.flush();
						continue;
					}

					out.write("0");
					out.flush();
					current = serveur.getListepartie(numero_partie);
					joueur = current.ajouter_client(client, nom);
					join = true;
				} else if (userInput.startsWith("TOP") && create && !current.getMarche().est_ouvert()) {
					current.getMarche().commence();
					for (Socket s : current.getListe_client())
						if (s != client) {
							BufferedWriter outAdvers = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
							outAdvers.write("0");
							outAdvers.flush();
						}
					out.write("0");
					out.flush();

				} else if (userInput.startsWith("TOP") && join && !current.getMarche().est_ouvert()) {
					// attente retour
				} else if (userInput.startsWith("SOLDE") && (create || join) && current.getMarche().est_ouvert()) {
					String begin = "{'euros':" + String.valueOf(joueur.getSolde_euros()) + ", ";
					out.write(begin + MapToStringPython(joueur.getSolde_actions()) + "}");
					out.flush();
				} else if (userInput.startsWith("OPERATIONS") && peut_jouer) {
					out.write(String.valueOf(ListPairToStringPythonKeyOnly(joueur.getOperationsOuvertes())));
					out.flush();
				} else if (userInput.startsWith("ACHATS ") && arguments.length == 2 && Action.estValide(arguments[1]) && peut_jouer) {
					Action a = Action.from(arguments[1]);
					out.write(String.valueOf(current.getMarche().getListeAchats(a)));
					out.flush();
				} else if (userInput.startsWith("VENTES ") && arguments.length == 2 && Action.estValide(arguments[1]) && peut_jouer) {
					Action a = Action.from(arguments[1]);
					out.write(String.valueOf(current.getMarche().getListeVentes(a)));
					out.flush();
				} else if (userInput.startsWith("HISTO ") && arguments.length == 2 && Action.estValide(arguments[1]) && 
						(create || join) && current.getMarche().est_ouvert()) {
					Action a = Action.from(arguments[1]);
					out.write(String.valueOf(current.getMarche().getHistoriqueEchanges(a)));
					out.flush();
				} else if (userInput.startsWith("ASK ") && arguments.length == 4 && Action.estValide(arguments[1])
						&& StringUtils.isNumeric(arguments[2]) && StringUtils.isNumeric(arguments[3]) && peut_jouer) {
					Action a = Action.from(arguments[1]);
					float prix = Float.parseFloat(arguments[2]);
					int volume = Integer.parseInt(arguments[3]);
					out.write(String.valueOf(current.getMarche().achat(joueur, a, prix, volume)));
					out.flush();
				} else if (userInput.startsWith("BID ") && arguments.length == 4 && Action.estValide(arguments[1])
						&& StringUtils.isNumeric(arguments[2]) && StringUtils.isNumeric(arguments[3]) && peut_jouer) {
					Action a = Action.from(arguments[1]);
					float prix = Float.parseFloat(arguments[2]);
					int volume = Integer.parseInt(arguments[3]);
					out.write(String.valueOf(current.getMarche().vend(joueur, a, prix, volume)));
					out.flush();
				} else if (userInput.startsWith("SUIVRE ") && arguments.length == 2 && StringUtils.isNumeric(arguments[1]) && peut_jouer) {
					int ordre = Integer.parseInt(arguments[1]);
					out.write(String.valueOf(current.getMarche().suivre(joueur, ordre)));
					out.flush();
				} else if (userInput.startsWith("ANNULER ") && arguments.length == 2 && StringUtils.isNumeric(arguments[1]) && peut_jouer) {
					int ordre = Integer.parseInt(arguments[1]);
					out.write(String.valueOf(current.getMarche().annuler(joueur, ordre)));
					out.flush();
				} else if (userInput.startsWith("FIN") && arguments.length == 1 && (create || join) && current.getMarche().est_ouvert()) {
					out.write(String.valueOf(current.getMarche().fin()));
					out.flush();
				} else {
					System.out.println("FAIL |" + userInput + "|");
					out.write("-1");
					out.flush();
				}
			}

			libererPartie(current, numero_partie, create, joueur);
		} catch (Exception e) {
			if(!(e instanceof SocketException))
				e.printStackTrace();
			try {
				libererPartie(current, numero_partie, create, joueur);
			} catch (Exception e1) {
				e1.printStackTrace();
				if(!(e1 instanceof SocketException))
					e.printStackTrace();
			}
		}

	}

	private void libererPartie(Partie current, int numero_partie, boolean create, Joueur joueur) throws IOException {
		if (joueur != null && current != null) {
			current.getMarche().retirer_joueur(joueur);
		}

		if (current != null && create) {
			for (Socket s : current.getListe_client())
				s.close();

			current.getMarche().destroy();
			serveur.retirerPartie(numero_partie);
		} else if (!create) {
			client.close();
		}
		System.out.println("Client déconnecté");
	}

	private <E, V> String MapToStringPython(Map<E, V> map) {
		StringBuffer sb = new StringBuffer(12 * map.size());
		for (Map.Entry<E, V> v : map.entrySet()) {
			sb.append('\'');
			sb.append(String.valueOf(v.getKey()));
			sb.append("':");
			sb.append(String.valueOf(v.getValue()));
			sb.append(',');
		}

		if (map.size() != 0)
			sb.deleteCharAt(sb.length() - 1);
		return new String(sb);
	}

	private <E, V> String ListPairToStringPythonKeyOnly(List<Pair<E, V>> list) {
		StringBuffer sb = new StringBuffer(12 * list.size());
		sb.append('[');
		for (Pair<E, V> v : list) {
			sb.append(String.valueOf(v.getKey()));
			sb.append(',');
		}

		if (list.size() != 0)
			sb.deleteCharAt(sb.length() - 1);
		sb.append(']');
		return new String(sb);
	}
}
