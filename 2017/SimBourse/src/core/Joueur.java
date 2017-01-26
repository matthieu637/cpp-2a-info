package core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Joueur {
	
	private final String nom;
	private int solde_euros;
	private Map <Action,Integer> solde_actions;
	
	public Joueur(String nom) {
		super();
		this.nom = nom;
		this.solde_euros = Config.getInstance().SOLDE_EUROS_INIT;
		this.solde_actions = new HashMap<Action,Integer>();
		
		for(Action a : Action.values())
			this.solde_actions.put(a, Config.getInstance().SOLDE_ACTIONS_INIT);
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

	public List<Ordre> getOperationsOuvertes() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
