package display;

public interface IDisplayUpdaterListener {
	public void displayUpdaterExecute(int actionType) ;
	public void reset() ;
	public void updateMatrix(String[][] txt);
}
