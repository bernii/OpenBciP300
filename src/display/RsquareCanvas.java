package display;

import general.EegData;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import astart.ProjectStarter;

public class RsquareCanvas extends Canvas {

	private int style ;
	private TimeCourse timeCourse;
	private TimeCourse timeCourseRsquare;
	
	public TimeCourse[] getTimeCourseGraphs(){
		if(timeCourseRsquare!=null)
			return new TimeCourse[]{timeCourse,timeCourseRsquare} ;
		return new TimeCourse[]{timeCourse} ;
	}
	
	public RsquareCanvas(Composite parent, int style) {
		super(parent, style);
		this.style = style ;
		
		final Button checkBox = new Button(this,SWT.CHECK);
		checkBox.setText("Show map view");
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		checkBox.setLayoutData(gridData);
		checkBox.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent event) {
		    	  surface.setVisible(checkBox.getSelection());
		    	  timeCourseRsquare.setVisible(!checkBox.getSelection());
		    	  if(!checkBox.getSelection()){
		    		    GridData gridData = new GridData();
		    			gridData.verticalAlignment = GridData.FILL;
		    			gridData.grabExcessVerticalSpace = true;
		    			gridData.horizontalAlignment = GridData.FILL;
		    			gridData.grabExcessHorizontalSpace = true;
		    			timeCourseRsquare.setLayoutData(gridData);
		    	  }else{
		    		  GridData gridData = new GridData();
		    		  gridData.exclude = true ;
		    		  timeCourseRsquare.setLayoutData(gridData);
		    	  }
		    	  GridData grid = new GridData();
				  grid.exclude = !checkBox.getSelection() ;
				  surface.setLayoutData(grid);
		    	  layout(true);
		      }
		});
		int channelsNumber = ProjectStarter.getConf().getValueInt("numberOfChannels");
		surface = new Surface(RsquareCanvas.this,style,new double[2*channelsNumber][100],2,false,1); 
		timeCourseRsquare = new TimeCourse(this,SWT.NO_BACKGROUND);
		timeCourse = new TimeCourse(this,SWT.NO_BACKGROUND);
		gridData = new GridData();
		gridData.exclude = true ;
		surface.setVisible(false);
		surface.setLayoutData(gridData);
		
		gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		timeCourseRsquare.setLayoutData(gridData);
		timeCourse.setLayoutData(gridData);
		
	}

	private Display disp = getParent().getDisplay() ;
	
	private Surface surface ;
	
	private volatile EegData[] data = null ;

	private volatile double[][] doubleData;
	public void setData(EegData[] positive, EegData[] negative){
		data =  positive ;
		timeCourse.setDataToDisplay(data,negative);
		// compute Rsquare and send it to timeCourseGraph
		timeCourseRsquare.setDataToDisplay(computeRsquare(positive,negative));

		doubleData = new double[data[0].getChannelsNumber()][data.length];
		for(int chan=0;chan<data[0].getChannelsNumber();chan++){
			for(int i=0;i<data.length;i++){
				doubleData[chan][i] = data[i].getValues()[chan] ;	
			}
		}
		// rescale it
		doubleData = scaleIt(doubleData, 300, 2*data[0].getChannelsNumber());	
		if(!disp.isDisposed()&&disp!=null){
			disp.asyncExec(new Runnable() {
				public void run() {
					if(!isDisposed()){
						surface.updateData(doubleData);
						layout(true);
						update();
						redraw() ;
					}
				}
			});
		}
		
	}
	
	private EegData[] computeRsquare(EegData[] positive, EegData[] negative) {
		System.out.println("Positive si num: "+positive.length+  " negative "+negative.length);
		EegData[] rsquare = new EegData[positive.length] ;
		for(int i=0;i<positive.length;i++){
			rsquare[i] = positive[i].clone() ;
			if(i>=negative.length)
				continue;
			for(int a=0;a<positive[i].getValues().length;a++){
				if(a>positive[i].getValues().length||a>negative[i].getValues().length)
					continue;
				rsquare[i].setValue(a,Math.abs(positive[i].getValues()[a]-negative[i].getValues()[a]));
			}
		}
		return rsquare;
	}

	private static double[][] scaleIt(double[][] data, int newX, int newY) {
			double[][] dataout = new double[newY][data[0].length];
			for(int chan=0,chanIn=0;chan<newY;chan++){
				if(chan!=0&&chan%2==0)
					chanIn++ ; 
				dataout[chan] = data[chanIn] ;
			}
			return dataout ;
	}

	public void addMyPaintListener(){
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Image image = null ;
				if (image == null
						|| image.getBounds().width != getSize().x
						|| image.getBounds().height != getSize().y) {
					image =
						new Image(
								disp,
								getSize().x,
								getSize().y);
					setData("double-buffer-imageR2", image);
				}

				// Initializes the graphics context of the image. 
				GC imageGC = new GC(image);
				imageGC.setBackground(e.gc.getBackground());
				imageGC.setForeground(e.gc.getForeground());
				imageGC.setFont(e.gc.getFont());
				if(data !=null){
					surface.redraw() ;
				}
				imageGC.setBackground(e.gc.getBackground());
				imageGC.setForeground(e.gc.getForeground());
				imageGC.setFont(e.gc.getFont());
				imageGC.setLineWidth(1);
				Font font = new Font(Display.getDefault(),"Arial",7,SWT.NORMAL);
				imageGC.setFont(font); 
				imageGC.setLineStyle(SWT.LINE_DOT); 
				imageGC.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE)); 
				imageGC.drawLine(10,RsquareCanvas.this.getBounds().height-10,surface.getSize().x,RsquareCanvas.this.getBounds().height-10);
				int val = 0 ;
				int stepX = 40 ;
				int pixelWidth = 300 ;
				if(data!=null)
					pixelWidth = 2*data.length ;
				int valStep = 700/((pixelWidth)/stepX);
				for(int x = 0 ;x<pixelWidth;x+=stepX,val+=valStep){
					imageGC.drawString(""+(val),x,RsquareCanvas.this.getBounds().height-25,true);
				}
				e.gc.drawImage(image, 0, 0);
				imageGC.dispose();
				image.dispose();
			}
		});
		

	}
}
