package core;

public class Achat extends Ordre {

	public Achat(int id_ordre, Action action, float prix, int volume, Joueur joueur) {
		super(id_ordre, action, prix, volume, joueur);
	}

	@Override
	public int compareTo(Ordre o) {
		return Float.compare(o.prix, this.prix);
	}
}
