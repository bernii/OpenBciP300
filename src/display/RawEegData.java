package display;

import general.EegData;
import hardware.DriverExecutor;
import hardware.IDriverListener;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.graphics.Image;

public class RawEegData implements IDriverListener {

	public Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="10,10"
	private Label labelStatisticalData = null;
	private Canvas canvasSignals = null;

	public RawEegData(Shell parent) {
		disp = parent.getDisplay() ;
		createSShell(parent);
		sShell.open();
	}


	/**
	 * This method initializes canvasSignals	
	 *
	 */
	int signalXpos = 0 ; 
	private void createCanvasSignals() {
		canvasSignals = new Canvas(sShell, SWT.NONE);
		canvasSignals.setBounds(new Rectangle(4, 26, 577, 230));
		canvasSignals.addPaintListener(new PaintListener() { 
			public void paintControl(PaintEvent e) {
		        // Creates new image only absolutely necessary.
		        Image image = (Image) canvasSignals.getData("double-buffer-image");
		        if (image == null
		          || image.getBounds().width != canvasSignals.getSize().x
		          || image.getBounds().height != canvasSignals.getSize().y) {
		          image =
		            new Image(
		              disp,
		              canvasSignals.getSize().x,
		              canvasSignals.getSize().y);
		          canvasSignals.setData("double-buffer-image", image);
		        }
		        
		        // Initializes the graphics context of the image. 
		        GC imageGC = new GC(image);
		        imageGC.setBackground(e.gc.getBackground());
		        imageGC.setForeground(e.gc.getForeground());
		        imageGC.setFont(e.gc.getFont());

		    	  if(signalXpos>canvasSignals.getSize().x)
		    		  signalXpos = 0 ;
		    	  if(actualData!=null){
		    		  imageGC.setClipping(signalXpos-1,0,signalXpos+1, canvasSignals.getSize().y);
		    		  imageGC.setForeground(disp.getSystemColor(SWT.COLOR_WHITE)); 
    				  imageGC.drawLine(signalXpos+1,0,signalXpos+1,canvasSignals.getSize().y);   				  
    				  imageGC.setForeground(disp.getSystemColor(SWT.COLOR_BLUE)); 
    				  imageGC.drawLine(signalXpos+2,0,signalXpos+2,canvasSignals.getSize().y);  
		    		  imageGC.setForeground(disp.getSystemColor(SWT.COLOR_RED)); 
		    		  drawSingals(actualData,imageGC);
			    	  signalXpos++ ;
		    	  }
		        // Draws the buffer image onto the canvas. 
		        e.gc.drawImage(image, 0, 0);       
		        imageGC.dispose();     
		      }
		    });
		maxPosition = canvasSignals.getSize().x ;
	}


	protected void drawSingals(EegData actualData, GC imageGC) {
		int stepY = canvasSignals.getSize().y / channels ;
		if(prevData!=null)
			for(int i = 0 ;i < channels ; i ++){
				int posY =(i+1)*stepY - stepY/2 ;
				float val1 = (float)prevData.getValues()[i]/1024 ;
				float val2 = (float)actualData.getValues()[i]/1024 ;
				imageGC.drawLine(signalXpos,posY+(int)(val1*stepY-10),signalXpos+1,posY+(int)(val2*stepY-10));
			}
		prevData = actualData ;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/* Before this is run, be sure to set up the launch configuration (Arguments->VM Arguments)
		 * for the correct SWT library path in order to run with the SWT dlls. 
		 * The dlls are located in the SWT plugin jar.  
		 * For example, on Windows the Eclipse SWT 3.1 plugin jar is:
		 *       installation_directory\plugins\org.eclipse.swt.win32_3.1.0.jar
		 */
		
	}
	
	/**
	 * This method initializes sShell
	 */
	private Display disp ;
	public void createSShell(Shell parent) {
		sShell = new Shell(parent);
		sShell.setText("Raw EEG data");
		sShell.setSize(new Point(602, 297));
		sShell.setLayout(null);
		labelStatisticalData = new Label(sShell, SWT.NONE);
		labelStatisticalData.setBounds(new Rectangle(4, 2, 578, 19));
		labelStatisticalData.setText("Some statistical data - probek / s itp");
		createCanvasSignals();
	}

	int lastDataNum = 0 ;
	int valuePosition = 0 ;
	int maxPosition ;
	EegData actualData ;
	EegData prevData ;
	long lastDrawTime = System.currentTimeMillis()  ;
	private int channels;
	private int scaleX = 75 ;
	public void dataArrived(EegData eeg) {
		if(scaleX==0){
			System.out.println("scale X = "+scaleX);
			lastDrawTime = System.currentTimeMillis();
			if(valuePosition>maxPosition-1)
				valuePosition = 0 ;
			if(lastDataNum==-1)
				lastDataNum = 0  ;
			actualData = eeg ;
			channels = eeg.getChannelsNumber() ;
				
			disp.asyncExec(new Runnable() {
				 public void run() {
					 if(!sShell.isDisposed())
						 canvasSignals.redraw() ; 
				 }
			});
			scaleX = 75 ;
		}
		scaleX-- ;
	}

	int[][] values  ;

	public boolean isDisposed() {
		return sShell.isDisposed() ;
	}
}
