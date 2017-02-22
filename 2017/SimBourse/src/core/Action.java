package core;
import java.util.Arrays;


public enum Action {
	Apple("Apple"), Facebook("Facebook"), Trydea("Trydea"), Google("Google");

	private String name;
	

	private Action(String name) {
		this.name = name;
	}
	private static int nombreActions(){
		int compteur=0;
		for (Action a : Action.values())
			compteur++;
		return compteur;
	}
	public static boolean estValide(String s) {
		for(Action a : Action.values())
			if(a.name.equalsIgnoreCase(s))
				return true;
		return false;
	}

	public static Action from(String s) {
		for(Action a : Action.values())
			if(a.name.equalsIgnoreCase(s))
				return a;
		System.out.println("ERROR Action.from " + s);
		return null;
	}
	public static String[] nomActions() {
		String[] vectName= new String[nombreActions()];
		int compteur=0;
		for(Action a : Action.values()){
			vectName[compteur]=a.name;
			compteur++;
		}
		Arrays.sort(vectName);
		return vectName;
	}
}
