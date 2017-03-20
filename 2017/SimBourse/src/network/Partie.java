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
	private final boolean isModeExam;
	
	public Partie(int modeBanque, int modeExamen) {
		isModeExam= modeExamen==1;	
		liste_HostAdress=new LinkedList<String>();
		marche = new Marche(modeBanque);
		liste_client = new LinkedList<>();
	}

	public List<Socket> getListe_client() {
		return liste_client;
	}
	
	public Joueur ajouter_client(Socket s, String nom, String nom_complet){
		liste_client.add(s);
		if(isModeExam)
			liste_HostAdress.add(nom_complet+":"+s.getInetAddress().getHostAddress());
		return marche.creer_joueur(nom, nom_complet+":"+s.getInetAddress().getHostAddress());
	}
	
	public boolean isModeExamen(){
		return isModeExam;
	}
	public boolean testUniciteDeLaConnexion(Socket s,String nom_complet){

		String hostAdress=nom_complet+":"+s.getInetAddress().getHostAddress();
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

	public void retirerJoueur(Socket client,String nom_complet) {
		if(isModeExam)
			liste_HostAdress.remove(nom_complet+":"+client.getInetAddress().getHostAddress());
		liste_client.remove(client);
	}
}
