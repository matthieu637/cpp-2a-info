package core;

public abstract class Ordre implements Comparable<Ordre> {
	protected final int id_ordre;
	protected final Action action;
	protected final float prix;
	protected int volume;
	protected final int volume_initial;
	protected final Joueur joueur;

	public Ordre(int id_ordre, Action action, float prix, int volume, Joueur joueur) {
		super();
		this.id_ordre = id_ordre;
		this.action = action;
		this.prix = prix;
		this.volume = volume;
		this.volume_initial = volume;
		this.joueur = joueur;
	}

	public int getId_ordre() {
		return id_ordre;
	}

	public Action getAction() {
		return action;
	}

	public float getPrix() {
		return prix;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public Joueur getJoueur() {
		return joueur;
	}
}
