package display;

import general.ITimeManagerListener;

import java.util.LinkedList;

import javolution.util.FastTable;

import org.eclipse.swt.graphics.Point;

import result.IResultListener;
import server.Server;
import server.ServerMatrixChanger;
import server.ServerResult;

import configuration.IConfigurationListener;

import astart.ProjectStarter;

public class DisplayUpdater implements Runnable , ITimeManagerListener, IConfigurationListener  {

	private boolean runState = false ;
	LinkedList<IDisplayUpdaterListener> listeners = new LinkedList<IDisplayUpdaterListener>();
	private int actionType;
	
	public void addListener(IDisplayUpdaterListener listener){
		listeners.add(listener);
	}
	
	public void updateDisplayMatrix(FastTable<FastTable<String>> characters) {
		String[][] txt = new String[characters.size()][];
		for(int i=0;i<txt.length;i++){
			txt[i] = new String[characters.get(i).size()];
			for(int a=0;a<characters.get(i).size();a++){
				txt[i][a] = characters.get(i).get(a) ;
			}
		}
		updateDisplayMatrix(txt, new Point(txt[0].length,txt.length));
	}
	
	public void updateDisplayMatrix(String [][] txt, Point dimensions){
		for(IDisplayUpdaterListener listener: listeners)
			listener.updateMatrix(txt) ;
		ProjectStarter.getInstance().getSignalAccumulator().update(new int[]{dimensions.y,dimensions.x}) ;
		((MatrixResponsesCanvas) ProjectStarter.getControlPanel().getCanvasMatrixResponses()).update(dimensions);
	}
	
	public void run() {
		runState = true ;
		while(runState){
			
			synchronized(this){
				try {
					this.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			for(IDisplayUpdaterListener listener: listeners)
				listener.displayUpdaterExecute(actionType) ;
		}
	}

	public synchronized void timeManagerExecute(int actionType, long timeDiff) {
		this.actionType = actionType;
		this.notify();
	}

	public boolean isRunState() {
		return runState;
	}

	public void setRunState(boolean runState) {
		if(runState==false){
			System.out.println("Display updater end !");
			server.stopRecording();
		}
		this.runState = runState;
	}

	public void reset() {
		for(IDisplayUpdaterListener listener: listeners)
			listener.reset() ;
	}

	private boolean serverActive = false ;
	private Server server = null ;
	public void configurationChanged() {
		if(ProjectStarter.getConf().getValueInt("startTcpIpInputServer")==1&&!serverActive){
			System.out.println("[DisplayUpdater] starting Matrix update TCP/IP server");
			server = new ServerMatrixChanger();
			new Thread(server,"Matrix update server Thread").start();
			serverActive = true ;
		}else if(serverActive&&ProjectStarter.getConf().getValueInt("startTcpIpInputServer")==0){
			System.out.println("[DisplayUpdater] ! stopping Matrix update TCP/IP server");
			server.stopRecording() ;
			serverActive = false ;
		}
	}
}
