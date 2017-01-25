package core;

public abstract class Ordre {
	protected int id_ordre;
	protected Action action;
	protected float prix;
	protected int volume;
	protected Joueur joueur;
	
	public Ordre(int id_ordre, Action action, float prix, int volume, Joueur joueur) {
		super();
		this.id_ordre = id_ordre;
		this.action = action;
		this.prix = prix;
		this.volume = volume;
		this.joueur = joueur;
	}
	
	public int getId_ordre() {
		return id_ordre;
	}
	public void setId_ordre(int id_ordre) {
		this.id_ordre = id_ordre;
	}
	public Action getAction() {
		return action;
	}
	public void setAction(Action action) {
		this.action = action;
	}
	public float getPrix() {
		return prix;
	}
	public void setPrix(float prix) {
		this.prix = prix;
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
	public void setJoueur(Joueur joueur) {
		this.joueur = joueur;
	}

}
