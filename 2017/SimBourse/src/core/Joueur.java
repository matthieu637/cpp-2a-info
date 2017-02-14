package core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

public class Joueur implements Comparable<Joueur> {

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

	private int nb_action_unique() {
		int nb_action_type = 0;
		for (Action a : Action.values())
			if (solde_actions.get(a) > 0)
				nb_action_type++;

		return nb_action_type;
	}

	@Override
	public int compareTo(Joueur o) {
		int j1_nb_ac = nb_action_unique();
		int j2_nb_ac = o.nb_action_unique();

		if (j1_nb_ac == 1 && j2_nb_ac == 1) {// cas normal
			int j1_max_ac = Collections.max(solde_actions.values());
			int j2_max_ac = Collections.max(o.solde_actions.values());
			return -Integer.compare(j1_max_ac, j2_max_ac);
		} else if (j1_nb_ac > 0 && j2_nb_ac == 0)// j1 gagne car j2 n'a plus que de l'argent
			return -1;
		else if (j2_nb_ac > 0 && j1_nb_ac == 0)// j2 gagne car j1 n'a plus que de l'argent
			return 1;
		else if (j2_nb_ac == 0 && j1_nb_ac == 0)// les 2 n'ont que de l'argent
			return -Integer.compare(this.solde_euros, o.solde_euros);
		else if (j1_nb_ac > 0 && j2_nb_ac > 0){ // les 2 ont plusieurs actions
			int j1_max_ac = Collections.max(solde_actions.values());
			int j2_max_ac = Collections.max(o.solde_actions.values());
			return -Integer.compare(j1_max_ac, j2_max_ac);
		}
		
		System.out.println("cas non traite "+solde_actions+" "+o.solde_actions);
		return 0;
	}
}
