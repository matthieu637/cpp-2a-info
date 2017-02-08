package core;

public class Achat extends Ordre {

	private int argent_paye;

	public Achat(int id_ordre, Action action, float prix, int volume, Joueur joueur, int argent_paye) {
		super(id_ordre, action, prix, volume, joueur);
		this.argent_paye = argent_paye;
	}

	@Override
	public int compareTo(Ordre o) {
		int c = Float.compare(o.prix, this.prix);
		if (c == 0)
			return Long.compare(this.temps, o.temps);
		return c;
	}

	public int getArgent_paye() {
		return argent_paye;
	}

	public void setArgent_paye(int argent_paye) {
		this.argent_paye = argent_paye;
	}
}
