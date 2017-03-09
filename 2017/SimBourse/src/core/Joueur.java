package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

public class Joueur implements Comparable<Joueur> {

	private final String nom;
	private final String nom_complet;
	private final int initiale_solde_euros;
	private int solde_euros;
	private final Map<Action, Integer> solde_actions;
	private final List<Pair<Integer, Ordre>> operations;

	public Joueur(String nom, int solde_euros, String nom_complet) {
		super();
		this.nom = nom;
		this.initiale_solde_euros = solde_euros;
		this.solde_euros = solde_euros;
		this.solde_actions = new HashMap<Action, Integer>();
		this.nom_complet = nom_complet;
		
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

	public int max2_actions() {
		ArrayList<Integer> ac = new ArrayList<>(solde_actions.values());
		Collections.sort(ac);
		
		return ac.get(ac.size()-1) + ac.get(ac.size()-2);
	}
	
	private boolean rend90pourcent(){
		return solde_euros >= (initiale_solde_euros * 0.90f) ;
	}

	@Override
	public int compareTo(Joueur o) {
		boolean j1_assez_argent = rend90pourcent();
		boolean j2_assez_argent = o.rend90pourcent();
		
		if(j1_assez_argent && !j2_assez_argent) //j1 gagne, j2 n'a plus assez d'argent
			return -1;
		else if(j2_assez_argent && !j1_assez_argent) //j2 gagne, j1 n'a plus assez d'argent
			return 1;
		else { // autre cas, on compare les actions
			int j1_nb_ac = max2_actions();
			int j2_nb_ac = o.max2_actions();
			
			int cmp = -Integer.compare(j1_nb_ac, j2_nb_ac);
			if(cmp == 0)
				return -Integer.compare(this.solde_euros, o.solde_euros);
			return cmp;
		}
	}

	public String getNomComplet() {
		return nom_complet;
	}
}
