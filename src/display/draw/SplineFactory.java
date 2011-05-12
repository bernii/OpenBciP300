package display.draw;

public class SplineFactory
{
  /**
   * Create a Bezier spline based on the given control points.
   * The generated curve starts in the first control point and ends
   * in the last control point.
   * 
   * @param controlPoints  Control points of spline (x0,y0,z0,x1,y1,z1,...).
   * @param nParts         Number of parts to divide each leg into.
   * @return               Spline (x0,y0,z0,x1,y1,z1,...).
   */
  public static double[] createBezier (double[] controlPoints, int nParts)
  {
    Spline spline = new BezierSpline (controlPoints, nParts);
    return spline.generate();
  }


  
  /**
   * Create a cubic spline based on the given control points.
   * The generated curve starts in the first control point and ends
   * in the last control point.
   * 
   * @param controlPoints  Control points of spline (x0,y0,z0,x1,y1,z1,...).
   * @param nParts         Number of parts to divide each leg into.
   * @return               Spline (x0,y0,z0,x1,y1,z1,...).
   */
  public static double[] createCubic (double[] controlPoints, int nParts)
  {
    Spline spline = new CubicSpline (controlPoints, nParts);
    return spline.generate();
  }


  
  /**
   * Create a Catmull-Rom spline based on the given control points.
   * The generated curve starts in the first control point and ends
   * in the last control point.
   * Im addition, the curve intersects all the control points.
   * 
   * @param controlPoints  Control points of spline (x0,y0,z0,x1,y1,z1,...).
   * @param nParts         Number of parts to divide each leg into.
   * @return               Spline (x0,y0,z0,x1,y1,z1,...).
   */
  public static double[] createCatmullRom (double[] controlPoints, int nParts)
  {
    Spline spline = new CatmullRomSpline (controlPoints, nParts);
    return spline.generate();
  }


  
  /**
   * Testing the spline package.
   * 
   * @param args  Not used.
   */
  public static void main (String[] args)
  {
    double[] c = new double[12];
    c[0]  =   0.0;  // x0
    c[1]  =   0.0;  // y0
    c[2]  =   0.0;  // z0      
    
    c[3]  =   1.0;  // x1
    c[4]  =   1.0;  // y1
    c[5]  =   0.0;  // z1      

    c[6]  =   2.0;  // x2
    c[7]  =  -1.0;  // y2
    c[8]  =   0.0;  // z2      

    c[9]  =  10.0;  // x3
    c[10] =   0.0;  // y3
    c[11] =   0.0;  // z3      

    double[] spline1 = SplineFactory.createBezier (c,     20);
    double[] spline2 = SplineFactory.createCubic (c,      20);
    double[] spline3 = SplineFactory.createCatmullRom (c, 20);        

    System.out.println ("-- Bezier");
    for (int i = 0; i < spline1.length; i+=3)
      System.out.println (spline1[i] + "," + spline1[i+1] + "," + spline1[i+2]);
    
    System.out.println ("-- Cubic");
    for (int i = 0; i < spline2.length; i+=3)
      System.out.println (spline2[i] + "," + spline2[i+1] + "," + spline2[i+2]);

    System.out.println ("-- Catmull-Rom");
    for (int i = 0; i < spline3.length; i+=3)
      System.out.println (spline3[i] + "," + spline3[i+1] + "," + spline3[i+2]);
  }
}




/*
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public 
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program; if not, write to the Free 
 * Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, 
 * MA  02111-1307, USA.
 */
//package no.geosoft.cc.geometry.spline;



/**
 * An abstract class defining a general spline object.
 * 
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */   
abstract class Spline
{
  protected double controlPoints_[];
  protected int    nParts_;

  abstract double[] generate();
}




/*
 * (C) 2004 - Geotechnical Software Services
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public 
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program; if not, write to the Free 
 * Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, 
 * MA  02111-1307, USA.
 */
//package no.geosoft.cc.geometry.spline;




/**
 * A Bezier spline object. Use the SplineFactory class to create
 * splines of this type.
 * 
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */   
class BezierSpline extends Spline
{
  /**
   * Construct a bezier spline. Package local; Use the SplineFactory
   * to create splines of this type. The control points are used according
   * to the definition of Bezier splines.
   * 
   * @param controlPoints  Control points of spline (x0,y0,z0,x1,y1,z1,...)
   * @param nParts         Number of parts in generated spline.
   */
  BezierSpline (double controlPoints[], int nParts)
  {
    controlPoints_ = controlPoints;
    nParts_ = nParts;
  }


  
  /**
   * Generate this spline.
   * 
   * @return  Coordinates of the spline (x0,y0,z0,x1,y1,z1,...)
   */
  double[] generate()
  {
    if (controlPoints_.length < 9) {
      double[] copy = new double[controlPoints_.length];
      System.arraycopy (controlPoints_, 0, copy, 0, controlPoints_.length);
      return copy;
    }
    
    int n = controlPoints_.length / 3;
    int length = (n - 3) * nParts_ + 1;
    double spline[] = new double[length * 3];

    p (0, 0, controlPoints_, spline, 0);
      
    int index = 3;
    for (int i = 0; i < n - 3; i += 3) {
      for (int j = 1; j <= nParts_; j++) {
        p (i, j / (double) nParts_, controlPoints_, spline, index);
        index += 3;
      }
    }
      
    return spline;      
  }

    
    
  private void p (int i, double t, double cp[], double spline[], int index)
  {
    double x = 0.0;
    double y = 0.0;
    double z = 0.0;
      
    int k = i;
    for (int j = 0; j <= 3; j++) {
      double b = blend (j, t);
      
      x += b * cp[k++];
      y += b * cp[k++];
      z += b * cp[k++];
    }
      
    spline[index + 0] = x;
    spline[index + 1] = y;
    spline[index + 2] = z;    
  }



  private double blend (int i, double t)
  {
    if      (i == 0) return (1 - t) * (1 - t) * (1 - t);
    else if (i == 1) return 3 * t * (1 - t) * (1 - t);
    else if (i == 2) return 3 * t * t * (1 - t);
    else             return t * t * t;
  }
}




/*
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public 
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program; if not, write to the Free 
 * Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, 
 * MA  02111-1307, USA.
 */
//package no.geosoft.cc.geometry.spline;



/**
 * A cubic spline object. Use the SplineFactory class to create
 * splines of this type.
 * 
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */   
class CubicSpline extends Spline
{
  /**
   * Construct a cubic spline. Package local; Use the SplineFactory
   * to create splines of this type. The control points are used according
   * to the definition of cubic splines.
   * 
   * @param controlPoints  Control points of spline (x0,y0,z0,x1,y1,z1,...)
   * @param nParts         Number of parts in generated spline.
   */
  CubicSpline (double controlPoints[], int nParts)
  {
    initialize (controlPoints, nParts);
  }


  
  protected void initialize (double controlPoints[], int nParts)
  {
    nParts_ = nParts;

    // Endpoints are added three times to get them include in the
    // generated array    
    controlPoints_ = new double[controlPoints.length + 12];
    System.arraycopy (controlPoints, 0, controlPoints_, 6,
                      controlPoints.length);
    
    controlPoints_[0] = controlPoints_[6];
    controlPoints_[1] = controlPoints_[7];
    controlPoints_[2] = controlPoints_[8];
    
    controlPoints_[3] = controlPoints_[6];
    controlPoints_[4] = controlPoints_[7];
    controlPoints_[5] = controlPoints_[8];

    controlPoints_[controlPoints_.length - 3] = controlPoints_[controlPoints_.length - 9];
    controlPoints_[controlPoints_.length - 2] = controlPoints_[controlPoints_.length - 8];
    controlPoints_[controlPoints_.length - 1] = controlPoints_[controlPoints_.length - 7];

    controlPoints_[controlPoints_.length - 6] = controlPoints_[controlPoints_.length - 9];
    controlPoints_[controlPoints_.length - 5] = controlPoints_[controlPoints_.length - 8];
    controlPoints_[controlPoints_.length - 4] = controlPoints_[controlPoints_.length - 7];
  }
  

    
  /**
   * Generate this spline.
   * 
   * @return  Coordinates of the spline (x0,y0,z0,x1,y1,z1,...)
   */
  double[] generate()
  {
    int n = controlPoints_.length / 3;
    int length = (n - 3) * nParts_ + 1;
    double spline[] = new double[length * 3];

    p (2, 0, controlPoints_, spline, 0);
      
    int index = 3;
    for (int i = 2; i < n - 1; i++) {
      for (int j = 1; j <= nParts_; j++) {
        p (i, j / (double) nParts_, controlPoints_, spline, index);
        index += 3;
      }
    }
      
    return spline;      
  }

    

  private void p (int i, double t, double cp[], double spline[], int index)
  {
    double x = 0.0;
    double y = 0.0;
    double z = 0.0;
      
    int k = (i - 2) * 3;
    for (int j = -2; j <= 1; j++) {
      // TODO: Precompute blending matrix
      double b = blend (j, t);
      
      x += b * cp[k++];
      y += b * cp[k++];
      z += b * cp[k++];
    }
      
    spline[index + 0] = x;
    spline[index + 1] = y;
    spline[index + 2] = z;    
  }

    
    
  protected double blend (int i, double t)
  {
    if      (i == -2) return (((-t + 3) * t - 3) * t + 1) / 6;
    else if (i == -1) return (((3 * t - 6) * t) * t + 4) / 6;
    else if (i ==  0) return (((-3 * t + 3) * t + 3) * t + 1) / 6;
    else              return (t * t * t) / 6;
  }
}




/*
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public 
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program; if not, write to the Free 
 * Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, 
 * MA  02111-1307, USA.
 */
//package no.geosoft.cc.geometry.spline;



/**
 * A Catmull-Rom spline object. Use the SplineFactory class to create
 * splines of this type.
 * 
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */   
class CatmullRomSpline extends CubicSpline
{
  /**
   * Construct a Catmull-Rom spline. Package local; Use the SplineFactory
   * to create splines of this type. The control points are used according
   * to the definition of Catmull-Rom splines.
   * 
   * @param controlPoints  Control points of spline (x0,y0,z0,x1,y1,z1,...)
   * @param nParts         Number of parts in generated spline.
   */
  CatmullRomSpline (double controlPoints[], int nParts)
  {
    super (controlPoints, nParts);
  }


  
  protected void initialize (double controlPoints[], int nParts)
  {
    nParts_ = nParts;

    // Endpoints are added twice to force in the generated array
    controlPoints_ = new double[controlPoints.length + 6];
    System.arraycopy (controlPoints, 0, controlPoints_, 3,
                      controlPoints.length);
    
    controlPoints_[0] = controlPoints_[3];
    controlPoints_[1] = controlPoints_[4];
    controlPoints_[2] = controlPoints_[5];
    
    controlPoints_[controlPoints_.length - 3] = controlPoints_[controlPoints_.length - 6];
    controlPoints_[controlPoints_.length - 2] = controlPoints_[controlPoints_.length - 5];
    controlPoints_[controlPoints_.length - 1] = controlPoints_[controlPoints_.length - 4];
  }
  

  
  protected double blend (int i, double t)
  {
    if      (i == -2) return ((-t + 2) * t - 1) * t / 2;
    else if (i == -1) return (((3 * t - 5) * t) * t + 2) / 2;
    else if (i ==  0) return ((-3 * t + 4) * t + 1) * t / 2;
    else              return ((t - 1) * t * t) / 2;
  }
}