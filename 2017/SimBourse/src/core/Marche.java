package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.tuple.Pair;



public class Marche {
	private boolean ouvert;
	private boolean fini;
	private long debut;
	private Map<Action, Set<Ordre>> liste_achats;
	private Map<Action, Set<Ordre>> liste_ventes;
	private List<Joueur> liste_joueurs;
	private Set<Integer> liste_id_ordres;
	private Map<Action, Set<Echange>> historiques;
	private final Lock mutex = new ReentrantLock();
	private Thread timer = null;

	public Marche() {
		ouvert = false;
		fini = false;
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
		
		if(Config.getInstance().BANQUE){
			Joueur banque = creer_joueur("banque");
			banque.setSolde_euros(Integer.MAX_VALUE);
			int max_action_en_jeu = Config.getInstance().SOLDE_ACTIONS_INIT*Action.values().length*100;//100 joueurs
			for(Action a : Action.values()){
				banque.getSolde_actions().put(a, Integer.MAX_VALUE);
				achat(banque, a, 0.25f, max_action_en_jeu);
				vend(banque, a, 25.0f, max_action_en_jeu);
			}
		}
	}

	public boolean est_ouvert() {
		return ouvert;
	}

	public void commence() {
		ouvert = true;
		debut = System.currentTimeMillis();

		timer = new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000 * 60 * Config.getInstance().TEMPS_PARTIE);
				} catch (InterruptedException e) {
					//might be interrupted if creator leaves
					//don't print error in this case
				}
				fini = true;
				try {
					Thread.sleep(1000 * 60); // sleep 1 s to be sure fini is taking into account
				} catch (InterruptedException e) {
					//might be interrupted if creator leaves
					//don't print error in this case
				}
				//fermer tous les ordres
				for(Joueur joueur : liste_joueurs){
					List<Integer> ordre_supprimer = new LinkedList<>();
					for(Pair<Integer, Ordre> t : joueur.getOperationsOuvertes()){
						ordre_supprimer.add(t.getLeft());
					}
					for(Integer i : ordre_supprimer){
						annuler(joueur, i);
					}
				}
			}
		};
		timer.start();
	}

	public boolean est_fini() {
		return fini;
	}

	public synchronized Joueur creer_joueur(String nom) {
		Joueur j = new Joueur(nom);
		liste_joueurs.add(j);
		return j;
	}

	public synchronized boolean nom_possible(String nom) {
		for (Joueur j : liste_joueurs)
			if (j.getNom().equalsIgnoreCase(nom))
				return false;
		return true;
	}

	public Set<Ordre> getListeAchats(Action a) {
		return liste_achats.get(a);
	}

	public Set<Ordre> getListeVentes(Action a) {
		return liste_ventes.get(a);
	}

	public Set<Echange> getHistoriqueEchanges(Action a,int n) {
		List<Echange> list = new ArrayList<>();
		final Iterator<Echange> i = historiques.get(a).iterator();
		for(int j=0;j<n;j++)
			i.next();
		for (int j=n; j<historiques.get(a).size() && i.hasNext();j++)
			list.add(i.next());
		//historiques.get(a)
		Set<Echange> retour= new LinkedHashSet<>(list);
		
		return retour;
	}

	public int achat(Joueur joueur_achat, Action a, float prix_achat, int volume_achat) {
		if (volume_achat <= 0.0)
			return -5;
		if (prix_achat <= 0.0)
			return -6;

		mutex.lock();
		int argent_joueur = joueur_achat.getSolde_euros();
		int argent_engage = (int) (volume_achat * prix_achat);
		if (argent_joueur < argent_engage) {
			mutex.unlock();
			return -7;
		}

		Iterator<Ordre> it = liste_ventes.get(a).iterator();

		while (it.hasNext() && volume_achat > 0) {
			Ordre vente = it.next();
			Joueur joueur_vente = vente.getJoueur();

			if (prix_achat >= vente.prix) {
				Echange e = new Echange(joueur_vente, joueur_achat, vente.prix, Math.min(vente.volume, volume_achat));
				historiques.get(a).add(e);

				// l'achat est complete
				if (volume_achat <= vente.volume) {
					int volume_vendu = volume_achat;

					joueur_vente.setSolde_euros(joueur_vente.getSolde_euros() + (int) (vente.prix * volume_vendu));
					joueur_achat.setSolde_euros(joueur_achat.getSolde_euros() - (int) (vente.prix * volume_vendu));
					joueur_achat.getSolde_actions().put(a, joueur_achat.getSolde_actions().get(a) + volume_vendu);
					vente.setVolume(vente.getVolume() - volume_vendu);

					mutex.unlock();
					return 0;
				}
				// volume_achat > vente.volume

				int volume_vendu = vente.volume;

				joueur_vente.setSolde_euros(joueur_vente.getSolde_euros() + (int) (vente.prix * volume_vendu));
				joueur_achat.setSolde_euros(joueur_achat.getSolde_euros() - (int) (vente.prix * volume_vendu));
				joueur_achat.getSolde_actions().put(a, joueur_achat.getSolde_actions().get(a) + volume_vendu);
				vente.setVolume(vente.getVolume() - volume_vendu);

				// remove
				Integer id_vente = vente.getId_ordre();
				joueur_vente.retirerOperation(id_vente);
				it.remove();

				volume_achat -= volume_vendu;
			} else
				break;
		}

		// provisionne le reste
		joueur_achat.setSolde_euros(joueur_achat.getSolde_euros() - (int) (prix_achat * volume_achat));

		int id = creer_id_ordre();
		Ordre achat = new Achat(id, a, prix_achat, volume_achat, joueur_achat);
		liste_achats.get(a).add(achat);
		joueur_achat.getOperationsOuvertes().add(Pair.of(id, achat));
		mutex.unlock();
		return id;
	}

	public Object vend(Joueur joueur_vente, Action a, float prix_vente, int volume_vente) {
		if (volume_vente <= 0.0)
			return -8;
		if (prix_vente <= 0.0)
			return -9;

		mutex.lock();
		int volume_joueur = joueur_vente.getSolde_actions().get(a);
		if (volume_joueur < volume_vente) {
			mutex.unlock();
			return -10;
		}

		joueur_vente.getSolde_actions().put(a, volume_joueur - volume_vente);
		Iterator<Ordre> it = liste_achats.get(a).iterator();

		while (it.hasNext() && volume_vente > 0) {
			Achat achat = (Achat) it.next();
			Joueur joueur_achat = achat.getJoueur();

			if (prix_vente <= achat.prix) {
				Echange e = new Echange(joueur_vente, joueur_achat, achat.prix, Math.min(achat.volume, volume_vente));
				historiques.get(a).add(e);

				// la vente est complete
				if (volume_vente <= achat.volume) {
					int volume_vendu = volume_vente;
					joueur_vente.setSolde_euros(joueur_vente.getSolde_euros() + (int) (achat.prix * volume_vendu));
					joueur_achat.getSolde_actions().put(a, joueur_achat.getSolde_actions().get(a) + volume_vendu);
					// achat.setArgent_paye(achat.getArgent_paye() + (int) (achat.prix * volume_vendu));
					achat.setVolume(achat.getVolume() - volume_vente);

					mutex.unlock();
					return 0;
				}
				// volume_vente > vente.volume

				int volume_vendu = achat.volume;
				joueur_vente.setSolde_euros(joueur_vente.getSolde_euros() + (int) (achat.prix * volume_vendu));
				joueur_achat.getSolde_actions().put(a, joueur_achat.getSolde_actions().get(a) + volume_vendu);
				// achat.setArgent_paye(achat.getArgent_paye() + (int) (achat.prix * volume_vendu));
				achat.setVolume(achat.getVolume() - volume_vente);

				// remove
				Integer id_vente = achat.getId_ordre();
				joueur_achat.retirerOperation(id_vente);
				it.remove();

				volume_vente -= volume_vendu;
			} else
				break;
		}

		int id = creer_id_ordre();
		Ordre vente = new Vente(id, a, prix_vente, volume_vente, joueur_vente);
		liste_ventes.get(a).add(vente);
		joueur_vente.getOperationsOuvertes().add(Pair.of(id, vente));
		mutex.unlock();

		return id;
	}

	public int suivre(Joueur joueur, Integer ordre) {
		mutex.lock();
		Ordre o = joueur.contientOperation(ordre);
		if (o == null) {
			mutex.unlock();
			return 0;
		}

		int vol = o.getVolume();
		mutex.unlock();
		return vol;
	}

	public int annuler(Joueur joueur, int ordre_id) {
		mutex.lock();
		Ordre o = joueur.contientOperation(ordre_id);
		if (o == null) {
			mutex.unlock();
			return -11;
		}

		if (o instanceof Achat) {
			liste_achats.remove(o);
			joueur.retirerOperation(ordre_id);

			int argent_recupere = (int) (o.prix * o.volume);
			if (argent_recupere > 0)
				joueur.setSolde_euros(joueur.getSolde_euros() + argent_recupere);

			mutex.unlock();
			return argent_recupere;
		} else {
			liste_ventes.remove(o);
			joueur.retirerOperation(ordre_id);
			int nb_action = joueur.getSolde_actions().get(o.action);
			joueur.getSolde_actions().put(o.action, nb_action + o.volume);

			mutex.unlock();
			return o.volume;
		}
	}

	private synchronized int creer_id_ordre() {
		int numero_partie = (int) (Math.random() * 100000000);
		while (liste_id_ordres.contains((Integer) numero_partie))
			numero_partie = (int) (Math.random() * 100000000);
		liste_id_ordres.add(numero_partie);
		return numero_partie;
	}

	public synchronized void retirer_joueur(Joueur joueur) {
		List<Integer> ordre_supprimer = new LinkedList<>();
		for(Pair<Integer, Ordre> t : joueur.getOperationsOuvertes()){
			ordre_supprimer.add(t.getLeft());
		}
		for(Integer i : ordre_supprimer){
			annuler(joueur, i);
		}
		liste_joueurs.remove(joueur);
	}

	public String fin() {
		StringBuffer sb = new StringBuffer(liste_joueurs.size() * 100);
		sb.append("{'temps':");
		long secondes = ((debut + Config.getInstance().TEMPS_PARTIE * 60 * 1000) - System.currentTimeMillis()) / 1000;
		if (secondes < 0)
			secondes = 0;
		sb.append(String.valueOf(secondes));
		if (fini) {
			sb.append(",'classement':[");
			Collections.sort(liste_joueurs);
			for(Joueur j : liste_joueurs){
				sb.append("'");
				sb.append(j.getNom());
				sb.append("',");
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append("]");
		}
		sb.append("}");

		return new String(sb);
	}

	@Override
	public String toString() {
		return "Marche [ouvert=" + ouvert + ", fini=" + fini + ", debut=" + debut + ", liste_achats=" + liste_achats
				+ ", liste_ventes=" + liste_ventes + ", liste_joueurs=" + liste_joueurs + ", liste_id_ordres="
				+ liste_id_ordres + ", historiques=" + historiques + ", mutex=" + mutex + "]";
	}

	public void destroy(){
		if(timer != null){
			timer.interrupt();
		}
	}
}
