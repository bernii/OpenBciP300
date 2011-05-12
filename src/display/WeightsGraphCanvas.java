package display;

import javolution.util.FastTable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableItem;

import astart.ProjectStarter;
import configuration.ClassificatorConfElem;
import configuration.ElementClassificatorConf;

import display.draw.SplineFactory;


public class WeightsGraphCanvas extends Canvas {

	public WeightsGraphCanvas(Composite parent, int style) {
		super(parent, style);
	}

	public void addMyPaintListener(){
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				FastTable<ClassificatorConfElem> data = ((ElementClassificatorConf) ProjectStarter.getConf().getElement("classificationParams")).getClassificationParams(true);
				Point max = new Point(0,0);
				Point min = new Point(Integer.MAX_VALUE,Integer.MAX_VALUE);
				for(ClassificatorConfElem elem : data){
					if(elem.weight*100>max.y)
						max.y = (int) (elem.weight*100) ;
					if(elem.weight*100<min.y)
						min.y = (int) (elem.weight*100) ;
					if(elem.span>max.x)
						max.x = elem.span ;
					if(elem.span<min.x)
						min.x = elem.span ;
				}
				
				// make margins 	
				max.x = (int) (max.x);
				max.y = (int) (max.y);
				min.x = (int) (min.x);
				min.y = (int) (min.y);

				int size = data.size() ;
				double[] c = new double[size*3];
				int i = 0 ;
				for(ClassificatorConfElem elem : data){
					c[i] = 10+((float)(elem.span -min.x)/(max.x-min.x))*(getBounds().width-20) ;
					c[i+1] = (getBounds().height-10) - ((float)(elem.weight*100 -min.y)/(max.y-min.y))*(getBounds().height-30) ; // elem.weight*10 ;//(((float)(elem.weight*100 - min.y))/max.y)*getBounds().y ;
					c[i+2] = 0 ;
					i+=3 ;
				}  
				
				e.gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
				for(i=0;i<c.length;i+=3){
					e.gc.fillRectangle((int)(c[i]-4),(int)(c[i+1]-4),8,8);
				}

			    double[] spline1 = SplineFactory.createCubic (c,20);
			    
			    for (i = 0; i < spline1.length-3; i+=3)
			    	e.gc.drawLine((int)spline1[i],(int)spline1[i+1],(int)spline1[i+3],(int)spline1[i+4]);
			    Font font = new Font(Display.getDefault(),"Arial",6,SWT.NONE); 
				e.gc.setFont(font);
				int a = 0 ;
			    for(i=10;i<getBounds().width-10;i+=((getBounds().width-20)/15))
					e.gc.drawString(""+(int)(min.x+(a++)*((max.x-min.x)/15F)),i,getBounds().height-10,true);
			    
			    a = 0 ;
			    for(i=20;i<(getBounds().height-10);i+=((getBounds().height-30)/20)){
			    	float val = (min.y+(a++)*((max.y-min.y)/20F))/100 ;
					e.gc.drawString(""+(int)(Math.floor(val))+"."+(int)((val-Math.floor(val))*100),0,getBounds().height-i,true);
			    };
			}
		});
	};
}
