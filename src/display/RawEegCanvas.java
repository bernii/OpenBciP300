package display;

import general.EegData;
import hardware.IDriverListener;
import hardware.ProtocolFactory;
import javolution.util.FastList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import astart.ProjectStarter;
import configuration.IConfigurationListener;

public class RawEegCanvas extends Canvas implements IDriverListener, IConfigurationListener {

	private int scaleX = 0 ;
	private boolean isInitalized = false ;
	private int visibleChannelsNumber;
	private String[] labelsChannel;
	private Font labelFont;
	private Runnable refresh ;
	
	volatile short bufferDrawn = 0 ;
	volatile short bufferAvailable = 0 ;
	public RawEegCanvas(Composite parent, int style) {
		super(parent, style);
		for(int i=0;i<100;i++)
			actualDataBuffer.add(new EegData(0,0,new int[]{0}));
		Font systemFont = Display.getDefault().getSystemFont();
	    //     FontData objects contain the font properties.
	    //     With some operating systems a font may possess multiple
	    //     FontData instances. We only use the first one.
	    FontData[] data = systemFont.getFontData();
	    FontData data0 = data[0];
	    //     Set the font style to italic
	    data0.setHeight(6);
	    labelFont = new Font(Display.getDefault(), data0);
	    refresh = new Runnable() {
			public void run() {
				if(!isDisposed()&&isVisible()){
					redraw() ; 
				}else{
					bufferDrawn = 0 ;
					bufferAvailable = 0 ;
				}
			}
		};
	}

	private void initializeChannelLabels(){

	int numberOfChans = ProjectStarter.getConf().getValueInt("numberOfChannels") ;
		labelsChannel = new String[numberOfChans] ;
		for(int i=0;i<numberOfChans;i++){		
			String btn = "Chan "+i ;
			labelsChannel[i] = btn ;
		}
	}

	public void dataArrived(EegData eeg) {
		if(!isInitalized){
			channelsNumber = eeg.getChannelsNumber() ;	
			visibleChannelsNumber = channelsNumber ;
			ProjectStarter.getControlPanel().initialize() ;
			initializeChannelLabels() ;
			isInitalized = true ;
		}
		if(scaleX --==0){
			actualDataBuffer.get(bufferAvailable).copy(eeg);
			bufferAvailable++;
			if(bufferAvailable>=actualDataBuffer.size())
				bufferAvailable = 0 ;
			channelsNumber = eeg.getChannelsNumber() ;	

			if(!disp.isDisposed()&&disp!=null){
				disp.asyncExec(refresh);
			}
			scaleX = currentScaleX ;
		}
	}
	private int signalXpos = 0 ;  

	private FastList<EegData>  actualDataBuffer = new FastList<EegData>() ;
	private EegData prevData ;

	private int channelsNumber = 0 ;

	private void drawSingals(EegData actualData, GC imageGC) {
		if(visibleChannelsNumber==0)
			visibleChannelsNumber = 1 ;
		int stepY = getSize().y / visibleChannelsNumber; // channelsNumber ;
		if(prevData!=null&&actualData!=null){
			int oldSize = extremeVals[1]-extremeVals[0] ;
			for(int i = 0 ,a = 1 ;i < channelsNumber ; i ++){
				if(visibleChannelsNumbers[i]){
					// x< [a, b] i y<[c, d].  y=(x-a)*(d-c)/(b-a)+c
					int newYmin = (a-1)*stepY ;
					int newYmax = a*stepY ;
					int val1 = newYmax - (int) (((float)(prevData.getValues()[i]*scaleY-extremeVals[0])*(stepY))/(oldSize)) ;
					int val2 = newYmax - (int) (((float)(actualData.getValues()[i]*scaleY-extremeVals[0])*(stepY))/(oldSize)) ;

					// zero line
					imageGC.setForeground(disp.getSystemColor(SWT.COLOR_GRAY));
					imageGC.drawLine(signalXpos,newYmin+stepY/2,signalXpos+1,newYmin+stepY/2);
					imageGC.setForeground(disp.getSystemColor(SWT.COLOR_RED)); 
					imageGC.drawLine(signalXpos,val1,signalXpos+1,val2);
					a++ ;
				}
			}
		}
		prevData = actualData ;		
	}
	private Display disp = getParent().getDisplay() ;

	private int currentScaleX;
	private boolean[] visibleChannelsNumbers;
	private int scaleY =1;
	private int[] extremeVals;
	public void addMyPaintListener(){
		addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				// Creates new image only absolutely necessary.
				Image image = (Image) getData("double-buffer-image");
				if (image == null
						|| image.getBounds().width != getSize().x
						|| image.getBounds().height != getSize().y) {
					image =
						new Image(
								disp,
								getSize().x,
								getSize().y);
					setData("double-buffer-image", image);
				}

				// Initializes the graphics context of the image. 
				GC imageGC = new GC(image);
				imageGC.setBackground(e.gc.getBackground());
				imageGC.setForeground(e.gc.getForeground());
				imageGC.setFont(e.gc.getFont());

				if(signalXpos>getSize().x)
					signalXpos = 0 ;
				while(bufferDrawn!=bufferAvailable){
					EegData eegd = actualDataBuffer.get(bufferDrawn);
					imageGC.setClipping(signalXpos-1,0,4, getSize().y);
					imageGC.setForeground(disp.getSystemColor(SWT.COLOR_WHITE)); 
					imageGC.drawLine(signalXpos+1,0,signalXpos+1,getSize().y);
					imageGC.setForeground(disp.getSystemColor(SWT.COLOR_BLUE)); 
					imageGC.drawLine(signalXpos+2,0,signalXpos+2,getSize().y);
					drawSingals(eegd,imageGC);
				    
					if(isInitalized){
						int stepY = getSize().y / visibleChannelsNumber;
					    imageGC.setFont(labelFont);
					    imageGC.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
					    for(int i=0,a=0;i<labelsChannel.length;i++)
					    	if(visibleChannelsNumbers[i])
					    		imageGC.drawText(labelsChannel[i], 4, stepY*(a++), true);
					}
					if(signalXpos>2&&signalXpos<getSize().x-2)
						e.gc.drawImage(image, 0, 0);
					
					signalXpos++ ;
					bufferDrawn++;
					if(bufferDrawn>=actualDataBuffer.size())
						bufferDrawn = 0 ;
				}
				imageGC.dispose();
			}
		});
	}

	private static String stateToString(int style, int styleFlag) {
		  return (style & styleFlag) != 0 ? "on" : "off";
	}
	
	public EegData getPrevData() {
		return prevData;
	}

	public void setPrevData(EegData prevData) {
		this.prevData = prevData;
	}

	public void configurationChanged() {
		prevData = null ;
		visibleChannelsNumbers = ProjectStarter.getControlPanel().getCheckBoxesVisibleValues();
		visibleChannelsNumber = 0 ;
		for(boolean val : visibleChannelsNumbers)
			if(val)
				visibleChannelsNumber++ ;
		currentScaleX = ProjectStarter.getConf().getValueInt("scaleFactorX") ;
		extremeVals = ProtocolFactory.get(ProjectStarter.getConf().getValueString("protocolType")).getExtremeValues(); 
	}

	public int getScaleY() {
		return scaleY;
	}

	public void setScaleY(int i) {
		if(i<1)
			i = 1 ;
		this.scaleY = i ;
	}

	public static void main(String[] args) {
		// TYPE CONVERSTION CHECK
		//int val1 = newYmax - (int) (((float)(prevData.getValues()[i]*scaleY-extremeVals[0])*(stepY))/(oldSize)) ;
		int val1 = 20 - (int) (((float)(12*17)*(12))/(7)) ;
		System.out.println("Type ?  "+((float)(12*17)*(12))/(7));
	}
}
