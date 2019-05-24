all: subs mdl.class

subs:
	$(MAKE) -C parseTables
	$(MAKE) -C parser

mdl.class: mdl.java MDLReader.class
	javac -classpath "." mdl.java

MDLReader.class: MDLReader.java Matrix.class EdgeMatrix.class Frame.class
	javac -cp "." MDLReader.java

Frame.class: Frame.java EdgeMatrix.class
	javac -cp "."  Frame.java

EdgeMatrix.class: EdgeMatrix.java Matrix.class
	javac -cp "." EdgeMatrix.java

Matrix.class: Matrix.java
	javac -cp "." Matrix.java


clean:
	$(MAKE) -C parseTables clean
	$(MAKE) -C parser clean
	rm *.class
	rm *~
