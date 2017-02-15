package core;

public class Config {
	private static Config instance = new Config();

	public final int SOLDE_EUROS_INIT = 1000;
	public final int SOLDE_ACTIONS_INIT = 100;
	public final int PORT = 3080;
	public final int TEMPS_PARTIE = 1;//en minutes
	public final int PACKET_SIZE = 8;
	
	private Config (){
		
	}
	
	public static Config getInstance() {
		return instance;
	}
}
