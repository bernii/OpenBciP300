package training;

import general.EegData;

public class EegDataWithDescr{
	/**
	 * EEG Data with flag indicating its type
	 * POSITIVE - row/col with letter flashed during training
	 * NEGATIVE - oposite - other symbol
	 */
	public static final int POSITIVE = 1;
	public static final int NEGATIVE = 0;
	private EegData data;
	public int type;
	
	public EegDataWithDescr(EegData data,int type){
		this.data = data ;
		this.type = type ;
	}
	
	public int getType(){
		return type ;
	}
	
	public EegData getData(){
		return data ;
	}
}