package collect;

import general.EegData;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javolution.util.FastTable;

import configuration.Configuration;

public class DataObject implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4986001150302031919L;
	private int dataType;
	private Object data;
	
	public DataObject(EegData eeg) {
		this.dataType = TYPE_EEGDATA ;
		data = eeg ;
	}
	
	public DataObject(Configuration conf) {
		this.dataType = TYPE_CONFIGURATION ;
		data = conf ;
	}
		
	public DataObject(int headerType) {
		this.dataType = headerType ;
	}

	public DataObject(FastTable<int[]> highlightedPositions, int type) {
		this.data = highlightedPositions;
		dataType= type;
	}

	public DataObject(int[] highlightedPositions, int type) {
		this.data = highlightedPositions;
		dataType= type;
	}

	public Object getData() {
		return data ;
	}
	
	public int getDataType() {
		return dataType ;
	}

	public static final int TYPE_CONFIGURATION = 0 ;
	public static final int TYPE_EEGDATA = 1 ;
	public static final int TYPE_DESCRIPTIONS = 2 ;
	public static final int HEADER_INITIALIZATION = 4 ;
	public static final int HEADER_NORMAL = 3 ;
	public static final int TYPE_HIGHLIGHTED_POSITIONS = 5 ;
	public static final int HEADER_ENDFILE = 6 ;
	public static final int HEADER_INITIALIZATION_SYMBOL_END = 7 ;
	public static final int HEADER_INITIALIZATION_SYMBOL_START = 8 ;
	public static final int HEADER_INITIALIZATION_NEXT_HIGHLIGHT = 9 ;
	public static final int HEADER_INITIALIZATION_NEXT_REPETITION = 10;

	public void setData(String text) {
		this.data = text ;
	}
	
	public void setData(Object data) {
		this.data = data ;
	}
}