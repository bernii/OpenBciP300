package general;

import hardware.IDriverListener;


import javolution.util.FastList;
import javolution.util.FastTable;

import astart.ProjectStarter;

public class TimeManager implements Runnable, IDriverListener {
	
	private volatile boolean runManager = true ;
	private volatile boolean freeze = false ;
	public void setRunState(boolean runState){
		System.out.println("[TimeManager] runState = "+runState);
		runManager = runState ;
	}
	
	public boolean getRunState(){
		return runManager ;
	}
	
	FastTable<TimeManagerListener> timers = new FastTable<TimeManagerListener>() ;
	FastList<TimeManagerListener[]> listnersChain = new FastList<TimeManagerListener[]>() ;
	private FastTable<TimeListener> listners = new FastTable<TimeListener>();
	
	public void addTimeManagerListener(ITimeManagerListener lis, int[] actionType){
		listners.add(new TimeListener(lis,actionType)) ;
	}
		
	public int getListenersChainSize(){
		return listnersChain.size() ;
	}
	public int addTimeManagerListenerChain(long[] timeSpan,int[] actionType,boolean loop){
		TimeManagerListener[] chain = new TimeManagerListener[timeSpan.length] ;
		long timeSp = 0 ;
		for(int i=0;i<timeSpan.length;i++){
			timeSp = timeSpan[i] ;
			chain[i] = new TimeManagerListener(timeSp,actionType[i]);
			chain[i].setMakeLoop(loop);
		}
		listnersChain.add(chain);
		return listnersChain.size()-1 ;
	}
	
	volatile long currTime = 0 ;
	volatile long profileTime = 0 ;
	volatile long timeToSleep = 1000 ;
	private boolean makeReset = false;
	private boolean makeFastReset;
	private boolean isInReset = false;
	private static long usSinceLastRunTime;
	private long tempSleepTime;
	private TimeManagerListener indicator;
	private static long oneSampleTime;
	private static long diff;
	
	private void makeReset(){
		isInReset = true ;
		System.out.println("Time manager reset.. ");
		oneSampleTime = ProjectStarter.getDriver().getSamplesInterspace() ;
		dataCounter = 0 ;
		if(!makeFastReset){
			try {
				synchronized(this){
					this.wait() ;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}else
			makeFastReset = false ;
		for (TimeManagerListener[] chain : listnersChain){
			chain[0].setLastRunTime(oneSampleTime*dataCounter);
			for(TimeManagerListener li : chain)
				li.setHasExecuted(false);
		}
		makeReset = false ;
		isInReset = false ;
	}
	
	public void run(){
		isInReset = true ;
		synchronized(this){
			try {
				System.out.println("[TimeManager] waitBefore - "+ProjectStarter.getConf().getValueInt("waitBeforeTime"));
				wait(ProjectStarter.getConf().getValueInt("waitBeforeTime"));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		isInReset = false ;
		System.out.println("[TimeManager] wait end");
		oneSampleTime = ProjectStarter.getDriver().getSamplesInterspace() ;
		while(runManager){
			if(makeReset)
				makeReset() ;

			timeToSleep = 100000000 ;
			int index = 0 ;
			
			for (TimeManagerListener[] chain : listnersChain) {
				int nextElem = 0 ;
				for (int i =0 ;i<chain.length;i++){
					indicator = chain[i];
					nextElem = i+1 ;
					if(i==chain.length-1){
						nextElem = 0 ;
					}
					
					if(indicator.hasExecuted()==true){
						continue ;
					}
					currTime = oneSampleTime*dataCounter ;
					usSinceLastRunTime = currTime-indicator.getLastRunTime();
					tempSleepTime = indicator.getExecutionSpanus()-usSinceLastRunTime ;
					if( usSinceLastRunTime >= indicator.getExecutionSpanus() ){
						if(indicator.hasNeverExecuted()==false){
							diff = usSinceLastRunTime - indicator.getExecutionSpanus();
							if(Math.abs(diff)>4000)
								System.out.println("Error probable !! Executing["+index+","+i+"] - "+usSinceLastRunTime+"us since last run time | span: "+indicator.getExecutionSpanus()+"us diff:"+diff);
							chain[nextElem].setLastRunTime(currTime) ;
							indicator.execute() ; 
							for(TimeListener lis : listners)
								if(lis.isRegistredForAction(indicator.getActionType())){
									lis.getListener().timeManagerExecute(indicator.getActionType(),diff);
								}
						}
						indicator.setLastRunTime(currTime) ;
						if(timeToSleep>(currTime+chain[nextElem].getExecutionSpanus()-(currTime-chain[nextElem].getLastRunTime()))){
							timeToSleep = currTime+chain[nextElem].getExecutionSpanus()-(currTime-chain[nextElem].getLastRunTime()) ;
						}
						
					}else{
						// time too short but update sleep time
						if(timeToSleep>(currTime+tempSleepTime)){
							timeToSleep = tempSleepTime+currTime ;
						}
						break ;
					}
					if(i==chain.length-1){
						if(chain[nextElem].getMakeLoop()==true){
							for (TimeManagerListener l : chain){
								if(l.actionType==ITimeManagerListener.PROCESSING_START)
									l.isWaiting = true ;
								l.setHasExecuted(false);
							}
						}else
							listnersChain.remove(index)	;	
						chain[nextElem].setLastRunTime(currTime) ;
					}
								
				}
				index++ ;
			}
			profileTime = System.nanoTime() ;
			try {
				waitIfPossible();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		ProjectStarter.getControlPanel().unblockButtons();
		System.out.println("[TimeManager] Time manager stopped.");
	}
	
	private synchronized void waitIfPossible() throws InterruptedException{
		while(timeToSleep>oneSampleTime*dataCounter&&runManager){
			this.wait();
		}
	}
		
	private class TimeListener{

		private ITimeManagerListener listener;
		private int[] actionTypes;

		public TimeListener(ITimeManagerListener listener, int[] actionTypes){
			this.listener = listener  ;
			this.actionTypes = actionTypes ;
		}
		
		public boolean isRegistredForAction(int actionType) {
			for(int actionTpe : actionTypes)
				if(actionTpe == actionType)
					return true ;
			return false;
		}

		public ITimeManagerListener getListener(){
			return listener ;
		}
		
		public int[] getActionTypes(){
			return actionTypes ;
		}

	}
	private class TimeManagerListener{
		private long timeSpan;
		
		public void setTimeSpan(long timeSpan){
			this.timeSpan = timeSpan ;
		}
		public boolean isWaiting() {
			return isWaiting;
		}
		private boolean isWaiting = false ;
		private long lastRunTime = - 32;
		private int actionType;
		
		private boolean makeLoop = false ;
		private boolean hasExecuted = false ;
		private boolean hasNeverExecuted = true ;

		public void setMakeLoop(boolean loop){
			makeLoop = loop ;
		}
		
		public void execute() {
			hasExecuted = true ;
		}

		public boolean hasNeverExecuted() {
			return hasNeverExecuted ;
		}
		public boolean getMakeLoop(){
			return makeLoop ;
		}

		public void setHasExecuted(boolean hasExecuted){
			this.hasExecuted = hasExecuted ;
		}
		public boolean hasExecuted(){
			return hasExecuted ;
		}
		public long getLastRunTime(){
			return lastRunTime ;
		}
		public void setLastRunTime(long lastRunTime){
			this.lastRunTime = lastRunTime ;
			hasNeverExecuted = false ;
		}
			
		public long getExecutionSpanus(){
			return timeSpan*1000 ;
		}
		
		public int getActionType(){
			return actionType ;
		}
		
		public TimeManagerListener(long timeSpan, int actionType) {
			this.timeSpan = timeSpan ;
			this.actionType = actionType ;
		}
	}

	public boolean isFreeze() {
		return freeze;
	}

	public void setFreeze(boolean freeze) {
		this.freeze = freeze;
	}

	public void reset() {
		System.out.println("[TimeManager] is goin to reset");
		makeReset = true ;
	}

	public void updateTimeSpan(int actionType, int newValue) {
		for (TimeManagerListener[] chain : listnersChain){
			for(TimeManagerListener li : chain)
				if(li.getActionType()==actionType){
					li.setTimeSpan(newValue) ;
					if(actionType==ITimeManagerListener.PROCESSING_START)
						li.isWaiting = true ;
				}
		}		
	}
	
	public void updateTime(int actionType, long currTime2) {
		for (TimeManagerListener[] chain : listnersChain){
			for(TimeManagerListener li : chain)
				if(li.getActionType()==actionType){
					li.setLastRunTime(currTime2) ;
					li.isWaiting = false ;
				}
		}		
	}

	public boolean isMakeFastReset() {
		return makeFastReset;
	}

	public void setMakeFastReset(boolean makeFastReset) {
		this.makeFastReset = makeFastReset;
	}

	private volatile long dataCounter = 0 ;
	public void dataArrived(EegData eeg) {
		if(!isInReset){
			dataCounter++ ;
			synchronized(this){
				this.notify();		
			}
		}
	}
	
}
