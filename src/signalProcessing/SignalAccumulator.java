package signalProcessing;
import display.UserVisibleMatrixCanvas;
import javolution.util.FastTable;

import astart.ProjectStarter;

public class SignalAccumulator implements ISignalProcessorListener {

	private int numberOfRepeats;
	private int actualRepeat = 0 ;
	private int actualSignal = 0 ;
	private int numberOfSignals;
	private int[] signalGroups;
	FastTable<ISignalFilter> signalFilters = new FastTable<ISignalFilter>() ;
	private int[][][] eegDataBuffer ;
	AveragingFilter avg ;
	private int actuallyProcessedSignal;
	private int[] highlightedPositions;
	private static Classifier classifier = ProjectStarter.getClassifier() ;
	
	public SignalAccumulator(int numberOfRepeats,int []signalGroups){
		this.numberOfRepeats = numberOfRepeats ;
		avg = new AveragingFilter() ; 
		update(signalGroups) ;
	}
	
	FastTable<ISignalAccumulatorWinnerListener> signalAccumulatorWinnerListeners = new FastTable<ISignalAccumulatorWinnerListener>() ;

	public void addSignalAccumulatorWinnerListeners(ISignalAccumulatorWinnerListener listener){
		signalAccumulatorWinnerListeners.add(listener);
	}
	
	FastTable<ISignalAccumulatorListener> signalAccumulatorListeners = new FastTable<ISignalAccumulatorListener>() ;

	public void addSignalAccumulatorListener(ISignalAccumulatorListener listener){
		signalAccumulatorListeners.add(listener);
	}
	
	public void addSignalFilter(ISignalFilter filter){
		signalFilters.add(filter);
	}
	
	private void processBuffer() {
		// process data available in buffer
		classifier.setSignalNumber(actuallyProcessedSignal);
		classifier.setNumberOfSamples(eegDataBuffer[actuallyProcessedSignal].length) ;
		int[] eegDataAvg = avg.processInt(eegDataBuffer[actuallyProcessedSignal],actualRepeat) ;
		// CLASSIFICATION 
		for(ISignalFilter filter : signalFilters){
			eegDataAvg = filter.process(eegDataAvg) ;
		}
		// send to listeners
		for(ISignalAccumulatorListener listener : signalAccumulatorListeners){
			listener.dataPortionAccumulated(eegDataAvg,actuallyProcessedSignal) ;
		}

	}
	
	/**
	 * Processing data and selecting maximal values (in processBuffer)
	 */
	public void dataPortionProcessed(int[] eegData) {
		if(highlightedPositions==null){
			highlightedPositions = new int[((UserVisibleMatrixCanvas) ProjectStarter.getInstance().getControlPanel()
					.getCanvasUserVisibleMatrix()).getHighlightedPositions().length];
			for(int a=0;a<highlightedPositions.length;a++)
				highlightedPositions[a] = ((UserVisibleMatrixCanvas) ProjectStarter.getInstance().getControlPanel()
					.getCanvasUserVisibleMatrix()).getHighlightedPositions()[a] ;
		}
		actuallyProcessedSignal = highlightedPositions[actualSignal] ;
		eegDataBuffer[actuallyProcessedSignal][actualRepeat] = eegData ;
		processBuffer() ;
		actualSignal++ ;
		if(actualSignal==numberOfSignals){
			actualRepeat ++ ;
			actualSignal = 0 ;
			for(int a=0;a<highlightedPositions.length;a++)
				highlightedPositions[a] = ((UserVisibleMatrixCanvas) ProjectStarter.getInstance().getControlPanel()
					.getCanvasUserVisibleMatrix()).getHighlightedPositions()[a] ;
		}
		if(actualRepeat>numberOfRepeats-1){
			winnerSelect();
			reset() ;
		}
	}
	
	public void update(int []sigGroups){
		int nmberOfSignals = 0 ;
		numberOfSignals = 0 ;
		for(int siGroup : sigGroups)
			nmberOfSignals += siGroup ;
		this.numberOfSignals = nmberOfSignals ;
		this.signalGroups = sigGroups ;
		eegDataBuffer = new int[numberOfSignals][numberOfRepeats][] ;
		reset();
	}
	
	public void reset() {
		actualRepeat = 0 ;
		actualSignal = 0 ;
		eegDataBuffer = new int[numberOfSignals][numberOfRepeats][] ;
		classifier.initialize(numberOfSignals);
	}

	public void winnerSelect(){
		boolean findMaximum = ProjectStarter.getConf().getValueString("classificationAvrageType").compareTo("max")==0?true:false;
		// Selecting one data portion
		System.out.println("Selecting one data portion.. ["+signalGroups.length+"]") ;
		System.out.print("MAX == "+findMaximum+" -> Val: ") ;
		int[] extremeVal = classifier.getExtremeVal() ;
		for(int val : extremeVal)
			System.out.print(" "+val);
		System.out.println(" ");
		
		System.out.print("signalGroups: ") ;
		for(int val : signalGroups)
			System.out.print(" "+val);
		System.out.println(" ");
		
		int[] indexes = new int[signalGroups.length];
		int[] vals = new int[signalGroups.length];
		if(findMaximum){
			for(int i=0,a=0;i<numberOfSignals;i++){
				if(i==signalGroups[0])
					a++ ;
				if(extremeVal[i]>vals[a]){
					vals[a] = extremeVal[i] ;
					indexes[a] = i ;
				}
			}
		}else{
			for(int i=0;i<signalGroups.length;i++)
				vals[i] = Integer.MAX_VALUE ;
			for(int i=0,a=0;i<numberOfSignals;i++){
				if(i==signalGroups[0])
					a++ ;
				if(extremeVal[i]<vals[a]){
					vals[a] = extremeVal[i] ;
					indexes[a] = i ;
				}
			}
		}
		System.out.print("WINNERS -> ") ;
		for(int i=0;i<signalGroups.length;i++)
			System.out.print(" index("+indexes[i]+") = "+vals[i]) ;
		System.out.println(" ") ;
		for(ISignalAccumulatorWinnerListener listener : signalAccumulatorWinnerListeners){
			listener.indexesWon(indexes) ;
		}
		synchronized(ProjectStarter.getInstance()){
			ProjectStarter.getInstance().notify() ;
		}
	}

	public int getNumberOfRepeats() {
		return numberOfRepeats;
	}

	public void setNumberOfRepeats(int numberOfRepeats) {
		this.numberOfRepeats = numberOfRepeats;
		eegDataBuffer = new int[numberOfSignals][numberOfRepeats][] ;
	}
	
}
