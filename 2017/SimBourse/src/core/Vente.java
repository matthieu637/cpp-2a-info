package core;

public class Vente extends Ordre {

	public Vente(int id_ordre, Action action, float prix, int volume, Joueur joueur) {
		super(id_ordre, action, prix, volume, joueur);
	}

	@Override
	public int compareTo(Ordre o) {
		return Float.compare(this.prix, o.prix);
	}
}
