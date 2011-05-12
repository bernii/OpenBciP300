package general;

import javolution.util.FastTable;

public class EegDataWithTimeStamp {
	
	public EegDataWithTimeStamp(int answerType){
		this.answerType = answerType ;
	}
	public EegDataWithTimeStamp() {
	}
	private long startTime = System.currentTimeMillis() ;
	
	private boolean analyzed = false ;
	
	private int answerType ;
	
	public int getAnswerType(){
		return answerType ;
	}
	public long getMsSinceStartTime(){
		return System.currentTimeMillis() - startTime;
	}
	
	private FastTable<EegData> eegData = new FastTable<EegData>() ;
	
	public void add(EegData eegData) {
		this.eegData.add(eegData);
	}
	
	public FastTable<EegData> getData(){
		return eegData ;
	}
	public void setAnalyzed(boolean analyzed) {
		this.analyzed = analyzed;
	}
	public boolean isAnalyzed() {
		return analyzed;
	}
}