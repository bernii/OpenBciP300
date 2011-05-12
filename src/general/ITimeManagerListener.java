package general;


public interface ITimeManagerListener {

	public static final int ACTION_HIGHLIGHT = 1 ;
	public static final int ACTION_CHANGE_POS = 2 ;
	public static final int PROCESSING_START = 3 ;
	public static final int PROCESSING_END = 4 ;
	
	/**
	 * actionType - type of action that listener was registred for
	 * timmeDiffenece - difference from time that listener was registred for
	 */
	public void timeManagerExecute(int actionType, long timeDifference) ;

}
