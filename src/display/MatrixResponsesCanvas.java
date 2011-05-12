package display;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import configuration.ElementMatrixDefinition;

import signalProcessing.ISignalAccumulatorListener;
import astart.ProjectStarter;

public class MatrixResponsesCanvas extends Canvas implements ISignalAccumulatorListener {

	private Display disp;

	public MatrixResponsesCanvas(Composite parent, int style) {
		super(parent, style);
	}

	Point spacing ;
	private int marginY;
	private int rowMargin ;

	private HighlightedPositionsPainter positionPainter ;
	public void addMyPaintListener(){
		disp = getParent().getDisplay() ;
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if(spacing==null){
					e.gc.fillRectangle(0,0,getBounds().width,getBounds().height);
					spacing = new Point(getSize().x/((dataPortions.length+1)/2),(getSize().y-20)/2) ;
					marginY = -8 ;
					rowMargin = 8 ;
				}
				
				int col = actualPortionIndex ;
				int drawingRow = 1 ;
				if(dataPortions[actualPortionIndex]!=null){
					if(actualPortionIndex>=((dataPortions.length+1)/2)){
						drawingRow++ ;
						col = actualPortionIndex - ((dataPortions.length+1)/2)  ;
					}
					e.gc.setBackground(disp.getSystemColor(SWT.COLOR_WHITE)); 
					e.gc.fillRectangle(col*spacing.x,(drawingRow-1)*(spacing.y+rowMargin+5),spacing.x,spacing.y+12) ;
					for(int a=0;a<dataPortions[actualPortionIndex].length-1;a++){
						int val = (int)(dataPortions[actualPortionIndex][a]) ;
						int val1 = (int)(dataPortions[actualPortionIndex][a+1]) ;
						if(val<minVal)
							minVal = val ;
						else if(val>maxVal)
							maxVal = val ;
						
						if(val1<minVal)
							minVal = val1 ;
						else if(val1>maxVal)
							maxVal = val1 ;
						// // x< [a, b] i y<[c, d].  y=(x-a)*(d-c)/(b-a)+c
						int maxY = (drawingRow)*getSize().y/2 ;
						e.gc.drawLine(col*spacing.x+a,(maxY-20)-(((val-minVal)*(getSize().y/2-20))/(maxVal-minVal)),
								col*spacing.x+a+1,(maxY-20)-(((val1-minVal)*(getSize().y/2-20))/(maxVal-minVal))) ;
					}
					e.gc.drawImage(positionPainter.getImage(actualPortionIndex, e.gc),col*spacing.x,marginY+drawingRow*(spacing.y+rowMargin)) ;
				}
			}
			
		});
	}
	
	private int minVal = Integer.MAX_VALUE ;
	private int maxVal = Integer.MIN_VALUE ;
	public void reset(){
		actualPortionIndex = 0 ;
	}
	
	public void update(Point dimensions){
		reset();
		this.dimensions = dimensions ;
		dataPortions = new float[dimensions.x+dimensions.y][] ;
		positionPainter = new HighlightedPositionsPainter((UserVisibleMatrixCanvas) ProjectStarter.getControlPanel().getCanvasUserVisibleMatrix());
	}
	
	private Point dimensions = ((ElementMatrixDefinition) ProjectStarter.getConf().getElement("userVisibleMatrix")).getMatrixDimensions(); 
	private float[][] dataPortions = new float[dimensions.x+dimensions.y][] ;
	private int actualPortionIndex = 0 ;
	public void dataPortionAccumulated(int[] eegData, int signalIndex) {
		actualPortionIndex = signalIndex ;
		float[] data = new float[eegData.length];
		
		for(int i=0;i<eegData.length;i++)
			data[i] = (float)eegData[i] ;
		
		dataPortions[signalIndex] = data ;
		if(!disp.isDisposed()){
			disp.asyncExec(new Runnable() {
				public void run() {
					if(!isDisposed())
						redraw();
				}
			});
		}	
	}
	public HighlightedPositionsPainter getPositionPainter() {
		return positionPainter;
	}
	public void setPositionPainter(HighlightedPositionsPainter positionPainter) {
		this.positionPainter = positionPainter;
	}

}
