package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import astart.ProjectStarter;

public class ServerResultThread extends IServerThread {
	
	public ServerResultThread(Socket socket) {
		super(socket);
	}

	private volatile String outputString;

	public void run() {

		try {
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(
							socket.getInputStream()));

			out.println("jP300 - ver "+ProjectStarter.VERSION);

			while(isRunning){
				System.out.println("[ServerResultThread] goin to sleep :]");
				synchronized(this){
					this.wait() ;
				}
				out.println(outputString);
			}
			out.close();
			in.close();
			socket.close();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public String getOutputString() {
		return outputString;
	}

	public void setOutputString(String outputString) {
		this.outputString = outputString;
	}
}
