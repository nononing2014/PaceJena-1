
all: compile test clean

compile: 
		javac PaceJena.java OwlTypes.java 

test:
		java PaceJena "relations.owl" "classes.owl"

clean:
		find . -name "*.class"|xargs rm

