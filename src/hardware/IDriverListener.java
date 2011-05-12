package hardware;

import general.EegData;

public interface IDriverListener {
	
	public void dataArrived(EegData eeg) ; 
}
