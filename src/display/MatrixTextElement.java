package display;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

public class MatrixTextElement implements IMatrixElement {

	String text ;
	private Point position;
	private Point size;
	
	Color activeColor = Display.getDefault().getSystemColor(SWT.COLOR_WHITE) ;
	Color normalColor = new Color(Display.getDefault(),50,50,50) ;
	public MatrixTextElement(String txt, Font font){
		this.text = txt ;
		size = new Point(20,20);
		this.font = font ;
	}
	Font font;
	public void render(Display display,GC gc) {
		if(isActive)
			gc.setForeground(activeColor);
		else
			gc.setForeground(normalColor);
		gc.setFont(font);
		gc.drawString(text,position.x,position.y,true); 
	}
	
	public void setFont(Font font){
		this.font = font ;
	}

	public void setPosition(Point position) {
		this.position = position ;
	}

	public void setSize(Point size) {
		this.size = size ;
	}
	
	public Point getSize(Point size) {
		return size ;
	}

	private boolean isActive = false ;
	public boolean getActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive ;
	}

	public String getText() {
		return text ;
	}

	public Color getActiveColor() {
		return activeColor;
	}

	public void setActiveColor(Color activeColor) {
		this.activeColor = activeColor;
	}

	public Color getNormalColor() {
		return normalColor;
	}

	public void setNormalColor(Color normalColor) {
		this.normalColor = normalColor;
	}

}
