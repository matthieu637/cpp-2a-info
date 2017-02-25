package core;

public class Echange implements Comparable<Echange> {
	private final Joueur vendeur;
	private final Joueur acheteur;
	private final float prix;
	private final float volume;
	private final long temps;

	public Echange(Joueur vendeur, Joueur acheteur, float prix, float volume, long temps) {
		super();
		this.vendeur = vendeur;
		this.acheteur = acheteur;
		this.prix = prix;
		this.volume = volume;
		this.temps = temps;
	}

	public Joueur getVendeur() {
		return vendeur;
	}

	public Joueur getAcheteur() {
		return acheteur;
	}

	public float getPrix() {
		return prix;
	}

	public float getVolume() {
		return volume;
	}

	public long getTemps() {
		return temps;
	}

	@Override
	public int compareTo(Echange o) {
		return Long.compare(this.temps, o.temps);
	}

	@Override
	public String toString() {
		return "('" + vendeur.getNom() + "','" + acheteur.getNom() + "'," + prix + "," + volume + ")";
	}
}
