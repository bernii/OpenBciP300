package client;

import java.io.*;
import java.net.*;

public class SimpleClient {
	public static void main(String[] args) throws IOException {

		Socket kkSocket = null;
		PrintWriter out = null;
		BufferedReader in = null;


		try {
			kkSocket = new Socket("localhost", 4444);
			out = new PrintWriter(kkSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: localhost.");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: localhost.");
			System.exit(1);
		}

		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String fromServer;
		try{
		while ((fromServer = in.readLine()) != null) {
			System.out.println("[FromServer]: "+fromServer);
		}
		}catch(SocketException e){
			// server unexpectedly closed the connection
			System.out.println("[Client]: Server closed the connection !!");
		}
		
		out.close();
		in.close();
		stdIn.close();
		kkSocket.close();
		System.out.println("[Client]: closing all");
	}
}