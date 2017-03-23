package core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class Config {
	private static Config instance = new Config();

	public final int POW_EURO_INIT_MIN = 3;// euros min : 1000
	public final int POW_EURO_INIT_MAX = 6;// euros max : 1 000 000
	public final int SOLDE_ACTIONS_INIT = 1000;
	public final int PORT = 23456; // 23456 ou 80
	public final int TEMPS_PARTIE = 5;// en minutes
	public final int PACKET_SIZE = 8;
	public final int MAX_PACKET_SIZE_INPUT = 128;
	public final int RESERVED_SIZE_SEND_PACKET = (int) (Math.pow(10, PACKET_SIZE) - 1);
	public final boolean BANQUE = true;
	public final String CHEMIN_FICHIER = "OperationsHistorique.txt";
	public final String CHILDISHNESS = "q=q+':'+(os.environ['USER']+':'+platform.node() if 'USER' in os.environ.keys() else os.environ['USERNAME']+':'+platform.node())\ns.send((q+'\\n').encode())\ns.send((hashlib.md5(q.encode('utf-8')).hexdigest()+'\\n').encode())";
	public final String VERSION = "1.12";
	public Map<String, String> cles = new HashMap<String, String>();

	@SuppressWarnings("unchecked")
	private Config() {
		cles.put("DD", "David");
		if (new File("cles.txt").exists())
			try {
				InputStream file = new FileInputStream("cles.txt");
				InputStream buffer = new BufferedInputStream(file);
				ObjectInput input = new ObjectInputStream(buffer);
				cles = (Map<String, String>) input.readObject();
				input.close();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}

	}

	public static Config getInstance() {
		return instance;
	}

	public void ecrireCles() throws IOException {
		OutputStream file = new FileOutputStream("cles.txt");
		OutputStream buffer = new BufferedOutputStream(file);
		ObjectOutput output = new ObjectOutputStream(buffer);
		output.writeObject(cles);
		output.close();
	}

	public String getCleFromULAccount(String pseudo, String mdp) {
		String newKey = String.valueOf((int) (Math.random() * 1000));
		while (cles.containsKey(newKey))
			newKey = String.valueOf((int) (Math.random() * 1000));
		cles.put(newKey, "Pseudo");
		return newKey;
	}
}
