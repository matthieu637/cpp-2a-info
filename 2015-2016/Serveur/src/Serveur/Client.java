package Serveur;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.StringTokenizer;

public class Client extends Thread {
	private Socket client;
	private Dispatcher serveur;

	public Client(Socket client, Dispatcher serveur) {
		super();
		this.client = client;
		this.serveur = serveur;
		start();

	}

	public void run() {
		System.out.println("Client connecté");
		
		Partie current = null;
		int numero_partie = -1;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

			String userInput;
			boolean create = false;
			boolean join = false;

			while ((userInput = in.readLine()) != null) {
				System.out.println(userInput + "\n");

				if (userInput.equals("CREATE") && !create && !join) {
					numero_partie = (int) (Math.random() * 100000);
					out.write(String.valueOf(numero_partie));
					current = new Partie();
					serveur.ajouterPartie(numero_partie, current);
					current.setClient1(client);
					out.flush();
					create = true;
				} else if (userInput.startsWith("JOIN ") && !create && !join) {
					StringTokenizer input = new StringTokenizer(userInput, " ");
					input.nextToken();
					numero_partie = Integer.parseInt(input.nextToken());
					if (serveur.partieExiste(numero_partie) && (!serveur.getListepartie(numero_partie).isJoined())) {
						out.write("YES");
						out.flush();
						current = serveur.getListepartie(numero_partie);
						current.setJoined(true);
						current.setClient2(client);
						join = true;
					} else {
						out.write("NO");
						out.flush();
					}
				} else if (userInput.startsWith("PLAY ") && (create || join) && (current.isStarted() || 
						(!current.isStarted() && create)) ) {
					StringTokenizer input = new StringTokenizer(userInput, " ");
					input.nextToken();
					int number = Integer.parseInt(input.nextToken());

					Socket oppose = getClientOppose(current, client);
					BufferedWriter outAdvers = new BufferedWriter(new OutputStreamWriter(oppose.getOutputStream()));
					outAdvers.write(String.valueOf(number));
					outAdvers.flush();

				} else if (userInput.equals("RECEIVE") && (create || join) && (!current.isStarted() && join)) {
					current.setStarted(true);
				}
			}
			
			libererPartie(current, numero_partie);
		} catch (Exception e) {
			try {
				libererPartie(current, numero_partie);
			} catch (Exception e1) {
			}
		}

	}
	
	private void libererPartie(Partie current, int numero_partie) throws IOException{
		if (current != null) {
			if (current.getClient1() != null)
				current.getClient1().close();
			if (current.getClient2() != null)
				current.getClient2().close();
			
			serveur.retirerPartie(numero_partie);
		}
		System.out.println("Client déconnecté");
	}

	private Socket getClientOppose(Partie p, Socket me) {
		if (p.getClient1().equals(me))
			return p.getClient2();
		else
			return p.getClient1();
	}

}
