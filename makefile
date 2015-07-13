
all: compile test clean

compile: 
		javac PaceJena.java OwlTypes.java 

test:
		java PaceJena "trial.owl"

clean:
		find . -name "*.class"|xargs rm

