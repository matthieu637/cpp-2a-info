package core;

import java.util.HashMap;
import java.util.Map;

public class Joueur {
	
	private final int id_joueur;
	private final String nom;
	private int solde_euros;
	private Map <Action,Integer> solde_actions;
	
	public Joueur(int id_joueur, String nom) {
		super();
		this.id_joueur = id_joueur;
		this.nom = nom;
		this.solde_euros = Config.getInstance().SOLDE_EUROS_INIT;
		this.solde_actions = new HashMap<Action,Integer>();
		
		for(Action a : Action.values())
			this.solde_actions.put(a, Config.getInstance().SOLDE_ACTIONS_INIT);
	}

	public int getId_joueur() {
		return id_joueur;
	}

	public String getNom() {
		return nom;
	}

	public float getSolde_euros() {
		return solde_euros;
	}

	public void setSolde_euros(int solde_euros) {
		this.solde_euros = solde_euros;
	}

	public Map<Action, Integer> getSolde_actions() {
		return solde_actions;
	}

	public void setSolde_actions(Map<Action, Integer> solde_actions) {
		this.solde_actions = solde_actions;
	}
	
	
}
