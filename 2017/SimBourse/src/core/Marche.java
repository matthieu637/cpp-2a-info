package core;

import java.util.List;
import java.util.Map;

public class Marche {
	private int id_marche;
	private boolean ouvert;
	private Map<Action, List<Ordre>> liste_achats;
	
	public Marche() {
		ouvert = false;
	}
	
	public boolean est_ouvert() {
		return ouvert;
	}

	public void commence() {
		ouvert = true;
	}

	public Joueur creer_joueur(String nom) {
		return new Joueur(nom);
	}

	public boolean nom_possible(String nom) {
		// TODO Auto-generated method stub
		return true;
	}

	public Object getListeAchats(Action a) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getListeVentes(Action a) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getHistoriqueEchanges(Action a) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object achat(Joueur joueur, Action a, float prix, int volume) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object vend(Joueur joueur, Action a, float prix, int volume) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object suivre(Joueur joueur, int ordre) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
