public class VaryNode {
    
    private double value;
    private String name;

    public VaryNode(double v, String n) {
	
	value = v;
	name = n;
    }

    public double getValue() {
	return value;
    }
    
    public String getName() {
	return name;
    }

    public void setValue( double v ) {
	value = v;
    }

    public boolean equals(Object o){
	if(o instanceof VaryNode)
	    return ((VaryNode)o).getName().equals(getName());
	else
	    return false;
    }

    public String toString(){
	String s = name+": "+value;
	return s;
    }
}
