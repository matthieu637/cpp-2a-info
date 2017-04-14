package network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
	private boolean attendTop;

	private static final String TOP = "1";
	private static final String SOLDE = "2";
	private static final String OPERATIONS = "3";
	private static final String ACHATS = "4 ";
	private static final String VENTES = "5 ";
	private static final String HISTO = "6 "; // on définit un dictionnaire qui permettra une communication
	private static final String ASK = "7 "; // client/serveur avec des messages très courts
	private static final String BID = "8 "; // sans perdre de lisibilité du code
	private static final String SUIVRE = "9 ";
	private static final String ANNULER = "A ";
	private static final String FIN = "B";
	private static final String CREATE = "C ";
	private static final String JOIN = "D ";
	private static final String LISTECOUPS = "E";
	private static final String AVANTTOP = "F";

	public Client(Socket client, DispatcherServeur serveur) {
		super();
		this.client = client;
		this.serveur = serveur;
		this.attendTop=false;
		start();
	}

	public void run() {
		System.out.println("Client connecté");
		int nombreActions = Action.values().length;
		Partie current = null;
		Joueur joueur = null;
		int numero_partie = -1;
		boolean create = false;
		String identifier = "";
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()),
					Config.getInstance().MAX_PACKET_SIZE_INPUT);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

			envoyer(out, Config.getInstance().VERSION);
			identifier = childishness(out, in);
			
			String userInput;
			boolean join = false;

			while ((userInput = in.readLine()) != null) {
				// System.out.println(userInput + "\n");

				String[] arguments = userInput.split(" ");
				boolean peut_jouer = (create || join) && current.getMarche().est_ouvert()
						&& !current.getMarche().est_fini();

				// Utilisateur n'ayant ni créé ni rejoint peut créer

				if (userInput.startsWith(SOLDE) && (create || join) && current.getMarche().est_ouvert()) {
					String begin = "{'euros':" + String.valueOf(joueur.getSolde_euros()) + ", ";
					envoyer(out, begin + MapToStringPython(joueur.getSolde_actions()) + "}");
				} else if (userInput.startsWith(OPERATIONS) && peut_jouer) {
					envoyer(out, String.valueOf(ListPairToStringPythonKeyOnly(joueur.getOperationsOuvertes())));
				} else if (userInput.startsWith(ACHATS)) {
					if (arguments.length == 2 && StringUtils.isNumeric(arguments[1]) && peut_jouer) {
						int arg = Integer.parseInt(arguments[1]);
						if (arg < nombreActions && arg >= 0) {
							Action a = Action.values()[arg];
							envoyer(out, current.getMarche().getListeAchatsString(a, 0));
						} else
							envoyer(out, "-4");
					} else if (arguments.length == 3 && StringUtils.isNumeric(arguments[1])
							&& StringUtils.isNumeric(arguments[2]) && peut_jouer) {
						int arg1 = Integer.parseInt(arguments[1]);
						if (arg1 < nombreActions && arg1 >= 0) {
							int arg2 = Integer.parseInt(arguments[2]);
							Action a = Action.values()[arg1];
							envoyer(out, current.getMarche().getListeAchatsString(a, arg2));
						} else
							envoyer(out, "-4");
					} else
						envoyer(out, "-4");
				} else if (userInput.startsWith(VENTES)) {
					if (arguments.length == 2 && StringUtils.isNumeric(arguments[1]) && peut_jouer) {
						int arg = Integer.parseInt(arguments[1]);
						if (arg < nombreActions && arg >= 0) {
							Action a = Action.values()[arg];
							envoyer(out, current.getMarche().getListeVentesString(a, 0));
						} else
							envoyer(out, "-4");
					} else if (arguments.length == 3 && StringUtils.isNumeric(arguments[1])
							&& StringUtils.isNumeric(arguments[2]) && peut_jouer) {
						int arg1 = Integer.parseInt(arguments[1]);
						if (arg1 < nombreActions && arg1 >= 0) {
							int arg2 = Integer.parseInt(arguments[2]);
							Action a = Action.values()[arg1];
							envoyer(out, current.getMarche().getListeVentesString(a, arg2));
						} else
							envoyer(out, "-4");
					} else
						envoyer(out, "-4");
				} else if (userInput.startsWith(HISTO) && arguments.length == 3 && StringUtils.isNumeric(arguments[1])
						&& StringUtils.isNumeric(arguments[2]) && (create || join)
						&& current.getMarche().est_ouvert()) {
					int arg = Integer.parseInt(arguments[1]);
					if (arg < nombreActions && arg >= 0) {
						Action a = Action.values()[arg];
						envoyer(out, String
								.valueOf(current.getMarche().getHistoriqueEchanges(a, Integer.parseInt(arguments[2]))));
					} else
						envoyer(out, "-4");
				} else if (userInput.startsWith(ASK) && arguments.length == 4 && StringUtils.isNumeric(arguments[1])
						&& NumberUtils.isCreatable(arguments[2]) && StringUtils.isNumeric(arguments[3]) && peut_jouer) {
					int arg = Integer.parseInt(arguments[1]);
					if (arg < nombreActions && arg >= 0) {
						Action a = Action.values()[arg];
						float prix = Float.parseFloat(arguments[2]);
						int volume = Integer.parseInt(arguments[3]);
						envoyer(out, String.valueOf(current.getMarche().achat(joueur, a, prix, volume)));
					} else
						envoyer(out, "-12");
				} else if (userInput.startsWith(BID) && arguments.length == 4 && StringUtils.isNumeric(arguments[1])
						&& NumberUtils.isCreatable(arguments[2]) && StringUtils.isNumeric(arguments[3]) && peut_jouer) {
					int arg = Integer.parseInt(arguments[1]);
					if (arg < nombreActions && arg >= 0) {
						Action a = Action.values()[arg];
						float prix = Float.parseFloat(arguments[2]);
						int volume = Integer.parseInt(arguments[3]);
						envoyer(out, String.valueOf(current.getMarche().vend(joueur, a, prix, volume)));
					} else
						envoyer(out, "-12");
				} else if (userInput.startsWith(SUIVRE) && arguments.length == 2 && StringUtils.isNumeric(arguments[1])
						&& peut_jouer) {
					int ordre = Integer.parseInt(arguments[1]);
					envoyer(out, String.valueOf(current.getMarche().suivre(joueur, ordre)));
				} else if (userInput.startsWith(ANNULER) && arguments.length == 2 && StringUtils.isNumeric(arguments[1])
						&& peut_jouer) {
					int ordre = Integer.parseInt(arguments[1]);
					envoyer(out, String.valueOf(current.getMarche().annuler(joueur, ordre)));
				} else if (userInput.startsWith(FIN) && arguments.length == 1 && (create || join)
						&& current.getMarche().est_ouvert()) {
					envoyer(out, String.valueOf(current.getMarche().fin()));
				} else if (userInput.startsWith(LISTECOUPS) && (create || join) && current.getMarche().est_fini()) {
					envoyer(out, current.getMarche().getListeOperationsString());
				} else if (userInput.startsWith(AVANTTOP) && create && !current.getMarche().est_ouvert()) {
					envoyer(out, current.getMarche().getListeJoueursStringDico());
				} else if (userInput.startsWith(CREATE) && arguments.length == 4 && !create && !join) {
					String nom = arguments[1];
					String modeBanque= arguments[2];
					String modeExam= arguments[3];
					
					numero_partie = (int) (Math.random() * 100000);
					if ((modeBanque.equals("1")||modeBanque.equals("2")||modeBanque.equals("3")) && (modeExam.equals("0")||modeExam.equals("1"))){
						if(modeExam.equals("1") && !Config.getInstance().cles.containsKey(nom)) {
							envoyer(out,"-5");
						}
						else{
							envoyer(out, String.valueOf(numero_partie));
							current = new Partie(Integer.parseInt(modeBanque),Integer.parseInt(modeExam));
							joueur = current.ajouter_client(this, nom, identifier);
							serveur.ajouterPartie(numero_partie, current);
							create = true;
						}
					}
					else
						envoyer(out,"-4");
					
				} else if (userInput.startsWith(JOIN) && arguments.length == 3 && StringUtils.isNumeric(arguments[1])
						&& !create && !join) {
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
					
					current = serveur.getListepartie(numero_partie);
					
					if (current.isModeExamen()&&( !Config.getInstance().cles.containsKey(nom) || !current.testUniciteDeLaConnexion(client,nom)))
					{
						envoyer(out, "-5");
						continue;
					}

					envoyer(out, "0");
					join=true;
					joueur = current.ajouter_client(this, nom, identifier);
				} else if (userInput.startsWith(TOP) && create && !current.getMarche().est_ouvert()) {
					String retour = current.getMarche().getListeJoueursStringDico();
					long futureTop = System.currentTimeMillis() + 2000;//in 2 seconds
					for (Client c : current.getListe_client()){
						Socket s = c.getSock();
						try {
							if (!s.equals(client) && c.isAttendTop()) {
								BufferedWriter outAdvers = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
								envoyer(outAdvers, "("+String.valueOf((int)(futureTop - System.currentTimeMillis()))+",)");
							}
						} catch(Exception e) {
							//peu importe client perdu
						}
					}
					envoyer(out, "("+String.valueOf((int)(futureTop - System.currentTimeMillis())+","+retour+")"));
					long reste = futureTop - System.currentTimeMillis();
					if(reste > 0)
						Thread.sleep(reste);
					current.getMarche().commence();
				} else if (userInput.startsWith(TOP) && !current.getMarche().est_ouvert()) {
				  // attend le top
					attendTop = true;
				} else if (userInput.startsWith(TOP) && current.getMarche().est_ouvert() && join && !attendTop && !create) {
				  // retardataires qui ont oublie d'appeller top
					envoyer(out, "0");
					attendTop = true;
				} else {
					System.out.println("FAIL |" + userInput + "|");
					envoyer(out, "-4");
				}
			}

			libererPartie(current, numero_partie, create, joueur,identifier);
		} catch (Exception e) {
			if (!(e instanceof SocketException))
				e.printStackTrace();
			try {
				libererPartie(current, numero_partie, create, joueur,identifier);
			} catch (Exception e1) {
				e1.printStackTrace();
				if (!(e1 instanceof SocketException))
					e.printStackTrace();
			}
		}

	}

	public Socket getSock() {
		return client;
	}

	public boolean isAttendTop() {
		return attendTop;
	}

	private String childishness(BufferedWriter out, BufferedReader in) throws IOException, NoSuchAlgorithmException {
		int ids = (int) (Math.random() * 100000);
		envoyer(out, String.valueOf(ids));
		envoyer(out, Config.getInstance().CHILDISHNESS);
		String identifier = in.readLine();
		MessageDigest mdj = MessageDigest.getInstance("MD5");
		mdj.update(identifier.getBytes());
		byte[] digest = mdj.digest();
		StringBuffer sb = new StringBuffer();
		for (byte b : digest) {
			sb.append(String.format("%02x", b & 0xff));
		}
		String md = in.readLine();
		if(!md.equals(new String(sb)))
			throw new IOException("child");
		
		return identifier.substring(identifier.indexOf(":")+1, identifier.length());
	}

	private void envoyer(BufferedWriter out, String packet) throws IOException {
		StringBuilder length = new StringBuilder(Config.getInstance().PACKET_SIZE);
		String llength = String.valueOf(packet.length());
		if (packet.length() > Config.getInstance().RESERVED_SIZE_SEND_PACKET) {
			System.out.println("ERROR : packet too small");
			System.err.println("ERROR : packet too small");
		}
		for (int i = llength.length(); i < Config.getInstance().PACKET_SIZE; i++)
			length.insert(0, "0");
		length.append(llength);

		out.write(new String(length));
		out.write(packet);
		out.flush();
	}

	private void libererPartie(Partie current, int numero_partie, boolean create, Joueur joueur, String identifier) throws IOException {
		if (joueur != null && current != null) {
			current.getMarche().retirer_joueur(joueur);
		}

		if (current != null && create) {
			for (Client c : current.getListe_client()){
				Socket s = c.getSock();
				s.close();
			}

			current.getMarche().destroy();
			serveur.retirerPartie(numero_partie);
		} else if (!create) {
			if(current != null)
				current.retirerJoueur(client,identifier);
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
