package display;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import astart.ProjectStarter;

/*File ImgMod29.java.java
Copyright 2005, R.G.Baldwin

The purpose of this program is to display a 3D
surface using color to represent the height of 
each point on the surface.

The constructor for this class receives a 3D 
surface defined as a rectangular 2D array of 
double values.  The surface values may be 
positive or negative or both.  When an object of 
the class is constructed, it draws the 3D surface
using one of six possible formats representing 
the height of each point on the surface with a 
color.  

The constructor requires four parameters:

double[][] dataIn
int blockSize
boolean axis
int display

The purpose of each parameter is as follows:

dataIn - The parameter named dataIn is a 
reference to the array containing the data that 
describes the 3D surface.

blockSize - The value of the parameter named 
blockSize defines the size of a colored square in
the final display that represents an input 
surface value.  For example, if blockSize is 1, 
each input surface value will be represented by a
single pixel in the display.  If blockSize is 5,
each input surface value will be represented by
a colored square having 5 pixels on each side.
For example, the test code in the main method 
displays a surface having 59 values along the 
horizontal axis and 59 values along the vertical 
axis.  Each value on the surface is represented 
in the final display by a colored square that is 
2 pixels on each side.

axis - The parameter named axis specifies whether
red axes will be drawn on the display with the 
origin at the center.

display - The parameter named display specifies 
one of six possible display formats.  The value 
of display must be between 0 and 5 inclusive. 
Values of 0, 1, and 2 specify the following 
formats:

  0 - Gray scale gradient from black at the 
  minimum to white at the maximum.
  1 - Color gradient from blue at the low end
  through aqua, green, yellow to red at the 
  high end. The minimum value is colored black.
  The maximum value is colored white..
  2.- The surface is subdivided into 23 levels 
  and each of the 23 levels is represented by 
  one of the following Color Contour Plot in order 
  from minimum to maximum.
    Color.BLACK
    Color.GRAY
    Color.LIGHT_GRAY
    Color.BLUE
    new Color(100,100,255)
    new Color(140,140,255)
    new Color(175,175,255)
    Color.CYAN
    new Color(140,255,255)
    Color.GREEN
    new Color(140,255,140)
    new Color(200,255,200)
    Color.PINK
    new Color(255,140,255)
    Color.MAGENTA
    new Color(255,0,140)
    Color.RED
    new Color(255,100,0)
    Color.ORANGE
    new Color(255,225,0)
    Color.YELLOW
    new Color(255,255,150)
    Color.WHITE

Values of 3, 4, and 5 for the parameter named 
display draw the surface in the same formats as 
above except that the surface values are first 
rectified and then converted to log base 10 
values before being converted to color and drawn.

When the surface is drawn, a horizontal scale 
strip is drawn immediately below the surface 
showing the colors used in the drawing 
starting with the color for the minimum at the 
left and progressing to the color for the maximum
at the right.

Regardless of whether the surface values are 
converted to log values or not, the surface 
values are normalized to cause them to extend 
from 0 to 255 before converting to color and 
drawing.

For a display value of 0 or 3, the highest point 
with a value of 255 is painted white.  The lowest
point with a value of 0 is painted black,  The 
surface is represented using shades of gray.  The
shade changes from black to white in a uniform 
gradient as the height of the normalized surface 
values progress from 0 to 255.

For a display value of 1 or 4, the lowest point 
is painted black and the highest point is 
painted white. The color changes from blue 
through aqua, green, and yellow to red in a 
smooth gradient as the normalized surface values
progress from 1 to 254.  (Values of 0 and 255 
would be pure blue and pure red if they were not
overridden by black and white.)

For a display value of 2 or 5, the highest point 
with a value of 255 is painted white.  The lowest
point with a value of 0 is painted black,  The 
surface is represented using a combination of 
unique shades of gray and unique colors as the 
normalized surface values progress from 0 to 255.
This is not a gradient display.  Rather, this 
display format is similar to a contour map where 
each distinct color traces out a constant level 
on the normalized surface being drawn.

Although the class is intended to be used by 
other programs to display surfaces produced by 
those programs, the class has a main method 
making it possible to run it in a stand-alone 
mode for testing.  When run as a stand-alone
program, the class produces and displays six 
individual surfaces with the lowest point in the 
upper left corner and the highest point in the 
lower right corner.  The scale strip is displayed
immediately below each surface. The six surfaces 
are stacked in the upper left corner of the 
screen.  (You must physically move the ones on 
the top to see the ones on the bottom.)  The 
stacking order of the surfaces from bottom to top
is based on display types in the order 0, 1, 2, 
3, 4, and 5.  The surfaces that are displayed
are 3D parabolas. Some of the surfaces show axes
and some do not.

The constructor defines an anonymous inner class
listener on the close button on the frame.  
Clicking the close button will terminate the 
program that uses an object of this class.

Tested using J2SE 5.0 and WinXP
 ************************************************/


class Surface extends Canvas{
	int dataWidth;
	int dataHeight;
	int blockSize;
	boolean axis;
	double[][] data;

	Canvas surface = null;
	Canvas scale = null;
	private int displayType;
	private int display ;
	private int style ;

//	Surface(Composite parent, int style){
//		super(parent,style);
//	}
	public void updateData(double[][] dataIn) {
		
		boolean logPlot = false;
		if(display == 3){
			displayType = 0;
			logPlot = true;
		}else if(display == 4){
			displayType = 1;
			logPlot = true;
		}else if(display == 5){
			displayType = 2;
			logPlot = true;
		}else if((display > 5) || (display < 0)){
			System.out.println(
					"DisplayType input error, terminating");
			System.exit(0);
		}//end if
		dataHeight = dataIn.length ;
		dataWidth = dataIn[0].length ;
		data = new double[dataHeight][dataWidth];
		for(int row = 0;row < dataHeight;row++){
			for(int col = 0;col < dataWidth;col++){
				data[row][col] = dataIn[row][col];
			}//end loop on col
		}//end loop on row

		if(logPlot){//Convert to log base 10.
			for(int row = 0;row < dataHeight;row++){
				for(int col = 0;col < dataWidth;col++){
					//Change the sign on negative values
					// before converting to log values.
					if(data[row][col] < 0){
						data[row][col] = -data[row][col];
					}//end if
					if(data[row][col] > 0){
						//Convert value to log base 10. Log
						// of 0 is undefined. Just leave it
						// at 0.
						data[row][col] = 
							Math.log10(data[row][col]);
					}//end if
				}//end col loop
			}//end row loop
		}//end if on logPlot

		//Force the data into the range from 0 to 255
		// regardless of whether or not it has been
		// converted to log values.
		scaleTheSurfaceData();
		
		if(surface!=null){
			surface.setSize(dataWidth * blockSize,310);
			this.pack();
//			this.setSize(dataWidth * blockSize,310);
		}
		

		//Add the plotted surface to center of the
		// Frame
//		add(BorderLayout.CENTER,surface);
		//Add the scale to bottom of Frame
//		add(BorderLayout.SOUTH,scale);
		//Cause the size of the Frame to be just
		// right to contain the two Canvas objects.
//		pack();

		//Set Frame cosmetics and make it visible.
//		setTitle("Copyright 2005 R.G.Baldwin");
//		setVisible(true);
//		System.out.println("[RsquareCanvas][Surface] redraw !");
//		redraw() ;
	}
	Surface(Composite parent, int style,double[][] dataIn,int blockSize,
			boolean axis,int display){
		super(parent,style);
		//Get and save several important values
		this.blockSize = blockSize;
		this.axis = axis;
		this.style = style;
		dataHeight = dataIn.length;
		dataWidth = dataIn[0].length;
		boolean logPlot = false;
		this.displayType = display;
		this.display = display ;
		this.setLayout(null) ;

		//Plot types 0, 1, and 2 with no log
		// conversion for display parameter
		// value  = 0, 1, or 2. This is the default
		// and no special code is required.
		//Plot types 0, 1, and 2 with log conversion
		// for display parameter value = 3, 4, or 5.
		

		//Make a copy of the input data array to
		// avoid damaging the original data.
		
		updateData(dataIn);

//		Establish the format based on the value of
		// the parameter named display.
		if(displayType == 0){
			//Create a type 0 Canvas object to draw the
			// surface on.  This is a gray scale 
			// display.
//			surface = new CanvasType0surface(this,style);
			//Create a Canvas object to draw the scale
			// on.
//			scale = new CanvasType0scale();
		}else if(displayType == 1){
			//Color Shift Plot
			System.out.println("Initializing color shift plot");
			surface = new CanvasType1surface(this,style);
//			scale = new CanvasType1scale(this,style);
		}else if(displayType == 2){
			//Color Contour Plot.
			surface = new CanvasType2surface(this,style);
//			scale = new CanvasType2scale(this,style);
		}//end if-else on display type
		
	}//end constructor
	//-------------------------------------------//

	double min;
	double max;
	//This method is used to scale the surface data
	// to force it to fit in the range from 0 to
	// 255.
	void scaleTheSurfaceData(){
		//Find the minimum surface value.
		min = Double.MAX_VALUE;
		for(int row = 0;row < dataHeight;row++){
			for(int col = 0;col < dataWidth;col++){
				if(data[row][col] < min)
					min = data[row][col];
			}//end col loop
		}//end row loop

		//Shift all values up or down to force new
		// minimum value to be 0.
		for(int row = 0;row < dataHeight;row++){
			for(int col = 0;col < dataWidth;col++){
				data[row][col] = data[row][col] - min;
			}//end col loop
		}//end row loop

		//Now get the maximum value of the shifted
		// surface values
		max = -Double.MAX_VALUE;
		for(int row = 0;row < dataHeight;row++){
			for(int col = 0;col < dataWidth;col++){
				if(data[row][col] > max)
					max = data[row][col];
			}//end col loop
		}//end row loop

		//Now scale all values to cause the new
		// maximum value to be 255.
		for(int row = 0;row < dataHeight;row++){
			for(int col = 0;col < dataWidth;col++){
				data[row][col] = 
					data[row][col] * 255/max;
			}//end col loop
		}//end row loop
	}//end scaleTheSurfaceData
	//-------------------------------------------//

	//main method for self-testing the class
	public static void main(String[] args){
		//Create the array of test data.
		int numberRows = 59;
		int numberCols = 159;
		double[][] data = 
			new double[numberRows][numberCols];
		int blockSize = 2;

		//Create a surface with a minimum at the
		// upper left corner and a maximum at the
		// lower right corner.  This surface is
		// a 3D parabola.
		for(int row = 0;row < numberRows;row++){
			for(int col = 0;col < numberCols;col++){
				int xSquare = col * col;
				int ySquare = row * row;
				data[row][col] = xSquare + ySquare;
			}//end col loop
		}//end row loop

		//Instantiate objects to display the test
		// data surface in six different formats on
		// top of one another in the upper left 
		// corner of the screen.  Represent each
		// surface value by a colored square that is
		// blockSize pixels on each side.  Draw a red
		// axis at the center of some of the
		// surfaces.
//		new Surface(data,blockSize,true,0);
//		new Surface(data,blockSize,false,1);    
//		new Surface(data,blockSize,true,2);
//		new Surface(data,blockSize,true,3);
//		new Surface(data,blockSize,false,4);    
//		new Surface(data,blockSize,true,5);
	}//end main
	//-------------------------------------------//

	int horizCenter;
	int vertCenter;
	//This helper method is used to find the
	// horizontal and vertical center of the
	// surface.  These values are used to locate
	// the red axes that are drawn on the surface.
	// Note that the returned values depend on
	// whether the dimensions of the surface are
	// odd or even.
	void getCenter(){
		if(dataWidth%2 == 0){//even
			horizCenter = 
				dataWidth * blockSize/2 + blockSize/2;
		}else{//odd
			horizCenter = dataWidth * blockSize/2;
		}//end else

		if(dataHeight%2 == 0){//even
			vertCenter = 
				dataHeight * blockSize/2 + blockSize/2;
		}else{//odd
			vertCenter = dataHeight * blockSize/2;
		}//end else
	}//end getCenter
	//-------------------------------------------//

	//Note that the following six classes are
	// inner classes. This makes it possible for
	// methods in the class to access instance
	// variables and methods of the containing
	// object.

	//This class is used to draw a gray scale
	// surface ranging from white at the high end
	// to black at the low end with a smooth
	// gradient in between.
//	class CanvasType0surface extends Canvas{
//	CanvasType0surface(){//constructor
//	//Set the size of the Canvas based on the
//	// size of the surface and the size of the
//	// square used to represent each value on
//	// the surface.
//	setSize(dataWidth * blockSize,
//	dataHeight * blockSize);
//	getCenter();
//	}//end constructor

//	//Override the paint method to draw the
//	// surface.
//	public void paint(Graphics g){
//	//Vary from white to black going from high
//	// to low.
//	Color color = null;
//	for(int row = 0;row < dataHeight;row++){
//	for(int col = 0;col < dataWidth;col++){
//	//Add in red, green, and blue in
//	// proportion to the value of the
//	// surface height.
//	int red = (int)data[row][col];
//	int green = red;
//	int blue = red;
//	//Compute the color value for the
//	// point on the surface.
//	color = new Color(red,green,blue);
//	//Set the color value.
//	g.setColor(color);
//	//Draw a square of the specified size
//	//in the specified color at the
//	// specified location.
//	g.fillRect(col * blockSize,
//	row * blockSize,
//	blockSize,
//	blockSize);
//	}//end col loop
//	}//end row loop

//	//If axis is true, draw red lines to form
//	// an origin at the center
//	if(axis){
//	g.setColor(Color.RED);
//	g.drawLine(0,vertCenter,2*horizCenter,
//	vertCenter);
//	g.drawLine(horizCenter,0,horizCenter,
//	2*vertCenter);
//	}//end if
//	}//end paint
//	}//end inner class CanvasType0surface
	//===========================================//

	//Note that this is an inner class.
	//This class is used to construct a color scale
	// that matches the color scheme used in the
	// class named CanvasType0surface.
//	class CanvasType0scale extends Canvas{
//	//Set the physical height of the scale strip
//	// in pixels.
//	int scaleHeight = 6 * blockSize;

//	CanvasType0scale(){//constructor
//	//Set the size of the Canvas based on the
//	// width of the surface and the size of the
//	// square used to represent each value on
//	// the surface.
//	setSize(dataWidth * blockSize,scaleHeight);
//	}//end constructor

//	//Override the paint method to draw the
//	// scale strip.
//	public void paint(Graphics g){
//	//Vary from white to black going from 255
//	// to 0.
//	Color color = null;
//	//Don't draw in top row. Leave it blank to
//	// separate the scale strip from the 
//	// drawing of the surface above it.
//	for(int row = 1;row < scaleHeight;row++){
//	for(int col = 0;col < dataWidth;col++){

//	//Compute the value of the scale
//	// surface.
//	int scaleValue = 255 * col/
//	(dataWidth - 1);

//	//See the class named 
//	// CanvasType0surface for explanatory
//	// comments regarding the following
//	// color algorithm.          
//	int red = scaleValue;
//	int green = red;
//	int blue = red;
//	color = new Color(red,green,blue);
//	g.setColor(color);
//	g.fillRect(col * blockSize,
//	row * blockSize,
//	blockSize,
//	blockSize);
//	}//end col loop
//	}//end row loop
//	}//end paint

//	}//end inner class CanvasType0scale
	//===========================================//

	//This class is used to draw a surface with the
	// colors ranging from blue at the low end
	// through aqua, green, and yellow to red at
	// the high end with a smooth gradient from 1
	// to 254.  The lowest point with a value of 0
	// is colored black.  The highest point with a
	// value of 255 is colored white.
	class CanvasType1surface extends Canvas{

		int channelToDraw = -1 ;
		CanvasType1surface(Composite parent, int style){//constructor   
			super(parent,style);
			//Set the size of the Canvas based on the
			// size of the surface and the size of the
			// square used to represent each value on
			// the surface.
//			setSize(dataWidth * blockSize,
//					dataHeight * blockSize);
			setSize(dataWidth * blockSize,
					310);
			getCenter();
			addMyPaintListener();
			addMouseListener(new MouseListener(){
				public void mouseDoubleClick(MouseEvent e) {	
				}

				public void mouseDown(MouseEvent e) {
					clickedChannel = channelToDraw ;
					for(TimeCourse graph : ((RsquareCanvas) ProjectStarter.getControlPanel().getCanvasRsquare()).getTimeCourseGraphs())
						graph.setStartChan(clickedChannel);
					
				}

				public void mouseUp(MouseEvent e) {
				}
				
			});
			addMouseMoveListener(new MouseMoveListener(){
				public void mouseMove(MouseEvent e) {
					int channelNumber = -1 ;
					int numberOfChans = ProjectStarter.getConf().getValueInt("numberOfChannels") ;
					for(int i=0;i<numberOfChans;i++)
						if(e.y<=(i+1)*dataHeight/numberOfChans*blockSize&&e.y>(i)*dataHeight/numberOfChans*blockSize)
							channelNumber = i ;
					System.out.println("[Surface - chan "+channelNumber+"] mouse "+e.x+","+e.y+" dataHeight "+dataHeight+" blockSize "+blockSize);
					if((channelToDraw!=channelNumber&&channelNumber!=-1)||channelToDraw!=clickedChannel){
						channelToDraw = channelNumber ;
						redraw() ;
					}
				}
			});
		}//end constructor
		private int clickedChannel;
		//Override the paint method to draw the
		// surface.
		public void draw(GC imageGC2){		
			Image image = null ; 
			if (image == null
					|| image.getBounds().width != getSize().x
					|| image.getBounds().height != getSize().y) {
				image =
					new Image(
							getParent().getDisplay(),
							getSize().x,
							getSize().y);
				setData("double-buffer-image2", image);
			}

			// Initializes the graphics context of the image. 
			GC imageGC = new GC(image);
			imageGC.setBackground(imageGC2.getBackground());
			imageGC.setForeground(imageGC2.getForeground());
			imageGC.setFont(imageGC2.getFont());
			
			System.out.println("[RsquareCanvas][Surface] PAINTING color shift plot");
			// Vary color as described in the comments
			// above.
			Color color = null;
			for(int row = 0;row < dataHeight;row++){
				for(int col = 0;col < dataWidth;col++){
					int red = 0;
					int green = 0;
					int blue = 0;

					if((int)data[row][col] == 255){
						red = green = blue = 255;//white
					}else if((int)data[row][col] == 0 ){
						red = green = blue = 0;//black

					}else if(((int)data[row][col] > 0) && 
							((int)data[row][col] <= 63)){
						int temp = 4 * ((int)data[row][col] 
						                               - 0);
						blue = 255;
						green = temp;

					}else if(((int)data[row][col] > 63) && 
							((int)data[row][col] <= 127)){
						int temp = 4 * ((int)data[row][col] 
						                               - 64);
						green = 255;
						blue = 255 - temp;

					}else if(((int)data[row][col] > 127) &&
							((int)data[row][col] <= 191)){
						int temp = 4 * ((int)data[row][col] 
						                               - 128);
						green = 255;
						red = temp;

					}else if(((int)data[row][col] > 191) &&
							((int)data[row][col] <= 254)){
						int temp = 4 * ((int)data[row][col] 
						                               - 192);
						red = 255;
						green = 255 - temp;

					}else{//impossible condition
						System.out.println(
								"Should not reach here.");
					System.exit(0);
					}//end else

						//Compute the color value for the
						// point on the surface.
						color = new Color(getParent().getDisplay(), red,green,blue);
					//Set the color value.
//						gc.setForeground(color);
						imageGC.setBackground(color);
						//Draw a square of the specified size
						// in the specified color at the
						// specified location.
						imageGC.fillRectangle(col * blockSize,
								row * blockSize,
								blockSize,
								blockSize);
				}//end col loop
			}//end row loop
			int dataVertEnd = dataHeight * blockSize + blockSize;
			imageGC.setForeground(getParent().getDisplay().getSystemColor(SWT.COLOR_RED));
			System.out.println("[Surface] Drawing channel "+channelToDraw+" data end = "+dataVertEnd);
			
			float scaleY = (float)(310-dataVertEnd)/(1024-0);
			int x = 0 ;
			if(channelToDraw==-1)
				channelToDraw = clickedChannel ;
			imageGC.drawString("Chan "+channelToDraw,10,dataVertEnd+5,true);
			for(int i=0;i<=data[channelToDraw].length-2;i++,x+=2){
				imageGC.drawLine(x,(int)(dataVertEnd-(data[channelToDraw*2][i]-1024)*scaleY),x+1,(int)(dataVertEnd-(data[2*channelToDraw][i+1]-1024)*scaleY));
			}
			//If axis is true, draw red lines to form
			// an origin at the center
			if(axis){
				imageGC.setForeground(getParent().getDisplay().getSystemColor(SWT.COLOR_RED));
				imageGC.drawLine(0,vertCenter,2*horizCenter,
						vertCenter);
				imageGC.drawLine(horizCenter,0,horizCenter,
						2*vertCenter);
			}//end if
			imageGC2.drawImage(image, 0, 0);
			imageGC.dispose();
			image.dispose();
		}
		public void addMyPaintListener(){
			addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				draw(e.gc);
			}
		});
		}
			
	}//end inner class CanvasType1surface
	//===========================================//

	//Note that this is an inner class.  This class
	// is used to construct a color scale that
	// matches the color scheme used in the class
	// named CanvasType1surface.
	class CanvasType1scale extends Canvas{
		int scaleHeight = 6 * blockSize;

		CanvasType1scale(Composite parent, int style){//constructor
			super(parent,style);
			//Set the size of the Canvas based on the
			// width of the surface and the size of the
			// square used to represent each value on
			// the surface.
			setSize(dataWidth * blockSize,scaleHeight);
		}//end constructor

		//Override the paint method to draw the
		// scale.
		public void paint(GC gc){
			//Vary from yellow to blue going from 255
			// to 0.
			Color color = null;
			for(int row = 1;row < scaleHeight;row++){
				for(int col = 0;col < dataWidth;col++){

					int scaleValue = 255 * col/(
							dataWidth - 1);
					// See the class named
					// CanvasType1surface for explanatory
					// comments regarding this color 
					// algorithm.
					int red = 0;
					int green = 0;
					int blue = 0;
					if(scaleValue == 255){
						red = green = blue = 255;//white
					}else if(scaleValue == 0 ){
						red = green = blue = 0;//black

					}else if((scaleValue > 0) && 
							(scaleValue <= 63)){
						scaleValue = 4 * (scaleValue - 0);
						blue = 255;
						green = scaleValue;

					}else if((scaleValue > 63) && 
							(scaleValue <= 127)){
						scaleValue = 4 * (scaleValue - 64);
						green = 255;
						blue = 255 - scaleValue;

					}else if((scaleValue > 127) && 
							(scaleValue <= 191)){
						scaleValue = 4 * (scaleValue - 128);
						green = 255;
						red = scaleValue;

					}else if((scaleValue > 191) && 
							(scaleValue <= 254)){
						scaleValue = 4 * (scaleValue - 192);
						red = 255;
						green = 255 - scaleValue;

					}else{//impossible condition
						System.out.println(
								"Should not reach here.");
					System.exit(0);
					}//end else

						color = new Color(getParent().getDisplay(), red,green,blue);
						gc.setBackground(color);
						gc.fillRectangle(col * blockSize,
								row * blockSize,
								blockSize,
								blockSize);
				}//end col loop
			}//end row loop
		}//end paint

	}//end inner class CanvasType1scale
	//===========================================//

	//This is a utility method used by the two
	// inner classes that follow.  The purpose of
	// this method is to establish a color palette
	// containing 23 distinct Colors and shades of
	// gray.  The values shown in comments
	// represent the values of red, green, and blue
	// for that specific color.
	Color[] getColorPalette(){
		//Note that the following is an initialized
		// 1D array of type Color.
		Color[] colorPalette = {
				new Color(getParent().getDisplay(),0,0,0),//             0,  0,  0
				new Color(getParent().getDisplay(),128,128,128),//            128,128,128
				new Color(getParent().getDisplay(),192,192,192),//      192,192,192
				new Color(getParent().getDisplay(),0,0,255),//              0,  0,255
				new Color(getParent().getDisplay(),100,100,255),//100,100,255
				new Color(getParent().getDisplay(),140,140,255),//140,140,255
				new Color(getParent().getDisplay(),175,175,255),//175,175,255
				new Color(getParent().getDisplay(),0,255,255),//              0,255,255
				new Color(getParent().getDisplay(),140,255,255),//140,255,255
				new Color(getParent().getDisplay(),0,255,0),//             0,255,  0
				new Color(getParent().getDisplay(),140,255,140),//140,255,140
				new Color(getParent().getDisplay(),200,255,200),//200,255,200
				new Color(getParent().getDisplay(),255,175,175),//            255,175,175
				new Color(getParent().getDisplay(),255,140,255),//255,140,255
				new Color(getParent().getDisplay(),255,0,255),//         255,  0,255
				new Color(getParent().getDisplay(),255,0,140),  //255,  0,140
				new Color(getParent().getDisplay(),255,0,0),//             255,  0,  0
				new Color(getParent().getDisplay(),255,100,0),//  255,100,  0
				new Color(getParent().getDisplay(),255,200,0),//          255,200,  0
				new Color(getParent().getDisplay(),255,225,0),//  255,225,  0
				new Color(getParent().getDisplay(),255,255,0),//          255,255,  0
				new Color(getParent().getDisplay(),255,255,150),//255,255,150
				new Color(getParent().getDisplay(),255,255,255)};//          255,255,255

		return colorPalette;
	}//end getColorPalette
	//===========================================//

	//Note that this is an inner class.
	//This class is used to draw a surface
	// representing the heights of the points on
	// the surface using the colors and shades of
	// gray defined in the color palette..
	class CanvasType2surface extends Canvas{

		CanvasType2surface(Composite parent, int style){//constructor
			super(parent,style);
			//Set the size of the Canvas based on the
			// size of the surface and the size of the
			// square used to represent each value on
			// the surface.
			setSize(dataWidth * blockSize,
					dataHeight * blockSize);
			getCenter();
		}//end constructor

		//Override the paint method to draw the
		// surface.
		public void paint(GC gc){
			Color[] colorPalette = getColorPalette();

			for(int row = 0;row < dataHeight;row++){
				for(int col = 0;col < dataWidth;col++){
					//Quantize the surface into a set of
					// levels where the number of levels is
					// equal to the number of colors in the
					// color palette.
					int quantizedData = (int)(Math.round(
							data[row][col]*(
									colorPalette.length-1)/255));
					//Set the color for this point to the
					// corresponding color from the
					// palette by matching the integer
					// value of the level and the index
					// value of the palette.
					gc.setBackground(colorPalette[
					                              quantizedData]);
					//Draw a square in the output image of
					// the specified color at the specified
					// location.
					gc.fillRectangle(col * blockSize,
							row * blockSize,
							blockSize,
							blockSize);
				}//end col loop
			}//end row loop

			//If axis is true, draw red lines to form
			// an origin at the center
			if(axis){
				gc.setForeground(getParent().getDisplay().getSystemColor(SWT.COLOR_RED));
				gc.drawLine(0,vertCenter,2*horizCenter,
						vertCenter);
				gc.drawLine(horizCenter,0,horizCenter,
						2*vertCenter);
			}//end if
		}//end paint
	}//end inner class CanvasType2surface
	//===========================================//

	//Note that this is an inner class.  This class
	// is used to construct a color scale that
	// matches the color scheme used in the class
	// named CanvasType2surface.
	class CanvasType2scale extends Canvas{
		int scaleHeight = 6 * blockSize;

		CanvasType2scale(Composite parent, int style){//constructor
			super(parent,style);
			//Set the size of the Canvas based on the
			// width of the surface and the size of the
			// square used to represent each value on
			// the surface.
			setSize(dataWidth * blockSize,scaleHeight);
		}//end constructor

		//Override the paint method to draw the
		// scale.
		public void paint(GC gc){
			Color[] colorPalette = getColorPalette();

			for(int row = 1;row < scaleHeight;row++){
				for(int col = 0;col < dataWidth;col++){

					//Get the value of the point on the
					// scale surface.
					double scaleValue = 
						255.0 * col/dataWidth;
					//See the class named
					// CanvasType2surface for an
					// explanation of this color
					// algorithm.
					int quantizedData = (int)(Math.round(
							scaleValue*(
									colorPalette.length-1)/255));
					gc.setBackground(colorPalette[
					                              quantizedData]);
					gc.fillRectangle(col * blockSize,
							row * blockSize,
							blockSize,
							blockSize);
				}//end col loop
			}//end row loop
		}//end paint

	}//end inner class CanvasType2scale
	//===========================================//

	

}//end outer class ImgMod29
