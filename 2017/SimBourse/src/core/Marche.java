package core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Marche {
	private boolean ouvert;
	private Map<Action, Set<Ordre>> liste_achats;
	private Map<Action, Set<Ordre>> liste_ventes;
	private List<Joueur> liste_joueurs;
	private Set<Integer> liste_id_ordres;
	private Map<Action, Set<Echange>> historiques;

	public Marche() {
		ouvert = false;
		liste_achats = new HashMap<>();
		liste_ventes = new HashMap<>();
		historiques = new HashMap<>();
		for (Action a : Action.values()) {
			liste_achats.put(a, new TreeSet<Ordre>());
			liste_ventes.put(a, new TreeSet<Ordre>());
			historiques.put(a, new TreeSet<Echange>());
		}
		liste_joueurs = new LinkedList<>();
		liste_id_ordres = new TreeSet<>();
	}

	public boolean est_ouvert() {
		return ouvert;
	}

	public void commence() {
		ouvert = true;
	}

	public Joueur creer_joueur(String nom) {
		Joueur j = new Joueur(nom);
		liste_joueurs.add(j);
		return j;
	}

	public boolean nom_possible(String nom) {
		for (Joueur j : liste_joueurs)
			if (j.getNom().equalsIgnoreCase(nom))
				return true;
		return false;
	}

	public Set<Ordre> getListeAchats(Action a) {
		return liste_achats.get(a);
	}

	public Set<Ordre> getListeVentes(Action a) {
		return liste_ventes.get(a);
	}

	public Object getHistoriqueEchanges(Action a) {
		// TODO Auto-generated method stub
		return null;
	}

	public int achat(Joueur joueur, Action a, float prix, int volume) {
		if (volume <= 0.0)
			return -1;
		if (prix <= 0.0)
			return -1;

		int id = creer_id_ordre();
		Ordre achat = new Achat(id, a, prix, volume, joueur);
		if (liste_ventes.get(a).size() > 0) {
			Ordre vente = liste_ventes.get(a).iterator().next();

			if (prix <= vente.prix && volume >= vente.volume)
				// ajout historique
				return 0;
			else if (prix <= vente.prix)
				;
			// ajout historique
			// else remove volume
		}

		liste_achats.get(a).add(achat);
		return id;
	}

	public Object vend(Joueur joueur, Action a, float prix, int volume) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object suivre(Joueur joueur, int ordre) {
		// TODO Auto-generated method stub
		return null;
	}

	private synchronized int creer_id_ordre() {
		int numero_partie = (int) (Math.random() * 100000000);
		while (liste_id_ordres.contains((Integer) numero_partie))
			numero_partie = (int) (Math.random() * 100000000);
		liste_id_ordres.add(numero_partie);
		return numero_partie;
	}
}
