package general;

import java.io.Serializable;

public class EegData implements Serializable {

	/**
	 * EEG serializable data
	 */
	private static final long serialVersionUID = 1L;
	private int packetNumber;
	private int channelsNumber;
	private int[] values;

	public EegData(int packetNumber, int channelsNumber, int[] values) {
		this.packetNumber = packetNumber ;
		this.channelsNumber = channelsNumber ;
		this.values = new int[channelsNumber] ;
		for(int i = 0 ; i<channelsNumber ; i++)
			this.values[i] = values[i] ;
	}

	public int getPacketNumber() {
		return packetNumber;
	}

	public void setPacketNumber(int packetNumber) {
		this.packetNumber = packetNumber;
	}

	public int getChannelsNumber() {
		return channelsNumber;
	}

	public void setChannelsNumber(int channelsNumber) {
		this.channelsNumber = channelsNumber;
	}

	public int[] getValues() {
		return values;
	}

	public void setValues(int[] values) {
		this.values = values;
	}

	public float[] getValuesNormalized() {
		float[] out = new float[values.length] ;
		for(int i = 0 ; i<values.length ; i++)
			out[i] = (float) ((float)values[i]/1024.0) ;
		return out;
	}

	public EegData clone(){
		int packetNr = packetNumber ;
		int channelsNr = channelsNumber ;
		int vals[] = new int[channelsNumber] ;
		return new EegData(packetNr,channelsNr,vals) ;
	}
	
	public String toString(){
		return "nr: "+packetNumber+" chan: "+channelsNumber;
	}

	public void setValue(int chan, int out) {
		values[chan] = out ;
	}

	public void setValues(double[] output) {
		for(int i=0;i<output.length;i++){
			values[i] = (int) output[i];
		}
	}

	public void copy(EegData eeg) {
		this.channelsNumber = eeg.channelsNumber ;
		this.packetNumber = eeg.packetNumber;
		if(values.length!= eeg.values.length)
			values = new int[eeg.values.length] ;
		for(int i=0;i<values.length;i++){
			values[i] = eeg.values[i];
		}
	}
}
