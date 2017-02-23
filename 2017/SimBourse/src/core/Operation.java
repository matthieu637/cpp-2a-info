package core;

public class Operation {
	private String nomOperation;
	private String nomJoueur;
	private String nomAction;
	private Float prixAction;
	private Integer volumeAchat;
	
	public Operation(String nomO,String nomJ, String nomA, Float prixA, Integer volA){
		nomOperation=nomO;
		nomJoueur=nomJ;
		nomAction=nomA;
		prixAction=prixA;
		volumeAchat=volA;
	}
	@Override
	public String toString() {
		return "['"+nomOperation+"','"+nomJoueur+"','"+nomAction+"',"+Float.toString(prixAction)+","+Integer.toString(volumeAchat)+"]";
	}
	
}
