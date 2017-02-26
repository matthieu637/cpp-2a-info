package core;

public class Achat extends Ordre {
	public Achat(int id_ordre, Action action, float prix, int volume, Joueur joueur, long temps) {
		super(id_ordre, action, prix, volume, joueur, temps);
	}

	@Override
	public int compareTo(Ordre o) {
		int c = Float.compare(o.prix, this.prix);
		if (c == 0)
			return Long.compare(this.temps, o.temps);
		return c;
	}
}
