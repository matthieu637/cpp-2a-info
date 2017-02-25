package core;

public class Vente extends Ordre {

	public Vente(int id_ordre, Action action, float prix, int volume, Joueur joueur, long vente) {
		super(id_ordre, action, prix, volume, joueur, vente);
	}

	@Override
	public int compareTo(Ordre o) {
		int c = Float.compare(this.prix, o.prix);
		if(c == 0)
			return Long.compare(this.temps, o.temps);
		return c;
	}
}
