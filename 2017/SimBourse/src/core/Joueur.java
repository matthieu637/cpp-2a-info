package core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

public class Joueur {

	private final String nom;
	private int solde_euros;
	private final Map<Action, Integer> solde_actions;
	private final List<Pair<Integer, Ordre>> operations;

	public Joueur(String nom) {
		super();
		this.nom = nom;
		this.solde_euros = Config.getInstance().SOLDE_EUROS_INIT;
		this.solde_actions = new HashMap<Action, Integer>();

		for (Action a : Action.values())
			this.solde_actions.put(a, Config.getInstance().SOLDE_ACTIONS_INIT);

		operations = new LinkedList<>();
	}

	public String getNom() {
		return nom;
	}

	public int getSolde_euros() {
		return solde_euros;
	}

	public void setSolde_euros(int solde_euros) {
		this.solde_euros = solde_euros;
	}

	public Map<Action, Integer> getSolde_actions() {
		return solde_actions;
	}

	public List<Pair<Integer, Ordre>> getOperationsOuvertes() {
		return operations;
	}

	public Ordre contientOperation(Integer i) {
		for (Pair<Integer, Ordre> p : operations)
			if (p.getLeft().equals(i))
				return p.getRight();

		return null;
	}

	public void retirerOperation(Integer i) {
		Iterator<Pair<Integer, Ordre>> it = operations.iterator();
		while (it.hasNext())
			if (it.next().getLeft().equals(i)) {
				it.remove();
				break;
			}

	}
}
