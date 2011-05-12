package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

import javolution.util.FastTable;

/**
 * Starts server that sends classification result to remote clients (TYPE_RESULT) or server
 * that listens for clients that will send matrix configuration updates (TYPE_MATRIX_CHANGER)
 * @author berni
 *
 */
public abstract class Server implements Runnable  {

	public static final int TYPE_RESULT = 0;
	public static final int TYPE_MATRIX_CHANGER = 1;
	
	FastTable<IServerThread> serverThreads = new FastTable<IServerThread>() ;
	private boolean isRunning = true ;
	
	public Server(){
	}
	
	public abstract int getType() ;
	

	public void stopRecording() {
		// turn off the server
		System.out.println("[Server] stopping server "+getType());
		isRunning = false ;
		try {
			if(serverSocket!=null&&!serverSocket.isClosed())
				serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	ServerSocket serverSocket = null;
	/*
	 * Run server on port 4444
	 */
	protected int port = 4444 ;
	public void run() {

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Could not listen ["+getType()+"]on port: "+port);
            System.exit(-1);
        }
		while(isRunning){
			IServerThread thread;
			try {
				if(getType()==TYPE_RESULT)
					thread = new ServerResultThread(serverSocket.accept());
				else
					thread = new ServerMatrixChangerThread(serverSocket.accept());
				thread.start() ;
				serverThreads.add(thread);
			}catch (SocketException e) {
				System.out.println("[Server] - socket closed! "+getType());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		for(IServerThread thread: serverThreads){
			thread.setRunning(false);
			synchronized(thread){
				thread.notify() ;
			}
		}
	}

}
