package core;

public class Config {
	private static Config instance = new Config();

	public final int POW_EURO_INIT_MIN = 3;//euros min : 1000
	public final int POW_EURO_INIT_MAX = 6;//euros max : 1 000 000
	public final int SOLDE_ACTIONS_INIT = 1000;
	public final int PORT = 23456; //23456 ou 80
	public final int TEMPS_PARTIE = 5;//en minutes
	public final int PACKET_SIZE = 8;
	public final int MAX_PACKET_SIZE_INPUT = 128;
	public final int RESERVED_SIZE_SEND_PACKET = (int) (Math.pow(10, PACKET_SIZE)-1);
	public final boolean BANQUE = false;
	public final String CHEMIN_FICHIER="OperationsHistorique.txt";
	public final String VERSION = "1.10";
	
	private Config (){
		
	}
	
	public static Config getInstance() {
		return instance;
	}
}
