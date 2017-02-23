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
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;

import core.Action;
import core.Config;
import core.Joueur;

public class Client extends Thread {
	private Socket client;
	private DispatcherServeur serveur;
	
	private static final String TOP = "1";
	private static final String SOLDE = "2";
	private static final String OPERATIONS = "3";
	private static final String ACHATS = "4 ";
	private static final String VENTES = "5 ";
	private static final String HISTO = "6 "; //on définit un dictionnaire qui permettra une communication 
	private static final String ASK = "7 ";   //client/serveur avec des messages très courts
	private static final String BID = "8 ";	//sans perdre de lisibilité du code
	private static final String SUIVRE = "9 ";
	private static final String ANNULER = "A ";
	private static final String FIN = "B";
	private static final String CREATE="C ";
	private static final String JOIN="D ";
	
	public Client(Socket client, DispatcherServeur serveur) {
		super();
		this.client = client;
		this.serveur = serveur;
		start();
	}

	public void run() {
		System.out.println("Client connecté");
		int nombreActions=Action.values().length;
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
				if (userInput.startsWith(CREATE) && arguments.length == 2 && !create && !join) {
					String nom = arguments[1];
					numero_partie = (int) (Math.random() * 100000);
					envoyer(out, String.valueOf(numero_partie));
					current = new Partie();
					joueur = current.ajouter_client(client, nom);
					serveur.ajouterPartie(numero_partie, current);
					create = true;
				} else if (userInput.startsWith(JOIN) && arguments.length == 3 && StringUtils.isNumeric(arguments[1]) && !create
						&& !join) {
					numero_partie = Integer.parseInt(arguments[1]);
					String nom = arguments[2];

					if (!serveur.partieExiste(numero_partie)) {
						envoyer(out, "-1");
						continue;
					}

					if (!serveur.getListepartie(numero_partie).getMarche().nom_possible(nom)) {
						envoyer(out, "-2");
						continue;
					}

					if (serveur.getListepartie(numero_partie).getMarche().est_ouvert()) {
						envoyer(out, "-3");
						continue;
					}

					envoyer(out, "0");
					current = serveur.getListepartie(numero_partie);
					joueur = current.ajouter_client(client, nom);
					join = true;
				} else if (userInput.startsWith(TOP) && create && !current.getMarche().est_ouvert()) {
					current.getMarche().commence();
					for (Socket s : current.getListe_client())
						if (s != client) {
							BufferedWriter outAdvers = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
							envoyer(outAdvers, "0");
						}
					StringBuffer sb = new StringBuffer(current.getMarche().getListe_joueurs().size() * 100);;
					sb.append("[");
					for(Joueur j: current.getMarche().getListe_joueurs())
						if(j.getNom()!="banque"){
							sb.append("'");
							sb.append(j.getNom());
							sb.append("',");
						}
					sb.deleteCharAt(sb.length()-1);//Pour retirer le dernier ","
					sb.append("]"); 
					envoyer(out, new String(sb)); 
				} else if (userInput.startsWith(TOP) && join && !current.getMarche().est_ouvert()) {
					// attente retour
				} else if (userInput.startsWith(SOLDE) && (create || join) && current.getMarche().est_ouvert()) {
					String begin = "{'euros':" + String.valueOf(joueur.getSolde_euros()) + ", ";
					envoyer(out, begin + MapToStringPython(joueur.getSolde_actions()) + "}");
				} else if (userInput.startsWith(OPERATIONS) && peut_jouer) {
					envoyer(out, String.valueOf(ListPairToStringPythonKeyOnly(joueur.getOperationsOuvertes())));
				} else if (userInput.startsWith(ACHATS) && arguments.length == 2 && StringUtils.isNumeric(arguments[1])&& peut_jouer){
					int arg=Integer.parseInt(arguments[1]);
					if (arg<nombreActions && arg>=0)  {
						Action a = Action.values()[arg]; 
					envoyer(out, String.valueOf(current.getMarche().getListeAchats(a)));
					}
				} else if (userInput.startsWith(VENTES) && arguments.length == 2 && StringUtils.isNumeric(arguments[1])&& peut_jouer){
					int arg=Integer.parseInt(arguments[1]);
					if(arg<nombreActions && arg>=0)  {
						Action a = Action.values()[arg]; 
					envoyer(out, String.valueOf(current.getMarche().getListeVentes(a)));
					}
				} else if (userInput.startsWith(HISTO) && arguments.length == 3 && StringUtils.isNumeric(arguments[1])
						&&StringUtils.isNumeric(arguments[2]) && (create || join) && current.getMarche().est_ouvert()){
					int arg=Integer.parseInt(arguments[1]);
					if(arg<nombreActions && arg>=0 ){
						Action a = Action.values()[arg]; 
					envoyer(out, String.valueOf(current.getMarche().getHistoriqueEchanges(a,Integer.parseInt(arguments[2]))));
					}
				} else if (userInput.startsWith(ASK) && arguments.length == 4 && StringUtils.isNumeric(arguments[1])
						&& NumberUtils.isCreatable(arguments[2]) && StringUtils.isNumeric(arguments[3]) && peut_jouer) {
					int arg=Integer.parseInt(arguments[1]);
					if(arg<nombreActions && arg>=0){
						Action a = Action.values()[arg]; 
					float prix = Float.parseFloat(arguments[2]);
					int volume = Integer.parseInt(arguments[3]);
					envoyer(out, String.valueOf(current.getMarche().achat(joueur, a, prix, volume)));
					}
				} else if (userInput.startsWith(BID) && arguments.length == 4 && StringUtils.isNumeric(arguments[1])
						&& NumberUtils.isCreatable(arguments[2]) && StringUtils.isNumeric(arguments[3]) && peut_jouer) {
					int arg=Integer.parseInt(arguments[1]);
					if(arg<nombreActions && arg>=0){
						Action a = Action.values()[arg]; 
					float prix = Float.parseFloat(arguments[2]);
					int volume = Integer.parseInt(arguments[3]);
					envoyer(out, String.valueOf(current.getMarche().vend(joueur, a, prix, volume)));
					}
				} else if (userInput.startsWith(SUIVRE) && arguments.length == 2 && StringUtils.isNumeric(arguments[1]) && peut_jouer) {
					int ordre = Integer.parseInt(arguments[1]);
					envoyer(out, String.valueOf(current.getMarche().suivre(joueur, ordre)));
				} else if (userInput.startsWith(ANNULER) && arguments.length == 2 && StringUtils.isNumeric(arguments[1]) && peut_jouer) {
					int ordre = Integer.parseInt(arguments[1]);
					envoyer(out, String.valueOf(current.getMarche().annuler(joueur, ordre)));
				} else if (userInput.startsWith(FIN) && arguments.length == 1 && (create || join) && current.getMarche().est_ouvert()) {
					envoyer(out, String.valueOf(current.getMarche().fin()));
				} else {
					System.out.println("FAIL |" + userInput + "|");
					envoyer(out, "-4");
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

	private void envoyer(BufferedWriter out, String packet) throws IOException {
		StringBuilder length = new StringBuilder(Config.getInstance().PACKET_SIZE);
		String llength = String.valueOf(packet.length());
		if(packet.length() > 99999){
			System.out.println("ERROR : packet too small");
			System.err.println("ERROR : packet too small");
		}
		for(int i=llength.length();i<Config.getInstance().PACKET_SIZE;i++)
			length.insert(0, "0");
		length.append(llength);
		
		out.write(new String(length));
		out.write(packet);
		out.flush();
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
