/*========== MDLReader.java ==========
  MDLReader objects minimally contain an ArrayList<opCode> containing
  the opCodes generated when an mdl file is run through the java created
  lexer/parser, as well as the associated SymTab (Symbol Table).

  The provided methods are a constructor, and methods to print out the
  entries in the symbol table and command ArrayList.

  Your job is to go through each entry in opCodes and perform
  the required action from the list below:

  frames: set numFrames for animation

  basename: set baseName for animation

  vary: manipluate knob values between two given frames
        over a specified interval

  push: push a new origin matrix onto the origin stack

  pop: remove the top matrix on the origin stack

  move/scale/rotate: create a transformation matrix 
                     based on the provided values, then 
		     multiply the current top of the
		     origins stack by it.

  box/sphere/torus: create a solid object based on the
                    provided values. Store that in a 
		    temporary matrix, multiply it by the
		    current top of the origins stack, then
		    call draw_polygons.

  line: create a line based on the provided values. Store 
        that in a temporary matrix, multiply it by the
	current top of the origins stack, then call draw_lines.

  save: save the current screen with the provided filename

  =========================*/

import java.util.*;
import java.io.*;
import java.awt.Color;

import parser.*;
import parseTables.*;

public class  MDLReader {

    ArrayList<opCode> opcodes;
    SymTab symbols;
    ArrayList<LinkedList<VaryNode>> symTableTable;
    Set<String> symKeys;
    Stack<Matrix> origins;
    EdgeMatrix tmp;
    Frame f;
    int numFrames;
    String baseName;

    public MDLReader(ArrayList<opCode> o, SymTab s) {

	opcodes = o;
	symbols = s;
	symKeys = s.keySet();
	numFrames = 0;
	baseName = "frame";

	tmp = new EdgeMatrix();
	f = new Frame();
	Matrix m = new Matrix(4);
	m.ident();
	origins = new Stack<Matrix>();
	origins.push(m);
    }

    public void printCommands() {
	
	Iterator i = opcodes.iterator();

	while (i.hasNext()) {
	    System.out.println(i.next());
	}
    }

    public void printSymbols() {

	Iterator i;

	i = symKeys.iterator();
	System.out.println("Symbol Table:");

	while (i.hasNext()) {
	    String key = (String)i.next();
	    Object value=symbols.get(key);
	    System.out.println(""+key+"="+value);
	}
    }

    /*======== public void firstPass()) ==========
      Inputs:   
      Returns: 

      Checks the op ArrayList for any animation commands
      (frames, basename, vary)
      
      Should set num_frames and basename if the frames 
      or basename commands are present
      
      If vary is found, but frames is not, the entire
      program should exit.
      
      If frames is found, but basename is not, set name
      to some default value, and print out a message
      with the name being used.

      05/17/12 09:54:22
      jdyrlandweaver
      ====================*/
    public void firstPass() {
	Iterator<opCode> opIter = opcodes.iterator();
	boolean isFrame = opcodes.get(0) instanceof opFrames;
	
	if (opcodes.get(0) instanceof opFrames){
	    numFrames = ((opFrames)opcodes.get(0)).getNum();
	    if(opcodes.get(1) instanceof opBasename){
		baseName = ((opBasename)opcodes.get(1)).getName();
	    }
	    else{
		baseName = "A";
		System.out.println("Using default basename: A");
	    }
	    while(opIter.hasNext()){
		if(opIter.next() instanceof opVary)
		    break;
	    }	
	}
	else{
	    numFrames = 1;
	    while(opIter.hasNext()){
		if(opIter.next() instanceof opVary){
		    System.out.println("Error: contains VARY, but FRAMES is not first operation");
		    System.exit(0);
		}
	    }
	}
    }


    /*======== public void secondPass()) ==========
      Inputs:   
      Returns: 

      In order to set the knobs for animation, we need to keep
      a seaprate value for each knob for each frame. We can do
      this by using an array of linked lists. Each array index
      will correspond to a frame (eg. knobs[0] would be the first
      frame, knobs[2] would be the 3rd frame and so on).

      Sets this knobs array as the value of the public variable
      symTableTable.
      
      Each index should contain a linked list of VaryNodes, each
      node contains a knob name and a value (see VaryNode.java)

      Go through the opcode ArrayList, and when you find vary, go 
      from knobs[0] to knobs[frames-1] and add (or modify) the
      vary_node corresponding to the given knob with the
      appropirate value. 

      05/17/12 09:55:29
      jdyrlandweaver
      ====================*/
    public void secondPass() {
	Iterator<opCode> opIter= opcodes.iterator();
	ArrayList<LinkedList<VaryNode>> knobs =
	    new ArrayList<LinkedList<VaryNode>>();
	for (int i=0;i<numFrames;i++)
	    knobs.add(new LinkedList<VaryNode>());
	opCode oc;
	while(opIter.hasNext()){
	    oc = opIter.next();
	    if(oc instanceof opVary){
		opVary ov = (opVary)oc;
		int startFrame = ov.getStartFrame();
		int endFrame = ov.getEndFrame();
		double startVal = ov.getStartVal();
		double endVal = ov.getEndVal();
		String knob = ov.getKnob();
		VaryNode eg = new VaryNode(0,knob);
		if(!varyNodeIsPresent(knobs,eg)){
		    for(int i = 0; i < numFrames; i++){
			VaryNode temp = new VaryNode(0,knob);
			if(i<startFrame){
			    temp.setValue(startVal);
			}
			else if(i<endFrame){
			    double v;
			    double extent = ((i-startFrame+0.0) / (endFrame - startFrame + 0.0));
			    v = ((endVal - startVal) * extent) + startVal;
			    temp.setValue(v);
			}
			else{
			    temp.setValue(endVal);
			}
			knobs.get(i).add(temp);
		    }
		}
		else{//?
		    System.out.println("I HAVEN'T CODED THIS YET!!!");
		    //only change the zone specified
		}
	    }
	}
	symTableTable = knobs;
	//tidyPrint(knobs);
    }
    
    public boolean varyNodeIsPresent(ArrayList<LinkedList<VaryNode>> arr, VaryNode v){
	boolean out = false;
	for(int i = 0; i < arr.size(); i++){
	    if(arr.get(i).contains(v)){
		out = true;
		break;
	    }
	}
	return out;
    }

    public void tidyPrint(ArrayList<LinkedList<VaryNode>> arr){
	for(int i = 0; i < arr.size(); i++){
	    String s = "index: "+i+" ";
	    for(int j = 0; j < arr.get(i).size(); j++){
		s += "\t"+arr.get(i).get(j).toString()+"\n";
	    }
	    System.out.print(s);
	}
    }

    public void printKnobs() {

	Iterator i;
	int c = 0;

	i = symKeys.iterator();
	System.out.println("Knob List:");
	System.out.println( "ID\tNAME\tVALUE\n" );

	while (i.hasNext()) {
	    String key = (String)i.next();
	    Object value=symbols.get(key);
	    System.out.printf( "%d\t%s\t%6.2f\n", c++, key, value );
	}
    }


    /*======== public void subprocess()) ==========
      Inputs:   
      Returns: 

      Insert your interpreting code here

      you can use instanceof to check waht kind of op
      you are looking at:
      if ( oc instanceof opPush ) ...
	  
      you will need to typecast in order to get the
      operation specific data values

      If frames is not present in the source (and therefore 
      num_frames is 1, then process_knobs should be called.
      
      If frames is present, the enitre op array must be
      applied frames time. At the end of each frame iteration
      save the current screen to a file named the
      provided basename plus a numeric string such that the
      files will be listed in order, then clear the screen and
      reset any other data structures that need it.
      
      Important note: you cannot just name your files in 
      regular sequence, like pic0, pic1, pic2, pic3... if that
      is done, then pic1, pic10, pic11... will come before pic2
      and so on. In order to keep things clear, add leading 0s
      to the numeric portion of the name. If you use String.format
      (look it up online), you can use "%0xd" for this purpose. 
      It will add at most x 0s in front of a number, if needed, 
      so if used correctly, and x = 4, you would get numbers 
      like 0001, 0002, 0011, 0487

      04/23/12 09:52:32
      jdyrlandweaver
      ====================*/
    public void subProcess(int frame) {
	
	double knobVal, xval, yval, zval;
	
	Iterator<opCode> i = opcodes.iterator();
	opCode oc;
	origins = new Stack<Matrix>();
	Matrix m = new Matrix();
	m.ident();
	origins.push(m);

	while (i.hasNext()) {
	    
	    oc = i.next();
	    //String command = oc.getClass().getName();
	    
	    if ( oc instanceof opPush ) {
		
		m = origins.peek().copy();
		origins.push( m );
	    }
	    
	    else if ( oc instanceof opPop ) {
		origins.pop();
	    }
	    
	    else if ( oc instanceof opSphere ) {
		
		tmp.addSphere( ((opSphere)oc).getCenter()[0],
			       ((opSphere)oc).getCenter()[1],
			       ((opSphere)oc).getCenter()[2],
			       ((opSphere)oc).getR());

		tmp.matrixMult( origins.peek() );
		f.drawPolygons( tmp, new Color( 0, 255, 255 ) );
		tmp.clear();
	    }

	    else if ( oc instanceof opTorus ) {
		
		tmp.addTorus( ((opTorus)oc).getCenter()[0],
			      ((opTorus)oc).getCenter()[1],
			      ((opTorus)oc).getCenter()[2],
			      ((opTorus)oc).getr(), 
			      ((opTorus)oc).getR());
		tmp.matrixMult( origins.peek() );
		f.drawPolygons( tmp, new Color( 0, 255, 255 ) );
		tmp.clear();
	    }

	    else if ( oc instanceof opBox ) {
		
		tmp.addBox( ((opBox)oc).getP1()[0],
			    ((opBox)oc).getP1()[1],
			    ((opBox)oc).getP1()[2],
			    ((opBox)oc).getP2()[0],
			    ((opBox)oc).getP2()[1],
			    ((opBox)oc).getP2()[2] );

		tmp.matrixMult( origins.peek() );
		f.drawPolygons( tmp, new Color( 0, 255, 255 ) );
		tmp.clear();
	    }
	    //transformations:
	    else if(oc instanceof opTrans){
		String knob = ((opTrans)oc).getKnob();
		if(knob != null){
		    if ( oc instanceof opMove )
			opMoveRun((opMove)oc,knob,frame);
		    else if ( oc instanceof opScale )
			opScaleRun((opScale)oc,knob,frame);
		    else if ( oc instanceof opRotate )
			opRotateRun((opRotate)oc,knob,frame);
		}
		else{
		    if ( oc instanceof opMove )
			opMoveRun((opMove)oc);
		    else if ( oc instanceof opScale )
			opScaleRun((opScale)oc);
		    else if ( oc instanceof opRotate )
			opRotateRun((opRotate)oc);
		}
	    }	    
	    // else if ( oc instanceof opSave ) {
	    // 	f.save( ((opSave)oc).getName() );
	    // }
	}//end loop
	String name = baseName + String.format("%03d",frame)+".png";
	f.save("anim/"+name);
	Matrix mat = new Matrix(4);
	mat.ident();
	origins = new Stack<Matrix>();
	origins.push(mat);
	f = new Frame();
    }
    
    public void process(){
	firstPass();
	secondPass();
	
	if(numFrames > 1){
	    for(int i = 0; i < numFrames; i++)
		subProcess(i);
	}
	else{}
    }
    
    
    public void opMoveRun(opMove om){
	Matrix t = new Matrix(4);
	double xval = om.getValues()[0];
	double yval = om.getValues()[1];
	double zval = om.getValues()[2];
	
	t.makeTranslate( xval, yval, zval );
	t.matrixMult( origins.peek() );
	origins.pop();
	origins.push( t );
    }
    public void opMoveRun(opMove om, String knob, int frame){
	Matrix t = new Matrix(4);
	double xval = om.getValues()[0];
	double yval = om.getValues()[1];
	double zval = om.getValues()[2];

	int knobindex = symTableTable.get(frame).indexOf(new VaryNode(0,knob));
	if(knobindex != -1){
	    VaryNode v = symTableTable.get(frame).get(knobindex);
	    xval = xval * (v.getValue());
	    yval = yval * (v.getValue());
	    zval = zval * (v.getValue());
	    System.out.println("Moving: "+xval+", "+yval+", "+zval+"(knob="+v.getValue()+")");
	}
	else{System.out.println("Knob not exists (move)");}
		       
	t.makeTranslate( xval, yval, zval );
	t.matrixMult( origins.peek() );
	origins.pop();
	origins.push( t );
    }
    public void opScaleRun(opScale os){			
	Matrix t = new Matrix(4);
	double xval = os.getValues()[0];
	double yval = os.getValues()[1];
	double zval = os.getValues()[2];

	t.makeScale( xval, yval, zval );
	t.matrixMult( origins.peek() );
	origins.pop();
	origins.push( t );
    }
    public void opScaleRun(opScale os, String knob, int frame){
	Matrix t = new Matrix(4);
	double xval = os.getValues()[0];
	double yval = os.getValues()[1];
	double zval = os.getValues()[2];

	int knobindex = symTableTable.get(frame).indexOf(new VaryNode(0,knob));
	
	if(knobindex != -1){
	    VaryNode v = symTableTable.get(frame).get(knobindex);
	    xval = xval * (v.getValue());
	    yval = yval * (v.getValue());
	    zval = zval * (v.getValue());
	    System.out.println("Scaling: "+xval+", "+yval+", "+zval + "(knob="+v.getValue()+")");
	}
	else{
	    System.out.println("Knob not exists (scale)");
	}
	
	t.makeScale( xval, yval, zval );
	t.matrixMult( origins.peek() );
	origins.pop();
	origins.push( t );
    }
    public void opRotateRun(opRotate or){
	double angle = or.getDeg() * (Math.PI / 180);
	char axis = or.getAxis();
	Matrix t = new Matrix(4);

	if ( axis == 'x' )
	    t.makeRotX( angle );
	else if ( axis == 'y' )
	    t.makeRotY( angle );
	else
	    t.makeRotZ( angle );
	
	t.matrixMult( origins.peek() );
	origins.pop();
	origins.push( t );
    }
    public void opRotateRun(opRotate or, String knob, int frame){
	double angle = or.getDeg() * (Math.PI / 180);
	char axis = or.getAxis();
	Matrix t = new Matrix(4);

	int knobindex = symTableTable.get(frame).indexOf(new VaryNode(0,knob));
	if(knobindex != -1){
	    VaryNode v = symTableTable.get(frame).get(knobindex);
	    angle = angle * (v.getValue());
	    if ( axis == 'x' )
		t.makeRotX( angle );
	    else if ( axis == 'y' )
		t.makeRotY( angle );
	    else if (axis == 'z' )
		t.makeRotZ( angle );
	    System.out.println("Rotating:"+axis+"-axis, "+angle+"radians"+"(knob="+v.getValue()+")");    
	}
	else{
	    System.out.println("Knob not exists (rotate)");
	}
	
	t.matrixMult( origins.peek() );
	origins.pop();
	origins.push( t );
    }
	
    
}
