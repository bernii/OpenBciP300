package display;

import general.ITimeManagerListener;
import general.Utils;

import java.util.Random;

import javolution.util.FastTable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import astart.ProjectStarter;
import collect.IShowedSymbols;
import configuration.ElementMatrixDefinition;

public class UserVisibleMatrixCanvas extends Canvas implements IDisplayUpdaterListener, IShowedSymbols {

	private Display disp;
	private String[][] chars;
	private MatrixTextElement[] matrixElements ;
	private int[]highlightedPositions ;
	private Point matrixDimensions = null ; 
	private Point step ;
	private int definedFontsize;
	private Point definedMargin;
	
	public void updateColors(Color bkg, Color normalElement, Color activeElement){
		backgroundColor = bkg ;
		for(MatrixTextElement elem: matrixElements){
			elem.setActiveColor(activeElement);
			elem.setNormalColor(normalElement);
		}
	}
	private boolean mouseDrag;
	private Point mouseStart ;
	private Point mouseStartSize ;

	private Point largeSize ;
	public UserVisibleMatrixCanvas(Composite parent, int style) {
		super(parent, style);
		largeSize = getSize() ;
		matrixDimensions = ((ElementMatrixDefinition) ProjectStarter.getConf().getElement("userVisibleMatrix")).getMatrixDimensions();
		generatehighlightedPositions() ;
		
		addMouseListener(new MouseListener() {
			public void mouseDown(MouseEvent e) {
		    	mouseDrag = true ;
		    	mouseStart = new Point(e.x,e.y);
		    	mouseStartSize = getSize();
		      }

			public void mouseDoubleClick(MouseEvent e) {
			}

			public void mouseUp(MouseEvent e) {
				largeSize = getSize() ;
				mouseDrag = false ;
			}
		});
		
		addMouseMoveListener(new MouseMoveListener() {
		      public void mouseMove(MouseEvent e) {
		    	  if(mouseDrag){
		    		  
		    		  if(definedMargin!=null)
		    			  updateElementsPositions(definedMargin, definedFontsize);
		    		  else
		    			  updateElementsPositions(new Point(0,0), step.y/2,false);
		    		  
		    		  int len = (int)(e.x-mouseStart.x + e.y-mouseStart.y);
		    		  setSize(mouseStartSize.x+len,  mouseStartSize.y+len);
		    		  ((GridData)getParent().getLayoutData()).widthHint = mouseStartSize.x+len ;
		    		  ((GridData)getParent().getLayoutData()).heightHint = mouseStartSize.y+len ;

		    		  getParent().getParent().layout(true);
		    	  }
		      }

		    });
	}

	public String[][] getChars(){
		return chars ;
	}
	public void updateMatrix(String[][]chars){
		actualHighlight = 0 ;
		this.chars = chars ;
		System.out.println("[UserVisibleMatrixCanvas] updating matrix with new values");
		this.matrixDimensions = new Point(chars[0].length,chars.length) ;
		generatehighlightedPositions();
		if(!disp.isDisposed()){
			disp.asyncExec(new Runnable() {
				public void run() {
					if(!isDisposed()){
						updateMatrixElements(UserVisibleMatrixCanvas.this.chars);
						redraw();
					}
				}
			});
		}
		
	}
	
	private int[] selectUniqueElements(int elementsToSelect){
		Random rnd = new Random() ;
		int toSelect = elementsToSelect ;
		FastTable<Integer>indexes = new FastTable<Integer>();
		while(toSelect>0){
			boolean unique = true ;
			int rndInt = rnd.nextInt(elementsToSelect) ;
			for(int index : indexes){
				if(index==rndInt)
					unique = false ;
			}
			if(unique){
				toSelect-- ;
				indexes.add(rndInt);
			}
		}
		int[] out = new int[elementsToSelect] ;
		Integer[] temp = indexes.toArray(new Integer[elementsToSelect]) ;
		for(int i=0;i<out.length;i++)
			out[i] = temp[i];
		return out;
	}

	
	private int[] generatehighlightedPositions(){

		int[] indexesRows = selectUniqueElements(matrixDimensions.y);
		int[] indexesCols = selectUniqueElements(matrixDimensions.x);
		
		if(highlightedPositions==null||highlightedPositions.length!=indexesRows.length+indexesCols.length)
			highlightedPositions = new int[indexesRows.length+indexesCols.length] ;
		int a = 0 ;
		for (int i : indexesRows) {
			highlightedPositions[a++] = i ;
		}
		for (int i : indexesCols) {
			highlightedPositions[a++] = matrixDimensions.y+i ;
		}
		return highlightedPositions ;
	}

	private int[] getItemIndexesInRow(int rowNumber){
		int[] out = new int[matrixDimensions.x];
		int a = 0 ;
		for(int i=matrixDimensions.x*rowNumber;i<matrixDimensions.x*rowNumber+matrixDimensions.x;i++)
			out[a++] = i ;
		return out ;
	}

	private int[] getItemIndexesInColumn(int colNumber){
		int[] out = new int[matrixDimensions.y];
		int a = 0 ;
		for(int i=colNumber;i<matrixDimensions.y*matrixDimensions.x;i+=matrixDimensions.x)
			out[a++] = i ;
		return out ;
	}
	
	public void createMatrixElements(){
		updateMatrixElements(((ElementMatrixDefinition) ProjectStarter.getConf().getElement("userVisibleMatrix")).getMatrixValues());
	}
	
	public void updateElementsPositions(Point margin, int fontsize, boolean updateFields){
		if(updateFields){
			this.definedMargin = margin ;
			this.definedFontsize= fontsize;
		}
		Font newFont = new Font(Display.getDefault(),"Arial",fontsize,SWT.BOLD);
		step = new Point(getSize().x/matrixDimensions.x-margin.x,getSize().y/matrixDimensions.y-margin.y) ;
		
		// set margin2 so that matrix is in the center
		int marginX = step.x/3+ (getSize().x-step.x*matrixDimensions.x)/2 ;
		int marginY = step.y/6+(getSize().y-step.y*matrixDimensions.y)/2 ;
		Point margin2 = new Point(marginX,marginY) ;
		int i = 0 ;
		int xpos = 0 ;
		int row = 0 ;
		for(MatrixTextElement elem:matrixElements){
			if(i%matrixDimensions.x==0&&i!=0){
				xpos = 0 ;
				row++ ;
			}
			elem.setPosition(new Point(margin2.x+step.x*xpos++,step.y*row+margin2.y));
			elem.setFont(newFont);
			i++;
		}
	}
	public void updateElementsPositions(Point margin, int fontsize){
		updateElementsPositions(margin,fontsize,true);
	}
	private  void updateMatrixElements(String[][] chars){
		matrixDimensions = new Point(chars[0].length,chars.length) ;
		String[] characters = new String[chars[0].length*chars.length] ;
		int row = 0 , col = 0 ;
		for(int i = 0 ; i<characters.length;i++){
			characters[i] = chars[row][col++] ;
			if(col>=chars[row].length){
				row++ ;
				col =0 ;
			}
		}
		step = new Point(getSize().x/matrixDimensions.x,getSize().y/matrixDimensions.y) ;
		Point margin = new Point(step.x/3,step.y/6) ;
		System.out.println("[UserVisibleMatrixCanvas] Creating matrix ["+matrixDimensions.x+","+matrixDimensions.y+"] ("+getSize().x+","+getSize().y+")- step("+step.x+","+step.y+")");
		row = -1 ;
		matrixElements = new MatrixTextElement[characters.length] ;
		int i = 0 ;
		int xpos = 0 ;
		Font font = new Font(Display.getDefault(),"Arial",step.y/2,SWT.BOLD);
		for (String character : characters) {
			MatrixTextElement elem = new MatrixTextElement(character,font) ;
			if(i%matrixDimensions.x==0){
				xpos = 0 ;
				row++ ;
			}
			elem.setPosition(new Point(margin.x+step.x*xpos++,step.y*row+margin.y));
			matrixElements[i++] = elem ;
		}
	}

	Color backgroundColor = Display.getDefault().getSystemColor(SWT.COLOR_BLACK) ;
	public void addMyPaintListener(){
		disp = getParent().getDisplay() ;
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Image image = (Image) UserVisibleMatrixCanvas.this.getData("double-buffer-image");
				if (image == null
						|| image.getBounds().width != UserVisibleMatrixCanvas.this.getSize().x
						|| image.getBounds().height != UserVisibleMatrixCanvas.this.getSize().y) {
					if(image!=null){
						image.dispose() ;
					}
					image =
						new Image(
								disp,
								UserVisibleMatrixCanvas.this.getSize().x,
								UserVisibleMatrixCanvas.this.getSize().y);
					UserVisibleMatrixCanvas.this.setData("double-buffer-image", image);
				}

				GC imageGC = new GC(image);
				imageGC.setBackground(backgroundColor); 
				imageGC.fillRectangle(0,0,getSize().x,getSize().y);
				if(matrixElements==null)
					createMatrixElements() ;
				int hihghLightedPos = highlightedPositions[actualHighlight] ;
				for(int i=0;i<matrixElements.length;i++){				
					if(actualAction==ITimeManagerListener.ACTION_HIGHLIGHT)
						matrixElements[i].setActive(matrixElementIsHighlighted(i,hihghLightedPos));
					matrixElements[i].render(disp,imageGC);
				}
				e.gc.drawImage(image, 0, 0);
				imageGC.dispose();
			}

		});
	}

	protected boolean matrixElementIsHighlighted(int elmIndex,
			int actualHighlighted) {
		if(actualHighlighted<matrixDimensions.y){
			for(int ind:getItemIndexesInRow(actualHighlighted))
				if(ind==elmIndex){
					return true ;
				}
		}else
			for(int ind:getItemIndexesInColumn(actualHighlighted-matrixDimensions.y))
				if(ind==elmIndex){
					return true ;
				}
		return false;
	}

	private int[] getElementIndexes(int highlitedPos){
		if(highlitedPos<matrixDimensions.y){
			return getItemIndexesInRow(highlitedPos) ;
		}else
			return getItemIndexesInColumn(highlitedPos-matrixDimensions.y) ;
	}

	private int actualHighlight = 0 ;
	private int actualAction ;
	public void displayUpdaterExecute(int actionType) {
		actualAction = actionType ;
		if(!disp.isDisposed()){
			disp.asyncExec(new Runnable() {
				public void run() {
					if(!isDisposed()){
						switch (actualAction) {
						case ITimeManagerListener.ACTION_HIGHLIGHT:
							redraw() ;
							break;

						case ITimeManagerListener.ACTION_CHANGE_POS:
							System.out.println("[UserVisibleMatrixCanvas] Change pos to "+(actualHighlight+1)+" actual -> "+highlightedPositions[actualHighlight]);
							for(int i=0;i<matrixElements.length;i++)
								matrixElements[i].setActive(false);
							actualHighlight++ ;
							if(actualHighlight==highlightedPositions.length){
								generatehighlightedPositions();
								actualHighlight = 0 ;
							}
							redraw() ;
							break;						
						case -1:
							// matrix reset
							break;
						default:
							System.out.println("[UserVisibleMatrixCanvas] ERROR: operation not supported !! action: "+actualAction);
						break;
						}
					}
				}
			});
		}

	}

	public int[] getHighlightedPositions() {
		return highlightedPositions;
	}

	/**
	 * Used when starting user matrix in seperate window
	 * @param highlightedPositions
	 */
	public void setHighlightedPositions(int[] highlightedPositions) {
		this.highlightedPositions = highlightedPositions;
	}

	/**
	 * Gets valid positions of character
	 * @param str
	 * @return
	 */
	public int[] getValidPositions(Character str){
		int out[] = new int[2];
		int b = 0 ;
		for(int i = 0;i<highlightedPositions.length;i++){
			if(i<matrixDimensions.y){
				for(int index : getItemIndexesInRow(i))
					if(matrixElements[index].getText().compareToIgnoreCase(str+"")==0)
						out[b++] = i ;
			}else{
				for(int index : getItemIndexesInColumn(i-matrixDimensions.y))
					if(matrixElements[index].getText().compareToIgnoreCase(str+"")==0)
						out[b++] = i ;
			}
		}
		return out ;
	}

	public MatrixTextElement[] getMatrixElements() {
		return matrixElements;
	}
	
	public MatrixTextElement getMatrixElementsIntersection(int[] indexes) {
		int[] tmp  = Utils.intersect(getItemIndexesInRow(indexes[0]),getItemIndexesInColumn(indexes[1]-matrixDimensions.y));
		return matrixElements[tmp[0]];
	} 
	

	public void reset() {
		System.out.println("[UserVisibleMatrixCanvas] Resetting visible matrix... ");
		actualHighlight = 0 ;		
		actualAction = -1 ;
		for(int i=0;i<matrixElements.length;i++)
			matrixElements[i].setActive(false);
		if(!disp.isDisposed()){
			disp.asyncExec(new Runnable() {
				public void run() {
					if(!isDisposed())
						redraw() ;
				}
			});
		}
	}

	public Point getLargeSize() {
		return largeSize;
	}
	
	public Point getMatrixDimensions(){
		return matrixDimensions ;
	}
}
