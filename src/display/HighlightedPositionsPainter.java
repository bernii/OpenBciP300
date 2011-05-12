package display;

import javolution.util.FastTable;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class HighlightedPositionsPainter {
	
	private FastTable<Image> hihglightedImages = new FastTable<Image>() ;
	private UserVisibleMatrixCanvas canvas;

	public HighlightedPositionsPainter(UserVisibleMatrixCanvas canvas){
		this.canvas = canvas ;
	}
	
	public Image getImage(int positionNumber, GC imageGC){
		if(hihglightedImages.size()==0){
			int[] highlightedPositions = canvas.getHighlightedPositions() ;
			Point dimensions = canvas.getMatrixDimensions() ;
			int a = 0 ;
			for(int position =0 ;position<highlightedPositions.length;position++){
				Image img = new Image(imageGC.getDevice(),dimensions.x*2,dimensions.y*2);	
				GC imgGC = new GC(img);
				imgGC.setBackground(new Color(imageGC.getDevice(),0,0,0));
				imgGC.setForeground(new Color(imageGC.getDevice(),255,255,255));
				imgGC.fillRectangle(0,0,12,14);
				imgGC.setBackground(new Color(imageGC.getDevice(),255,0,0));
				if(position<dimensions.y){
					imgGC.fillRectangle(0,2*position,12,2);
				}else{
					imgGC.fillRectangle(2*(position-dimensions.y),0,2,14);
				}
				hihglightedImages.add(img);
				a++ ;
			}
		}
		return hihglightedImages.get(positionNumber);

	}
	
}
