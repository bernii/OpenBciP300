package server;

import java.net.Socket;

public abstract class IServerThread extends Thread{

	protected boolean isRunning = true ;
	protected Socket socket;

	public IServerThread(Socket socket){
		this.socket = socket ;
	}

	public boolean isRunning() {
		return isRunning ;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	
}
