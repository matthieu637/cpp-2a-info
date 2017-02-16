package core;

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
	
	private int nb_action_total() {
		int nb_action_tot = 0;
		for (Action a : Action.values())
				nb_action_tot+=solde_actions.get(a);

		return nb_action_tot;
	}

	@Override
	public int compareTo(Joueur o) {
		int j1_nb_ac = nb_action_unique();
		int j2_nb_ac = o.nb_action_unique();
		
		int j1_tot_ac = nb_action_total();
		int j2_tot_ac = o.nb_action_total();
		

		if (j1_nb_ac == 2 && j2_nb_ac == 2) {// meilleur cas
			int cmp = -Integer.compare(j1_tot_ac, j2_tot_ac);
			if(cmp == 0)
				return -Integer.compare(this.solde_euros, o.solde_euros);
			return cmp;
		} 
		
		if (j1_nb_ac == 2 && j2_nb_ac != 2)// j1 gagne car il a vendu les 2 types
			return -1;
		
		if (j2_nb_ac == 2 && j1_nb_ac != 2)// j2 gagne car il a vendu les 2 types
			return 1;
		
		if (j2_nb_ac == 0 && j1_nb_ac == 0)// les 2 n'ont que de l'argent
			return -Integer.compare(this.solde_euros, o.solde_euros);
		
		if (j1_nb_ac != 4 && j2_nb_ac == 4)// j1 gagne car il a vendu au moins un type d action
			return -1;
		
		if (j1_nb_ac == 4 && j2_nb_ac != 4)// j2 gagne car il a vendu au moins un type d action
			return 1;
		
		if (j1_nb_ac > 0 && j2_nb_ac > 0){ // les 2 ont plusieurs actions (1/3/4)
			int cmp = -Integer.compare(j1_tot_ac, j2_tot_ac);
			if(cmp == 0)
				return -Integer.compare(this.solde_euros, o.solde_euros);
			return cmp;
		}
		
		System.out.println("cas non traite "+solde_actions+" "+o.solde_actions);
		return 0;
	}
}
