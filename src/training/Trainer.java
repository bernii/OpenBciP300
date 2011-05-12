package training;

import general.EegData;
import general.EegDataWithTimeStamp;
import general.ITimeManagerListener;
import hardware.IDriverListener;
import javolution.util.FastTable;
import astart.ProjectStarter;
import collect.DataObject;
import configuration.IConfigurationListener;
import display.RsquareCanvas;
import display.UserVisibleMatrixCanvas;

public class Trainer implements IDriverListener, Runnable, ITimeManagerListener, IConfigurationListener{
	/**
	 * Trainer module allowing to check what is the delay between visual stimuli
	 * and P300 maximum  
	 */
	private volatile FastTable<EegDataWithTimeStamp> eegData = new FastTable<EegDataWithTimeStamp>();
	boolean isSignalPositive ;
	public void dataArrived(EegData eeg) {
		// add EEG data to table - for further off-line analysis
		if(processData&&runState&&answerType!=-1){
			isSignalPositive = false;
			int i=0 ;
			for(EegDataWithTimeStamp data: eegData){
				i++ ;
				if(data.getMsSinceStartTime()<responseLength){
					data.add(eeg) ;
					if(data.getMsSinceStartTime()>200&&data.getMsSinceStartTime()<250
							&&data.getAnswerType()==EegDataWithDescr.POSITIVE)
						isSignalPositive = true ;
				}
			}
			ProjectStarter.getDriver().setSignalPositive(isSignalPositive);
		}
		
	}
	volatile boolean runState = false ;
	private volatile boolean processData = false ;
	private int[] highlitedPositions;
	private int[] validPositions;
	
	public void initialize(){
		eegData.clear() ;
		positionRepetitions = 0 ;
		actualPosition = 0 ;
		trainedSymbols = 0 ;
		processData = false ;
		configurationChanged();
		updatePositivePositions();
	}
	
	private void updatePositivePositions() {
		highlitedPositions =((UserVisibleMatrixCanvas) ProjectStarter.getInstance().getControlPanel()
				.getCanvasUserVisibleMatrix()).getHighlightedPositions().clone();
		System.out.println("Valid positions ["+validPositions.length+"]:");
		for(int validPos: validPositions)
			System.out.print(" "+validPos);
		System.out.println("");

	}

	public boolean getRunState(){
		return runState ;
	}
	
	public void run() {
		this.runState = true ;
				
		while(runState){
			try {
				synchronized(this){
					this.wait() ;
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			processData = false ;
			
			if(runState){
				
				ProjectStarter.getTimeManager().reset();
				ProjectStarter.getDisplayUpdater().reset(); 
				
				ProjectStarter.getInstance().getHddCollector().addData(new DataObject(DataObject.HEADER_INITIALIZATION_SYMBOL_END)) ;
				try {
					synchronized(this){
						this.wait(3000);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				ProjectStarter.getInstance().getHddCollector().addData(new DataObject(DataObject.HEADER_INITIALIZATION_SYMBOL_START)) ;
				synchronized(ProjectStarter.getInstance().getTimeManager()){
					ProjectStarter.getInstance().getTimeManager().notify() ;
				}
			}else{
				
				ProjectStarter.getInstance().getTimeManager().setRunState(false);
			}
		}
		ProjectStarter.getInstance().getControlPanel().unblockButtons();
		System.out.println("[Trainer] Trainer stopped.");
	}
	
	int positionRepetitions = 0 ;
	int actualPosition = 0 ;
	private int trainedSymbols = 0 ;
	private volatile int answerType = -1 ; //EegDataWithDescr.NEGATIVE ;
	private int repeatsPerSymbol;
	private String textToTrain;
	int responseLength = 700 ;
	
	public void timeManagerExecute(int actionType, long timeDiff) {
		if(runState){
			if(actionType==ITimeManagerListener.ACTION_HIGHLIGHT){
				DataObject dobj = new DataObject(DataObject.HEADER_INITIALIZATION_NEXT_HIGHLIGHT);
				if(actualPosition==highlitedPositions.length){
					updatePositivePositions();				
					dobj = new DataObject(DataObject.HEADER_INITIALIZATION_NEXT_REPETITION);
					ProjectStarter.getInstance().getHddCollector().addData(dobj) ;
					positionRepetitions++ ;
					actualPosition = 0 ;
					
				}
				answerType = EegDataWithDescr.NEGATIVE ;
				for(int positivePosIndex : validPositions){
					if(positivePosIndex==highlitedPositions[actualPosition]){
						answerType = EegDataWithDescr.POSITIVE ;
					}
				}
				eegData.add(new EegDataWithTimeStamp(answerType)) ;
				dobj.setData(answerType) ;
				ProjectStarter.getInstance().getHddCollector().addData(dobj) ;
				processData = true ;
				actualPosition++ ;
			}
			if(positionRepetitions>=repeatsPerSymbol){
				System.out.println("Repetitions exceeded - next leter, size :"+trainedSymbols);
				trainedSymbols++ ;
				
				if(trainedSymbols==textToTrain.length()){
					this.setRunState(false);
					System.out.println("Training process completed !");
					drawRsquareSpecturm() ;
					ProjectStarter.getInstance().stopLearning();
				}else{
					validPositions = ((UserVisibleMatrixCanvas) ProjectStarter.getInstance().getControlPanel()
							.getCanvasUserVisibleMatrix()).getValidPositions(textToTrain.charAt(trainedSymbols)).clone() ;
				}
				positionRepetitions = 0 ;
				actualPosition = 0 ;
				synchronized(this){
					this.notify() ;
				}	
			}
		}
	}

	public void drawRsquareSpecturm() {
		trainedSymbols = 0 ;
		int lastType = EegDataWithDescr.NEGATIVE ;
		// aquiure positive signals from recorded stream
		FastTable<FastTable<EegData>> signals = new FastTable<FastTable<EegData>>() ;
		FastTable<FastTable<EegData>> negativeSignals = new FastTable<FastTable<EegData>>() ;
		System.out.println("Samples in one vector: "+eegData.get(0).getData().size());

		for(EegDataWithTimeStamp vector : eegData)
			if(vector.getAnswerType()==EegDataWithDescr.NEGATIVE&&
					(negativeSignals.size()<1||vector.getData().size()>(negativeSignals.get(0).size()-10)))
				negativeSignals.add(vector.getData()) ;
		System.out.println("Negative signals - "+negativeSignals.size()) ;
		for(int rep=0;rep<negativeSignals.size();rep++){
			System.out.println(" NEGelements["+rep+"]: "+negativeSignals.get(rep).size());
		}
		EegData[] negAvg = new EegData[negativeSignals.get(0).size()];
		for(int i =0 ; i <negAvg.length ; i++){
			int[] chans = new int[negativeSignals.get(0).get(0).getValues().length]; 
			// i - time
			EegData data = ((EegData)negativeSignals.get(0).get(i)).clone();
			for(int rep=0;rep<negativeSignals.size();rep++){
				// rep - repetition
				for(int b=0;b<chans.length;b++){
				// b - channel
					if(negativeSignals.get(rep).size()>i)
						chans[b] += negativeSignals.get(rep).get(i).getValues()[b] ;
				}
			}
			for(int b=0;b<chans.length;b++)
				chans[b] = chans[b]/negativeSignals.size() ;
			data.setValues(chans) ;
			negAvg[i] = data ;
		}
		
		// END NEGATIVE SIGS

		for(EegDataWithTimeStamp vector : eegData)
			if(vector.getAnswerType()==EegDataWithDescr.POSITIVE&&
					(signals.size()<1||vector.getData().size()>(signals.get(0).size()-10)))
				signals.add(vector.getData()) ;
		
		// avarage them
		System.out.print("Number of positive signals "+signals.size());
		for(int rep=0;rep<signals.size();rep++){
			System.out.println(" elements["+rep+"]: "+signals.get(rep).size());
		}
		EegData[] avg = new EegData[signals.get(0).size()];
		for(int i =0 ; i <avg.length ; i++){
			int[] chans = new int[signals.get(0).get(0).getValues().length]; 
			// i - time
			EegData data = ((EegData)signals.get(0).get(i)).clone();
			for(int rep=0;rep<signals.size();rep++){
				// rep - repetition
				for(int b=0;b<chans.length;b++){
				// b - channel
					if(signals.get(rep).size()>i)
						chans[b] += signals.get(rep).get(i).getValues()[b] ;
				}
			}
			for(int b=0;b<chans.length;b++)
				chans[b] = chans[b]/signals.size() ;
			data.setValues(chans) ;
			avg[i] = data ;
		}
		//draw them
		((RsquareCanvas)ProjectStarter.getInstance().getControlPanel().getCanvasRsquare()).setData(avg,negAvg);
		eegData.clear();		
	}

	public void setRunState(boolean b) {
		this.runState = b ;
	}

	public int getRepeatsPerSymbol() {
		return repeatsPerSymbol;
	}

	public void setRepeatsPerSymbol(int repeatsPerSymbol) {
		this.repeatsPerSymbol = repeatsPerSymbol;
	}

	public void configurationChanged() {
		System.out.println("[Trainer] Updating configuration");
		if(ProjectStarter.getInstance().getControlPanel()!=null){
			textToTrain = ProjectStarter.getInstance().getControlPanel().getTextOutput().getText() ;
			this.highlitedPositions = ((UserVisibleMatrixCanvas) ProjectStarter.getInstance().getControlPanel()
					.getCanvasUserVisibleMatrix()).getHighlightedPositions().clone() ;
			if(textToTrain.length()>0)
				validPositions = ((UserVisibleMatrixCanvas) ProjectStarter.getInstance().getControlPanel()
						.getCanvasUserVisibleMatrix()).getValidPositions(textToTrain.charAt(trainedSymbols)).clone() ;
		}
		repeatsPerSymbol = ProjectStarter.getConf().getValueInt("signalRepeats");
	}

	public int[] getPositivePositions() {
		return validPositions ;
	}
}
