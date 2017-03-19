package network;

import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import core.Joueur;
import core.Marche;

public class Partie {
	private final Marche marche;
	private final List<Socket> liste_client;
	private final List<String> liste_HostAdress;
	private final int modeExam;
	
	public Partie(int modeBanque, int modeExamen) {
		modeExam=modeExamen;
		liste_HostAdress=new LinkedList<String>();
		marche = new Marche(modeBanque);
		liste_client = new LinkedList<>();
	}

	public List<Socket> getListe_client() {
		return liste_client;
	}
	
	public Joueur ajouter_client(Socket s, String nom, String nom_complet){
		liste_client.add(s);
		if(modeExam==1)
			liste_HostAdress.add(s.getInetAddress().getHostAddress());
		return marche.creer_joueur(nom, nom_complet+":"+s.getInetAddress().getHostAddress());
	}
	
	public Boolean isModeExamen(){
		if(modeExam==1)
			return true;
		return false;
	}
	public Boolean testUniciteDeLaConnexion(Socket s){

		String hostAdress=s.getInetAddress().getHostAddress();
		Iterator<String> iter= liste_HostAdress.iterator();
		while(iter.hasNext())
			if(hostAdress.equals(iter.next()))
				return false;
		
		return true;
	}

	public Marche getMarche() {
		return marche;
	}

	@Override
	public String toString() {
		return String.valueOf(marche);
	}

	public void retirerJoueur(Socket client) {
		liste_client.remove(client);
	}
}
