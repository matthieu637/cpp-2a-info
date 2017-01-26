package network;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import core.Config;

public class ClientTest extends Thread {
	private BufferedOutputStream out;

	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		new ClientTest();
	}

	private ClientTest() throws UnknownHostException, IOException, InterruptedException {
		@SuppressWarnings("resource")
		Socket soc = new Socket("localhost", Config.getInstance().PORT);
		new ListenIn("ServeurTestor1 :", new BufferedInputStream(soc.getInputStream()));
		out = new BufferedOutputStream(soc.getOutputStream());

//		out.write("CREATE\n".getBytes());
//		out.flush();

		start();
	}

	public void run() {
		try {
			String Commande;
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

			System.out.println("Write your commande here :");

			while ((Commande = in.readLine()) != null) {
				if (Commande.equalsIgnoreCase("stop"))
					System.exit(1);

				Commande = Commande + "\n";

				out.write(Commande.getBytes());
				out.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

class ListenIn extends Thread {
	private BufferedInputStream in;
	private String name;

	public ListenIn(String name, BufferedInputStream in) {
		this.name = name;
		this.in = in;
		start();
	}

	public void run() {
		try {
			int i = 0;
			while (true) {
				byte b = (byte) in.read();
				byte r[] = new byte[in.available()];
//				System.out.println(b);
				in.read(r, 0, r.length);

//				DEBUG NETWORK
//				String str = " -- " + String.valueOf(b) + " -- ";
//
//				for (short j = 0; j < r.length; j++)
//					str += r[j] + " ";

				System.out.println(name + ((char)b)+new String(r));
				i++;
				if (i > 5)
					Thread.sleep(1000);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
		}
	}
}
