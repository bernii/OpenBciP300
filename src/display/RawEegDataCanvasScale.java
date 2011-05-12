package display;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import astart.ProjectStarter;
import configuration.IConfigurationListener;

/**
 * Class responsible for drawing scale and metrics in RawEegDataCanvas screen
 * @author berni
 *
 */
public class RawEegDataCanvasScale extends Canvas implements IConfigurationListener {

	// 1 pixel = 1 sample = 1000/SamplingFreq ms
	private int scaleFactorX = 0 ;
	private Display disp;
	private int msPerSample;
	
	public RawEegDataCanvasScale(Composite parent, int style) {
		super(parent, style);
		addMyPaintListener() ;
		disp = Display.getDefault() ;
	}

	public void configurationChanged() {
		scaleFactorX = ProjectStarter.getConf().getValueInt("scaleFactorX") ;
		msPerSample = 1000 / ProjectStarter.getConf().getValueInt("samplingFrequency") ;
		if(!disp.isDisposed()&&disp!=null){
			disp.asyncExec(new Runnable() {
				public void run() {
					if(!isDisposed())
						redraw() ; 
				}
			});
		}
	}
	
	private void addMyPaintListener(){
		addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				
				Font font = new Font(Display.getDefault(),"Arial",6,SWT.NONE); 
				e.gc.setFont(font);
				e.gc.drawLine(0,4*getBounds().height/5,getBounds().width,4*getBounds().height/5) ;
				
				for(int i = 0 ; i<getBounds().width ;i+=10){
					if(i%50==0){
						drawDivision(e.gc, true, i);
						e.gc.drawString(""+i*(scaleFactorX+1)*msPerSample,i,getBounds().height/10,true);
					}else{
						drawDivision(e.gc, false, i);
					}
				}
			}
			
			private void drawDivision(GC gc, boolean longDiv, int x){
				if(longDiv)
					gc.drawLine(x,getBounds().height/3,x,getBounds().height-1);
				else
					gc.drawLine(x,getBounds().height/2,x,getBounds().height-1);
			}
		});
	};
}
