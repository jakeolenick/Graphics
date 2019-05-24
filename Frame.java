/*========== Frame.java ==========
  Wrapper class for java's built in BufferedImage class.
  Allows use of java's DrawLine and image saving methods

  =========================*/

import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;

public class Frame {

    public static final int XRES = 500;
    public static final int YRES = 500;
    public static final int COLOR_VALUE = 255;

    private int maxx, maxy, maxcolor;
    private double[][] zbuffer;
    private BufferedImage bi;


    public Frame() {
	maxx = XRES;
	maxy = YRES;
	maxcolor = COLOR_VALUE;
	zbuffer = new double[XRES][YRES];
	for(int i=0;i<XRES;i++){  for(int j=0;j<YRES;j++) 
		zbuffer[i][j]= -Double.MAX_VALUE;
	}
	bi = new BufferedImage(maxx,maxy,BufferedImage.TYPE_BYTE_INDEXED);
    }

    public void clearScreen() {
	bi = new BufferedImage(maxx,maxy,BufferedImage.TYPE_BYTE_INDEXED);
    }	

    /*======== public void drawPolygons() ==========
      Inputs:  EdgeMatrix pm
               Color c 
      Returns: 
      
      Go through the point matrix as if it were a polygon matrix
      Call drawline in batches of 3s to create triangles.
 
      04/16/12 22:05:02
      jdyrlandweaver
      ====================*/
    public void drawPolygons(EdgeMatrix pm, Color c) {
	
	if ( pm.getLastCol() < 3 ) 
	    return;
	double botX; int botY;
	double midX;int midY;
	double topX;int topY;
	for (int i=0; i < pm.getLastCol() - 2; i+=3)  {
	    
	    if ( pm.calculateDot( i ) > 0 ) {

		int getYi = Math.round((float)pm.getY(i));
		int getYi1 = Math.round((float)pm.getY(i+1));
		int getYi2 = Math.round((float)pm.getY(i+2));

		drawLine( (int)pm.getX(i), getYi,pm.getZ(i),
			  (int)pm.getX(i+1), getYi1,pm.getZ(i+1), c);
		drawLine( (int)pm.getX(i+1), getYi1,pm.getZ(i+1),
			  (int)pm.getX(i+2), getYi2,pm.getZ(i+2), c);
		drawLine( (int)pm.getX(i+2), getYi2,pm.getZ(i+2),
			  (int)pm.getX(i), getYi,pm.getZ(i), c);

		if(getYi<getYi1 && getYi<getYi2){
		    botY=getYi; botX=pm.getX(i); 
		    if(getYi1<getYi2){
			midY=getYi1; midX=pm.getX(i+1);
			topY=getYi2; topX=pm.getX(i+2); 
		    }
		    else{
			midY = getYi2; midX = pm.getX(i+2);
			topY = getYi1; topX = pm.getX(i+1);
		    }
		}
		else if (getYi1<getYi && getYi1<getYi2){
		    botY = getYi1; botX = pm.getX(i+1);
		    if(getYi<getYi2){
			midY = getYi; midX = pm.getX(i); 
			topY = getYi2; topX = pm.getX(i+2);
		    }
		    else{
			midY = getYi2; midX = pm.getX(i+2); 
			topY = getYi; topX = pm.getX(i);
		    }
		}
		else{
		    botY = getYi2; botX = pm.getX(i+2); 
		    if(getYi < getYi1){
			midY = getYi; midX = pm.getX(i); 
			topY = getYi1; topX = pm.getX(i+1);
		    }
		    else{
			midY = getYi1; midX = pm.getX(i+1);
			topY = getYi; topX = pm.getX(i);
		    }
		}
		Random rand = new Random();
		float r = rand.nextFloat();
		float g = rand.nextFloat();
		float b = rand.nextFloat();
		Color tmp = new Color(r,g,b);
		double d0 = (topX-botX) / (topY-botY);		    
		double d1 = (midX-botX) / (midY-botY);
		double x0 = botX;
		double x1 = botX;
		double z0 = zbuffer[(int)x0][botY];
		double z1 = zbuffer[(int)x1][botY];
		if(botY != topY){
		    int y;
		    for(y = botY; y<midY; y += 1){
			z0 = zbuffer[(int)x0][y];
			z1 = zbuffer[(int)x1][y];
			drawLine((int)x0,y,z0,(int)x1,y,z1,tmp);
			x0 += d0;
			x1 += d1;
		    }
		    d1 = (topX-midX) / (topY-midY);
		    x1 = midX;
		    for(y = midY; y<topY; y += 1){
			z0 = zbuffer[(int)x0][y];
			z1 = zbuffer[(int)x1][y];
			drawLine((int)x0,y,z0,(int)x1,y,z1,tmp);
			x0 += d0;
			x1 += d1;
		    }
		}
		else{
		    System.out.println("flat polygon!");
		}

	    }
	}
    }

    /*======== public void drawLines() ==========
      Inputs:  PointMatrix pm
      Color c 
      Returns: 
      calls drawLine so that it draws all the lines within PointMatrix pm
      ====================*/
    public void drawLines(EdgeMatrix pm, Color c) {
	
	for (int i=0; i < pm.getLastCol() - 1; i+=2) 
	    drawLine( (int)pm.getX(i), (int)pm.getY(i), pm.getZ(i),
		      (int)pm.getX(i+1), (int)pm.getY(i+1), pm.getZ(i+1),c);
    }	


    /*======== public void drawLine() ==========
      Inputs:  int x0
      int y0
      int x1
      int y1
      Color c 
      Returns: 
      Wrapper for java's built in drawLine routine
      ====================*/
    public void drawLine(int x0, int y0, double z0,
			 int x1, int y1, double z1, Color c) {

	boolean isVisible = (zbuffer[x0][y0]<=z0+1) && (zbuffer[x1][y1]<=z1+1);

	for(int y = y0; y<y1;y++){
	    double extent = (y-y0 + 0.0) / (y1-y0 + 0.0);
	    int x = (int)(x0 + (x1-x0)*extent);
	    double z = z0 + (z1-z0)*extent;
	    if(zbuffer[x][y] < z)
		zbuffer[x][y] = z;
	}
	if (isVisible){
	    Graphics2D g = bi.createGraphics();
	    g.setColor(c);
	    g.drawLine(x0,y0,x1,y1);
	}
	else{
	    // System.out.println("zbuf(x0,y0),z0,zbuf(x1)(y1),z1: " + 
	    // 		       zbuffer[x0][y0]+","+z0+","+zbuffer[x1][y1]+","+z1);
	}
    }	
   
    /*======== public void save() ==========
      Inputs:  String filename 
      Returns: 
      saves the bufferedImage as a png file with the given filename
      ====================*/
    public void save(String filename) {
	try {
	    File fn = new File(filename);
	    ImageIO.write(bi,"png",fn);
	}
	catch (IOException e) {}
    }

}
