package Serveur;

import java.net.Socket;

public class Partie {
	private boolean joined, started;
	private Socket client1, client2;

	public Partie() {
		this.joined = false;
		this.started = false;
		this.client1 = null;
		this.client2 = null;
	}

	public boolean isJoined() {
		return joined;
	}

	public void setJoined(boolean joined) {
		this.joined = joined;
	}
	
	public Socket getClient1() {
		return client1;
	}

	public void setClient1(Socket client1) {
		this.client1 = client1;
	}

	public Socket getClient2() {
		return client2;
	}

	public void setClient2(Socket client2) {
		this.client2 = client2;
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}
}
