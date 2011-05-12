package display;

import javolution.util.FastTable;
import general.EegData;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import astart.ProjectStarter;

public class TimeCourse extends Canvas {

	private EegData[] eegData;
	Display disp = Display.getDefault() ;
	Verticles verts = new Verticles();
	
	Point interestPoint = null ;
	int interestIndex = -1 ;
	int startChan = 0 ;
	private int lastInterestIndex;
	private EegData[] eegData2;
	
	public void setStartChan(int chan){
		startChan = chan ;
		redraw();
	}
	public TimeCourse(Composite parent, int style) {
		super(parent, style);
		addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
		        Point p = getSize();
		        
		        interestIndex = verts.getNearestIndex(new Point(e.x,e.y));
		        if(interestIndex!=-1&&lastInterestIndex != interestIndex){
		        	lastInterestIndex = interestIndex ;
		        	interestPoint = verts.getScreenValue(interestIndex);
			        redraw();
		        }
		      }

		    });
		addPaintListener(new PaintListener(){
			
			int marginY = 10 ;
			int graphHeight = 0 ;
			int numberOfGraphs = 3 ;
			
			float signalXpos = 0 ;
			int minY = Integer.MAX_VALUE ;
			int maxY = Integer.MIN_VALUE ;
			int chanNumber = 0 ;
			int newYmax = 0 ;
			int marginX = 3 ;
			private float sampleLen;
			
			public void paintControl(PaintEvent e) {
				Image image = new Image(disp, getSize().x, getSize().y);
				
				GC imageGC = new GC(image);
				imageGC.setBackground(e.gc.getBackground());
				imageGC.setForeground(e.gc.getForeground());
				imageGC.setFont(e.gc.getFont());
				
				verts.clear() ;
//				System.out.println("[TimeCourse] painting");
				graphHeight = (getSize().y-numberOfGraphs*(marginY-1))/numberOfGraphs ;
				int startY = 0 ;
				for(int i=0;i<numberOfGraphs;i++){
					marginX = 3 ;
					signalXpos = 0 ;
					imageGC.setForeground(disp.getSystemColor(SWT.COLOR_GRAY));
					// osY
					imageGC.drawLine(marginX, startY, marginX, startY+graphHeight);
					
					//dane
					minY = Integer.MAX_VALUE ;
					maxY = Integer.MIN_VALUE ;
					chanNumber = startChan+i ;
					newYmax = startY+graphHeight ;
					
					if(eegData!=null){
						for(EegData data: eegData){
							if(data.getValues()[chanNumber]<minY)
								minY = data.getValues()[chanNumber] ;
							if(data.getValues()[chanNumber]>maxY)
								maxY = data.getValues()[chanNumber] ;
						}
						if(eegData2!=null){
							for(EegData data: eegData2){
								if(data.getValues()[chanNumber]<minY)
									minY = data.getValues()[chanNumber] ;
								if(data.getValues()[chanNumber]>maxY)
									maxY = data.getValues()[chanNumber] ;
							}
						}
						if(minY>0)
							minY = 0 ;
						int val0 = newYmax - (int) (((float)(-minY)*(graphHeight))/(maxY-minY)) ;
						imageGC.drawLine(marginX, val0, getSize().x, val0);
						
						// descriptions of Y axis
						for(int b=0,c=0;b<graphHeight;b+=graphHeight/5,c++){
							imageGC.drawString(" "+(minY+c*(maxY-minY)/5), marginX+2, newYmax-b,true);
						}
						sampleLen = 1000f/ProjectStarter.getConf().getValueInt("samplingFrequency") ;
						if(i==numberOfGraphs-1){
							// descriptions of X axis
							for(int b=0,c=0;b<getSize().x;b+=(getSize().x-marginX)/10,c++){
								if(c==0)
									continue;
								imageGC.drawString(" "+(c*(eegData.length*sampleLen)/10), b, newYmax-18,true);
							}
						}
					}
					
					if(eegData2!=null){
						drawTimeCourse(eegData2,imageGC,disp.getSystemColor(SWT.COLOR_BLUE));
					}
					signalXpos = 0 ;
					if(eegData!=null){
						drawTimeCourse(eegData,imageGC,disp.getSystemColor(SWT.COLOR_RED));
					}
					
					
					startY += graphHeight+marginY ;
				}
				if(interestIndex!=-1){
					imageGC.setForeground(disp.getSystemColor(SWT.COLOR_BLUE)); 
					Point realVal = verts.getRealValue(interestIndex);
					imageGC.drawOval(interestPoint.x-3, interestPoint.y-3, 6, 6);
					imageGC.drawString("("+realVal.x+","+realVal.y+")", interestPoint.x+5, interestPoint.y,true);
				}
				e.gc.drawImage(image, 0, 0);	 
				imageGC.dispose();
				image.dispose() ;
			}
			private void drawTimeCourse(EegData[] eegData, GC imageGC, Color color) {
				float stepX = (float)(getSize().x-marginX)/eegData.length ;
				for(int a=0;a<eegData.length-1;a++){
					EegData actualVal = eegData[a];
					EegData nextVal = eegData[a+1];
					// x< [a, b] i y<[c, d].  y=(x-a)*(d-c)/(b-a)+c
					// x = 0 -> y = (-a)*(d-c/(b-a)+c)
					int val1 = newYmax - (int) (((float)(actualVal.getValues()[chanNumber]-minY)*(graphHeight))/(maxY-minY)) ;
					int val2 = newYmax - (int) (((float)(nextVal.getValues()[chanNumber]-minY)*(graphHeight))/(maxY-minY)) ;
					imageGC.setForeground(color); 
					verts.addVerticle(new Point((int)(marginX+signalXpos),val1), new Point((int) (a*sampleLen),actualVal.getValues()[chanNumber]));
					imageGC.drawLine((int)(marginX+signalXpos),val1,(int)(marginX+(signalXpos+stepX)),val2);
					signalXpos+= stepX ;
				}
			}
			
		});
	}

	public void setDataToDisplay(EegData[] data) {
		this.eegData = data ;
	}
	
	public void setDataToDisplay(EegData[] data,EegData[] data2) {
		this.eegData = data ;
		this.eegData2 = data2 ;
	}

	private class Verticles{
		FastTable<Point>screenVals = new FastTable<Point>();
		FastTable<Point>realVals = new FastTable<Point>();
		
		public void addVerticle(Point screenVal, Point realVal){
			screenVals.add(screenVal);
			realVals.add(realVal);
		}
		public void clear() {
			screenVals.clear();		
			realVals.clear();	
		}
		public int getNearestIndex(Point screenVal){
			int index = -1 ;
			int value = Integer.MAX_VALUE ;
			int i = 0 ;
			for(Point val:screenVals){
				int distance = (int) Math.sqrt(Math.pow(val.x-screenVal.x, 2)+Math.pow(val.y-screenVal.y, 2)) ;
				if(distance<value){
					index = i ;
					value = distance ;
				}
				i++;
			}
			return index;
		}
		
		public Point getRealValue(int index){
			return realVals.get(index);
		}
		
		public Point getScreenValue(int index){
			return screenVals.get(index);
		}
	}
}
