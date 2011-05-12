package signalProcessing;

import general.EegData;


public interface ISignalFilter {

	/**
	 * used by SignalProcessor listeners
	 */
	int[] process(int[] eegDataBuffer) ;

	/**
	 * used by listeners of DriverExecutor - modifying EegData (convolutionFilter)
	 */
	public void process(EegData data);
	
	public String getFilterName() ;
}
