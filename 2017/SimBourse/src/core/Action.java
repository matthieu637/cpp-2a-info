package core;
import java.util.Arrays;


public enum Action {
	Apple("Apple"), Facebook("Facebook"), Trydea("Trydea"), Google("Google");

	private String name;
	

	private Action(String name) {
		this.name = name;
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
	public static int[] nomActions() {
		int n=Action.values().length;
		String[] vectName= new String[n];
		int[] vectPos= new int[n]; 
		for(int i=0;i<n;i++){
			vectName[i]=Action.values()[i].name;
		}
		Arrays.sort(vectName);
		
		for(int i=0;i<n;i++){
			for(int k=0;k<n;k++)
				if(vectName[k]==Action.values()[i].name){
					vectPos[k]=i;
					break;
				}
		}
		return vectPos;
	}
}
