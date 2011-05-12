package signalProcessing;

public interface ISignalAccumulatorListener {
	
	void dataPortionAccumulated(int[] eegData,int signalIndex);
	
}
