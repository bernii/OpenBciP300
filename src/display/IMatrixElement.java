package display;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

public interface IMatrixElement {

	public void render(Display display,GC gc) ;
	
	public void setSize(Point size) ;
	public void setPosition(Point position) ;
	
	public void setActive(boolean isActive) ;
	public boolean getActive() ;
}
