package server;

import result.IResultListener;
import astart.ProjectStarter;

public class ServerResult extends Server implements IResultListener {

	public ServerResult() {
		this.port = ProjectStarter.getConf().getValueInt("tcpIpServerPort") ;

		//this.port = ProjectStarter.getConf().getValueInt("tcpIpServerInputPort") ;
	}

	public void resultArrived(String txt) {
		// send data to clients
		System.out.println("[ServerResult] - sending "+txt+" to clients");
		for(IServerThread thread: serverThreads){
			((ServerResultThread) thread).setOutputString(txt);
			synchronized(thread){
				thread.notify() ;
			}
		}
	}

	@Override
	public int getType() {
		return Server.TYPE_RESULT;
	}
}
