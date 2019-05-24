/*========== Matrix.java ==========
  Matrix will hold a 2-d array of doubles and have a default size of 4x4.
  Handles basic matrix maintenence and math.
  Creates transformation matricies for tralation, scale and rotate
=========================*/

import java.io.*;
import java.util.*;

public class Matrix {

    public static final int DEFAULT_SIZE = 4;
    protected double[][] m;

    /*===========Constructors================
      Default constructor creates a 4x4 matrix
      Second constructor creates a 4xN matrix
    */
    public Matrix() {
	m = new double[DEFAULT_SIZE][DEFAULT_SIZE];
    }
    public Matrix(int c) {
	m = new double[DEFAULT_SIZE][c];
    }


    /*========     public double calculateDot() ==========
      Inputs:   int i  
      Returns:  The dot product of a surface normal and
                a view vector 
   
      calculates the dot product of the surface normal to
      triangle points[i], points[i+1], points[i+2] and a 
      view vector (use <0, 0, -1> to start.      


      04/17/12 17:10:13
      jonalf
      ====================*/
    public double calculateDot( int i ) {
	
	double ax, ay, az, bx, by, bz;
	double[] normal;
	double vx, vy, vz;
	double dot, mag;

	//get vectors form the points
	ax = m[0][ i + 1 ] - m[0][ i ];
	ay = m[1][ i + 1 ] - m[1][ i ];
	az = m[2][ i + 1 ] - m[2][ i ];

	bx = m[0][ i + 2 ] - m[0][ i ];
	by = m[1][ i + 2 ] - m[1][ i ];
	bz = m[2][ i + 2 ] - m[2][ i ];

	//surmace normal = A x B
	normal = crossProduct( ax, ay, az, bx, by, bz );

	//normalize normal
	mag = Math.sqrt( normal[0] * normal[0] +
			 normal[1] * normal[1] +
			 normal[2] * normal[2] );
	normal[0] = normal[0] / mag;
	normal[1] = normal[1] / mag;
	normal[2] = normal[2] / mag;

	//set up view vector
	vx = 0;
	vy = 0;
	vz = -1;

	//calculate dot product
	dot = normal[0] * vx + normal[1] * vy + normal[2] * vz;

	return dot;
    }
    
    /*========  public double[] crossProduct() ==========
      Inputs:   double ax
                double ay
		double az
		double bx
		double by
		double bz  
      Returns:  A double arry of size 3 representing the 
                cross product of <ax, ay, az> and <bx, by, bz>
      
      04/17/12 17:07:07
      jonalf
      ====================*/
    public double[] crossProduct( double ax, double ay, double az, 
				     double bx, double by, double bz ) {

	double[] normal = new double[3];
	
	normal[0] = ay * bz - az * by;
	normal[1] = az * bx - ax * bz;
	normal[2] = ax * by - ay * bx;

	return normal;
    }


    /*===========grow================
      Increase the number of columns in a matrix by 10
      You can change the growth factor as you see fit
    */
    public void grow() {

	double[][] n = new double[m.length][m[0].length + 10];
	for (int r=0; r<m.length; r++)
	    for (int c=0; c<m[r].length; c++)
		n[r][c] = m[r][c];
	
	m = n;
    }

    /*======== public void clear() ==========
      Inputs:  
      Returns: 
      Sets every entry in the matrix to 0
      ====================*/
    public void clear() {

	for (int i=0; i<m.length; i++) 
	    for (int j=0; j<m[i].length; j++) 
		m[i][j] = 0;
    }		

    /*===========ident================
      Turns this matrix into the indentity matrix
      You may assume the calling Matrix is square
    */
    public void ident() {
	
	for (int i=0; i<m.length; i++) {
	    for (int j=0; j<m[i].length; j++) {
		
		if (i==j)
		    m[i][j] = 1;
		else
		    m[i][j] = 0;
	    }
	}
    }

    /*===========scalarMult================
      Inputs:  double x
      
      multiply each element of the calling matrix by x
    */
    public void scalarMult( int s ) {

	for (int i=0; i<m.length; i++) 
	    for (int j=0; j<m[i].length; j++) 
		m[i][j] = m[i][j] * s;
    }		

    /*===========matrixMult================
      Multply matrix n by the calling matrix, modify
      the calling matrix to store the result.
      
      eg.
      In the call a.matrixMult(n), n will remain the same
      and a will now be the product of n * a
    */
    public void matrixMult( Matrix n ) {

	double[][] tmp = new double[4][1];

	for (int c=0; c<m[0].length; c++) {
	    for (int r=0; r<4; r++) 
		tmp[r][0] = m[r][c];

	    for (int r=0; r<4; r++)
		m[r][c] = n.m[r][0] * tmp[0][0] +
		    n.m[r][1] * tmp[1][0] +
		    n.m[r][2] * tmp[2][0] +
		    n.m[r][3] * tmp[3][0];
	}
    }
   
    /*===========copy================
      Create and return new matrix that is a duplicate 
      of the calling matrix
    */
    public Matrix copy() {

	Matrix n = new Matrix( m[0].length );
	for (int r=0; r<m.length; r++)
	    for (int c=0; c<m[r].length; c++)
		n.m[r][c] = m[r][c];

	return n;
    }

    /*===========toString================
      Crate a readable String representation of the 
      calling matrix.
    */
    public String toString() {

	String s = "";
	for (int i=0; i<m.length; i++) {
	    for (int j=0; j<m[i].length; j++)
		s = s + m[i][j] + " ";
	    s = s + "\n";
	}
	return s;
    }

    /*===========MakeTranslate================
      Turns the calling matrix into the appropriate
      translation matrix using x, y, and z as the translation
      offsets.
    */
    public void makeTranslate(double x, double y, double z) {

	ident();
	m[0][3] = x;
	m[1][3] = y;
	m[2][3] = z;
    }
    
    /*===========MakeScale================
      Turns the calling matrix into the appropriate scale
      matrix using x, y and z as the scale factors.
    */
    public void makeScale(double x, double y, double z) {

	ident();
	m[0][0] = x;
	m[1][1] = y;
	m[2][2] = z;
    }

    /*=========== MakeRotX ================
      Turns the calling matrix into the appropriate rotation
      matrix using theta as the angle of rotation and X
      as the axis of rotation.
    */
    public void makeRotX(double theta) {
	
	ident();
	m[1][1] = Math.cos( theta );
	m[1][2] = -1 * Math.sin( theta );
	m[2][1] = Math.sin( theta );
	m[2][2] = Math.cos( theta );
    }

    /*=========== MakeRotY ================
      Turns the calling matrix into the appropriate rotation
      matrix using theta as the angle of rotation and Y
      as the axis of rotation.
    */
    public void makeRotY(double theta) {

	ident();
	m[0][0] = Math.cos( theta );
	m[0][2] = -1 * Math.sin( theta );
	m[2][0] = Math.sin( theta );
	m[2][2] = Math.cos( theta );
    }

    /*=========== MakeRotZ ================
      Turns the calling matrix into the appropriate rotation
      matrix using theta as the angle of rotation and axis
      as the axis of rotation.
    */
    public void makeRotZ(double theta) {

	ident();
	m[0][0] = Math.cos( theta );
	m[0][1] = -1 * Math.sin( theta );
	m[1][0] = Math.sin( theta );
	m[1][1] = Math.cos( theta );
    }
    
    /*======== public void makeHermite()) ==========
      Inputs:   
      Returns: 
      
      Turn the calling matrix into a hermite coeficient
      generating matrix

      03/09/12 18:15:58
      jonalf
      ====================*/
    public void makeHermite() {
	
	ident();
	m[0][0] = 2;
	m[0][1] = -2;
	m[0][2] = 1;
	m[0][3] = 1;

	m[1][0] = -3;
	m[1][1] = 3;
	m[1][2] = -2;
	m[1][3] = -1;

	m[3][0] = 1;
	m[3][3] = 0;
    }

    /*======== public void makeBezier()) ==========
      Inputs:   
      Returns: 

      Turn the calling matrix into a bezier coeficient
      generating matrix

      03/09/12 18:15:00
      jonalf
      ====================*/
    public void makeBezier() {
	
	ident();
	m[0][0] = -1;
	m[0][1] = 3;
	m[0][2] = -3;
	m[0][3] = 1;

	m[1][0] = 3;
	m[1][1] = -6;
	m[1][2] = 3;
	m[1][3] = 0;

	m[2][0] = -3;
	m[2][1] = 3;
	m[2][2] = 0;

	m[3][0] = 1;
	m[3][3] = 0;
    }

    /*======== public void generateHermiteCoefs() ==========
      Inputs:  double p1
               double p2
	       double p3
	       double p4 
      Returns: 
      
      Turns the calling matrix into a matrix that provides the 
      coefiecients required to generate a Hermite curve given 
      the values of the 4 parameter coordinates.

      03/09/12 18:17:16
      jonalf
      ====================*/
    public void generateHermiteCoefs(double p1, double p2, 
				     double p3, double p4) {

	Matrix mult = new Matrix(4);

	m[0][0] = p1;
	m[1][0] = p3;
	m[2][0] = p2 - p1;
	m[3][0] = p4 - p3;

	mult.makeHermite();
	matrixMult( mult );
    }

    /*======== public void generateBezierCoefs() ==========
      Inputs:  double p1
               double p2
	       double p3
	       double p4 
      Returns: 
      
      Turns the calling matrix into a matrix that provides the 
      coefiecients required to generate a Bezier curve given 
      the values of the 4 parameter coordinates.

      03/09/12 18:17:16
      jonalf
      ====================*/
    public void generateBezierCoefs(double p1, double p2, 
				    double p3, double p4) {

	Matrix mult = new Matrix(4);

	m[0][0] = p1;
	m[1][0] = p2;
	m[2][0] = p3;
	m[3][0] = p4;

	mult.makeBezier();
	matrixMult( mult );
    } 


}
