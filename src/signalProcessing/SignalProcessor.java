package signalProcessing;

import collect.DataObject;
import training.EegDataWithDescr;
import configuration.Configuration;
import general.EegData;
import general.EegDataWithTimeStamp;
import general.ITimeManagerListener;
import hardware.IDriverListener;


import javolution.util.FastTable;

import astart.ProjectStarter;

import display.MatrixTextElement;
import display.UserVisibleMatrixCanvas;

public class SignalProcessor implements Runnable, IDriverListener, ITimeManagerListener {

	private FastTable<EegData> eegDataBuffer;
	private int startTime ;
	private int endTime ;
	FastTable<ISignalProcessorListener> signalProcessorListeners = new FastTable<ISignalProcessorListener>() ;
	private volatile boolean processData = false ;
	private volatile FastTable<EegDataWithTimeStamp> eegDatas = new FastTable<EegDataWithTimeStamp>();
	private int[] highlitedPositions;
	int actualPosition =0 ;
	// 3 and 8 when using random number generator as source / just for testing purposes
	private int []validPositions = new int[]{3,8};
	AveragingFilter avg = new AveragingFilter() ;
	private static int[] eegData ;
	
	public SignalProcessor(){
		ProjectStarter.getConf().addConfigurationListener(avg) ;
	}

	public void addSignalProcessorListener(ISignalProcessorListener listener){
		signalProcessorListeners.add(listener);
	}

	FastTable<ISignalFilter> signalFilters = new FastTable<ISignalFilter>() ;

	public void addSignalFilter(ISignalFilter filter){
		signalFilters.add(filter);
	}

	public int getStartTime() {
		return startTime;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public int getEndTime() {
		return endTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}

	public void dataArrived(EegData eeg) {
		// buffering data
		if(processData){
			boolean isSignalPositive = false;
			for(EegDataWithTimeStamp data: eegDatas){
				if(data.getMsSinceStartTime()>startTime){
					if(data.getMsSinceStartTime()<endTime){
						data.add(eeg) ;
						if(data.getAnswerType()==EegDataWithDescr.POSITIVE)
							isSignalPositive  = true ;
					}else if(!data.isAnalyzed()){
						data.setAnalyzed(true);
						eegDataBuffer = data.getData() ;
						synchronized(this){
							this.notify();
						}
					}
				}
			}
			ProjectStarter.getDriver().setSignalPositive(isSignalPositive);			
		}
		System.nanoTime();
	}

	private void updatePositivePositions() {
		highlitedPositions =((UserVisibleMatrixCanvas) ProjectStarter.getInstance().getControlPanel()
				.getCanvasUserVisibleMatrix()).getHighlightedPositions();
	}
	
	public void timeManagerExecute(int actionType, long timeDiff) {
		if(!runState)
			return ;
		int answerType;
		switch (actionType){
			case ITimeManagerListener.ACTION_HIGHLIGHT:
				processData = true ;
				if(highlitedPositions==null)
					updatePositivePositions();
				
				if(actualPosition==highlitedPositions.length){
					updatePositivePositions();				
					actualPosition = 0 ;
					
				}
				answerType = EegDataWithDescr.NEGATIVE ;
				for(int positivePosIndex : validPositions){
					if(positivePosIndex==highlitedPositions[actualPosition]){
						answerType = EegDataWithDescr.POSITIVE ;
					}
				}
				eegDatas.add(new EegDataWithTimeStamp(answerType)) ;
				actualPosition++ ;
			break;
			
			default:
				System.out.println("ERROR (SignalProcessor) - action type not supported!!");
				break ;

		}
	}
	
	public void reset(){
		processData= false ;
		actualPosition =0 ;
		eegDatas.clear() ;
	}
	
	private void processBuffer() {
		System.out.println("SignalProcessor] starting processing buffer");
		// process data available in buffer
		// averaged signals from many channels
		eegData = avg.process(eegDataBuffer).clone() ;
		for(ISignalFilter filter : signalFilters){
			eegData = filter.process(eegData) ;
		}

		for(ISignalProcessorListener listener : signalProcessorListeners){
			listener.dataPortionProcessed(eegData) ;
		}
	}
	private boolean runState = false ;
	
	public boolean getRunState(){
		return runState ;
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
			processBuffer();
		}
	}
	public boolean isRunState() {
		return runState;
	}
	public void resetGenerator(){
		reset();
	}
	public void setRunState(boolean runState) {
		this.runState = runState;
	}
	
}
