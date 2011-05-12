package result;


import javolution.util.FastTable;
import configuration.IConfigurationListener;

import server.Server;
import server.ServerResult;
import signalProcessing.ISignalAccumulatorWinnerListener;

import display.MatrixTextElement;
import display.UserVisibleMatrixCanvas;

import astart.ProjectStarter;

/**
 * Updates everything that wants to know what is the classification result
 *  - GUI, server clients, file, etc...
 * @author berni
 *
 */
public class ResultManager implements ISignalAccumulatorWinnerListener, IConfigurationListener {

	private FastTable<IResultListener> resultListeners = new FastTable<IResultListener>() ;
	private String outText;

	public void addResultListener(IResultListener listner){
		resultListeners.add(listner) ;
	}
	public void notifyListeners() {
		for(IResultListener listner: resultListeners)	
			listner.resultArrived(outText);
	}
	
	public void indexesWon(int[] indexes) {
		MatrixTextElement me = ((UserVisibleMatrixCanvas)ProjectStarter.getInstance().getControlPanel().getCanvasUserVisibleMatrix()).getMatrixElementsIntersection(indexes);
		outText = me.getText() ;
		notifyListeners() ;
	}
	
	public void stopRecording(){
		for(IResultListener listner: resultListeners)	
			listner.stopRecording();
	}
	Server serverResult ;
	HardDriveResult hardDriveResult ;
	public void configurationChanged() {
		if(ProjectStarter.getConf().getValueInt("startTcpIpServer")==1&&resultListeners.contains(serverResult)==false){
			System.out.println("[ResultManager] starting TCP/IP server");
			serverResult = new ServerResult();
			new Thread(serverResult,"ServerResult Thread").start();
			addResultListener((IResultListener) serverResult) ;
		}else if(resultListeners.contains(serverResult)==true&&ProjectStarter.getConf().getValueInt("startTcpIpServer")==0){
			System.out.println("[ResultManager] ! stopping TCP/IP server");
			resultListeners.remove(serverResult) ;
			serverResult.stopRecording() ;
		}
		
		if(ProjectStarter.getConf().getValueInt("logClassificationToFile")==1&&resultListeners.contains(hardDriveResult)==false){
			hardDriveResult = new HardDriveResult(ProjectStarter.getConf().getValueString("subjectName")) ;
			addResultListener(hardDriveResult) ;
		}else if(resultListeners.contains(hardDriveResult)==true){
			resultListeners.remove(hardDriveResult) ;
			hardDriveResult.stopRecording() ;
		}
		
		
		
		
	}
}
