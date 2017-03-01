package core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.tuple.Pair;


public class Marche {
	private volatile boolean ouvert;
	private volatile boolean fini;
	private long debut;
	private Map<Action, Set<Ordre>> liste_achats;
	private Map<Action, Set<Ordre>> liste_ventes;
	private List<Joueur> liste_joueurs;
	private Set<Integer> liste_id_ordres;
	private Map<Action, TreeSet<Echange>> historiques;
	private final ReadWriteLock mutex_ordre = new ReentrantReadWriteLock();
	private final Lock mutex_ordre_read = mutex_ordre.readLock();
	private final Lock mutex_ordre_write = mutex_ordre.writeLock();
	private Thread timer = null;
	private List<Operation> liste_Operations;
	private long echange_unique = 0;
	private long ordre_unique = 0;
	private final int initial_euros;
	
	public Marche() {
		ouvert = false;
		fini = false;
		liste_achats = new HashMap<>();
		liste_ventes = new HashMap<>();
		historiques = new HashMap<>();
		liste_Operations = new LinkedList<Operation>();
		for (Action a : Action.values()) {
			liste_achats.put(a, new TreeSet<Ordre>());
			liste_ventes.put(a, new TreeSet<Ordre>());
			historiques.put(a, new TreeSet<Echange>());
		}
		liste_joueurs = new LinkedList<>();
		liste_id_ordres = new TreeSet<>();
		
		Random r = new Random();
		int pow = Config.getInstance().POW_EURO_INIT_MIN + 
				r.nextInt(Config.getInstance().POW_EURO_INIT_MAX - Config.getInstance().POW_EURO_INIT_MIN + 1);
		initial_euros = (int) Math.pow(10, pow);
		
		if(Config.getInstance().BANQUE){
			Joueur banque = creer_joueur("banque");
			banque.setSolde_euros(Integer.MAX_VALUE);
			int max_action_en_jeu = Config.getInstance().SOLDE_ACTIONS_INIT*Action.values().length*100;//100 joueurs
			for(Action a : Action.values()){
				banque.getSolde_actions().put(a, Integer.MAX_VALUE);
				//prix plus à jour
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

	// synchronized pour les rares appels de gestion de joueur
	public synchronized Joueur creer_joueur(String nom) {
		Joueur j = new Joueur(nom, initial_euros);
		liste_joueurs.add(j);
		return j;
	}

	public synchronized boolean nom_possible(String nom) {
		for (Joueur j : liste_joueurs)
			if (j.getNom().equalsIgnoreCase(nom))
				return false;
		return true;
	}

	//synchronized est moins efficace car il bloque les lectures concurrentes
	public String getListeAchatsString(Action a, int nbMaxAchats) {
		mutex_ordre_read.lock();
		String r;
		if(nbMaxAchats<=0 || nbMaxAchats>=liste_achats.get(a).size())
			r = String.valueOf(liste_achats.get(a));
		else{
			LinkedList<Ordre> list = new LinkedList<Ordre>();
			final Iterator<Ordre> i = liste_achats.get(a).iterator();
			for (int j=0; j<nbMaxAchats && i.hasNext();j++)
				list.add(i.next());
			r=String.valueOf(list);
		}
		mutex_ordre_read.unlock();
		return r;
	}

	public String getListeVentesString(Action a, int nbMaxVentes) {
		mutex_ordre_read.lock();
		String r;
		if(nbMaxVentes<=0 || nbMaxVentes>liste_ventes.get(a).size())
			r = String.valueOf(liste_ventes.get(a));
		else{
			LinkedList<Ordre> list = new LinkedList<Ordre>();
			final Iterator<Ordre> i = liste_ventes.get(a).iterator();
			for (int j=0; j<nbMaxVentes && i.hasNext();j++)
				list.add(i.next());
			r=String.valueOf(list);
		}
		mutex_ordre_read.unlock();
		return r;
	}
	
	/**
	 * @param a : le nom de l'action
	 * @param n : le numéro de liste à partir duquel il faut envoyer les éléments historiques du serveur au client
	 * @return  : retourne une liste chainée contenant les éléments de 'historiques' voulus
	 */
	public LinkedList<Echange> getHistoriqueEchanges(Action a, int n) {
		mutex_ordre_read.lock();
		int tailleH=historiques.get(a).size(); //Pour calculer une seule fois la taille de la liste
		LinkedList<Echange> list = new LinkedList<Echange>();//on instancie la liste chainée que l'on va remplir
		
		//on crée un iterateur parcourant 'historiques' dans le sens décroissant
		final Iterator<Echange> i = historiques.get(a).descendingIterator();
		for (int j=0; j<tailleH-n && i.hasNext();j++)
			//on ajoute dans la liste chainée les éléments au sens décroissants de 'historiques'
			//au début de la liste chainée à chaque fois pour que les éléments apparaissent dans le bon sens
			list.addFirst(i.next());
		
		mutex_ordre_read.unlock();
		//on retourne la liste chainée
		return list;
	}

	public int achat(Joueur joueur_achat, Action a, float prix_achat, int volume_achat) {
		if (volume_achat <= 0.0)
			return -5;
		if (prix_achat <= 0.0)
			return -6;

		mutex_ordre_write.lock();
		int argent_joueur= joueur_achat.getSolde_euros();
		int argent_engage = (int) (volume_achat * prix_achat);
		if (argent_joueur < argent_engage) {
			mutex_ordre_write.unlock();
			return -7;
		}

		if (!joueur_achat.getNom().equals("banque"))
			liste_Operations.add(new Operation("Achat",joueur_achat.getNom(),a.toString(),prix_achat,volume_achat));
		
		Iterator<Ordre> it = liste_ventes.get(a).iterator();

		while (it.hasNext() && volume_achat > 0) {
			Ordre vente = it.next();
			Joueur joueur_vente = vente.getJoueur();

			if (prix_achat >= vente.prix) {
				Echange e = new Echange(joueur_vente, joueur_achat, vente.prix, Math.min(vente.volume, volume_achat), echange_unique++);
				historiques.get(a).add(e);

				// l'achat est complete
				if (volume_achat <= vente.volume) {
					int volume_vendu = volume_achat;

					joueur_vente.setSolde_euros(joueur_vente.getSolde_euros() + (int) (vente.prix * volume_vendu));
					joueur_achat.setSolde_euros(joueur_achat.getSolde_euros() - (int) (vente.prix * volume_vendu));
					joueur_achat.getSolde_actions().put(a, joueur_achat.getSolde_actions().get(a) + volume_vendu);
					vente.setVolume(vente.getVolume() - volume_vendu);
					
					//ordre restant vide
					if(vente.getVolume() == 0){
						Integer id_vente = vente.getId_ordre();
						joueur_vente.retirerOperation(id_vente);
						it.remove();
					}

					mutex_ordre_write.unlock();
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
		Ordre achat = new Achat(id, a, prix_achat, volume_achat, joueur_achat, ordre_unique++);
		liste_achats.get(a).add(achat);
		joueur_achat.getOperationsOuvertes().add(Pair.of(id, achat));
		mutex_ordre_write.unlock();
		return id;
	}

	public int vend(Joueur joueur_vente, Action a, float prix_vente, int volume_vente) {
		if (volume_vente <= 0.0)
			return -8;
		if (prix_vente <= 0.0)
			return -9;

		mutex_ordre_write.lock();
		int volume_joueur = joueur_vente.getSolde_actions().get(a);
		if (volume_joueur < volume_vente) {
			mutex_ordre_write.unlock();
			return -10;
		}

		if (!joueur_vente.getNom().equals("banque"))
			liste_Operations.add(new Operation("Vente",joueur_vente.getNom(),a.toString(),prix_vente,volume_vente));
		
		joueur_vente.getSolde_actions().put(a, volume_joueur - volume_vente);
		Iterator<Ordre> it = liste_achats.get(a).iterator();

		while (it.hasNext() && volume_vente > 0) {
			Achat achat = (Achat) it.next();
			Joueur joueur_achat = achat.getJoueur();

			if (prix_vente <= achat.prix) {
				Echange e = new Echange(joueur_vente, joueur_achat, achat.prix, Math.min(achat.volume, volume_vente), echange_unique++);
				historiques.get(a).add(e);

				// la vente est complete
				if (volume_vente <= achat.volume) {
					int volume_vendu = volume_vente;
					joueur_vente.setSolde_euros(joueur_vente.getSolde_euros() + (int) (achat.prix * volume_vendu));
					joueur_achat.getSolde_actions().put(a, joueur_achat.getSolde_actions().get(a) + volume_vendu);
					// achat.setArgent_paye(achat.getArgent_paye() + (int) (achat.prix * volume_vendu));
					achat.setVolume(achat.getVolume() - volume_vente);

					//ordre restant vide
					if(achat.getVolume() == 0){
						Integer id_achat = achat.getId_ordre();
						joueur_achat.retirerOperation(id_achat);
						it.remove();
					}
					
					mutex_ordre_write.unlock();
					return 0;
				}
				// volume_vente > vente.volume

				int volume_vendu = achat.volume;
				joueur_vente.setSolde_euros(joueur_vente.getSolde_euros() + (int) (achat.prix * volume_vendu));
				joueur_achat.getSolde_actions().put(a, joueur_achat.getSolde_actions().get(a) + volume_vendu);
				// achat.setArgent_paye(achat.getArgent_paye() + (int) (achat.prix * volume_vendu));
				achat.setVolume(achat.getVolume() - volume_vente);

				// remove
				Integer id_achat = achat.getId_ordre();
				joueur_achat.retirerOperation(id_achat);
				it.remove();

				volume_vente -= volume_vendu;
			} else
				break;
		}

		int id = creer_id_ordre();
		Ordre vente = new Vente(id, a, prix_vente, volume_vente, joueur_vente, ordre_unique++);
		liste_ventes.get(a).add(vente);
		joueur_vente.getOperationsOuvertes().add(Pair.of(id, vente));
		mutex_ordre_write.unlock();

		return id;
	}

	public int suivre(Joueur joueur, Integer ordre) {
		mutex_ordre_read.lock();
		Ordre o = joueur.contientOperation(ordre);
		if (o == null) {
			mutex_ordre_read.unlock();
			return 0;
		}

		int vol = o.getVolume();
		mutex_ordre_read.unlock();
		return vol;
	}

	public int annuler(Joueur joueur, int ordre_id) {
		mutex_ordre_write.lock();
		Ordre o = joueur.contientOperation(ordre_id);
		if (o == null) {
			mutex_ordre_write.unlock();
			return -11;
		}

		if (o instanceof Achat) {
			if (!joueur.getNom().equals("banque"))
				liste_Operations.add(new Operation("AnnulationAchat",joueur.getNom(),o.action.toString(),o.prix,o.volume));
			liste_achats.get(o.action).remove(o);
			joueur.retirerOperation(ordre_id);

			int argent_recupere = (int) (o.prix * o.volume);
			if (argent_recupere > 0)
				joueur.setSolde_euros(joueur.getSolde_euros() + argent_recupere);

			mutex_ordre_write.unlock();
			return argent_recupere;
		} else {
			if (!joueur.getNom().equals("banque"))
				liste_Operations.add(new Operation("AnnulationVente",joueur.getNom(),o.action.toString(),o.prix,o.volume));
			liste_ventes.get(o.action).remove(o);
			joueur.retirerOperation(ordre_id);
			int nb_action = joueur.getSolde_actions().get(o.action);
			joueur.getSolde_actions().put(o.action, nb_action + o.volume);

			mutex_ordre_write.unlock();
			return o.volume;
		}
	}

	//appel déjà protégé, synchronized inutile
	private int creer_id_ordre() {
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
			sb.append(",'classement':");
			synchronized(this){
				Collections.sort(liste_joueurs);
			}
			String ljs = getListeJoueursString();
			sb.append(ljs);
		}
		sb.append("}");

		return new String(sb);
	}
	
	public String getListeOperationsString(){
		mutex_ordre_read.lock();
		String r = String.valueOf(liste_Operations);
		mutex_ordre_read.unlock();
		return r;
	}
	
	//synchronized non nécessaire seul le créateur l'utilise
	// ou lors de fin(), la liste ne change pas
	public String getListeJoueursString(){
		StringBuffer sb = new StringBuffer(liste_joueurs.size() * 100);
		sb.append("[");
		for(Joueur j: liste_joueurs )
			if(!j.getNom().equals("banque")){
				sb.append("'");
				sb.append(j.getNom());
				sb.append("',");
			}
		sb.deleteCharAt(sb.length()-1);//Pour retirer le dernier ","
		sb.append("]");
		
		return new String(sb);
	}

	@Override
	public String toString() {
		return "Marche [ouvert=" + ouvert + ", fini=" + fini + ", debut=" + debut + ", liste_achats=" + liste_achats
				+ ", liste_ventes=" + liste_ventes + ", liste_joueurs=" + liste_joueurs + ", liste_id_ordres="
				+ liste_id_ordres + ", historiques=" + historiques + ", mutex=" + mutex_ordre + "]";
	}

	public void destroy(){
		//un seul appel
		//lock nécessaire pour être sur que les opérations sont protégées
		mutex_ordre_write.lock();
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(Config.getInstance().CHEMIN_FICHIER, true));
			writer.write(String.valueOf(liste_Operations));
			writer.write("\r\n");
			
			writer.close();
		}catch (Exception e){
	         e.printStackTrace();
	    }
		if(timer != null){
			timer.interrupt();
		}
		mutex_ordre_write.unlock();
	}
}
