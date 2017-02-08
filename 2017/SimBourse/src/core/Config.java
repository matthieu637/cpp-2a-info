package core;

public class Config {
	private static Config instance = new Config();

	public int SOLDE_EUROS_INIT = 1000;
	public int SOLDE_ACTIONS_INIT = 100;
	public int PORT = 3080;
	
	private Config (){
		
	}
	
	public static Config getInstance() {
		return instance;
	}
}
