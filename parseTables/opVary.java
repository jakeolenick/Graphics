package parseTables;

import parseTables.*;

public class opVary extends opCode
{
    private String knob;
    private int startframe,endframe;
    private double startval, endval;

    public opVary(String knob, int startframe, int endframe,
		  double startval, double endval)
    {
	this.knob=knob;this.startframe=startframe;this.endframe=endframe;
	this.startval=startval;this.endval=endval;
    }
    public int getStartFrame(){ return startframe;}
    public int getEndFrame(){ return endframe;}
    public double getStartVal(){ return startval;}
    public double getEndVal(){ return endval;}
    public String getKnob(){return knob;}
    public String toString()
    {
	return "Vary: "+knob+" - "+startframe+" to "+endframe+
	    " values - "+startval+" to "+endval;
    }
}
