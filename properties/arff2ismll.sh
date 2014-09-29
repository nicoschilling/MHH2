#!/bin/bash

classname=de.ismll.converter.Arff2Ismll
sparse=false
progress=100000
binary=-1
while [ -n "$*" ]
do
   echo "This is parameter number $count $1"
	case $1 in
	-p)
#	-progress)
		progress=$2
		shift
	;;
	-i)
#	-input)
		input=$2
		shift
	;;
	-o)
#	-output)
		output=$2
		shift
	;;
	-s)
#	-sparse)
		sparse=true
	;;
	-b)
#	-binary)
		binary=1
		shift
	;;
	-h)
#	-help)
		./run.sh $classname --help
		exit 0
	;;
	-q)
#	-qsub)
		qsub=$2
		shift
	;;
	*)
		echo "Unknown command: $1"
	esac

   shift
done


if [ 'w'$input = 'w' ]; then
	echo "Error! Need input file. Please use switch -i, or -input!"
	exit 2
fi

if [ 'w'"$output" = 'w' ]; then
	echo "Error! Need output file. Please use switch -o, or -output!"
	exit 2
fi

export JAVA_DIR=/usr/java/latest
export JAVA_HOME=$JAVA_DIR

echo "output: $output"

if [ ! 'w'$qsub = 'w' ]; then
	./qrun.sh $qsub $classname input="$input" sparse=$sparse binary=$binary progress=$progress output="$output"  $* 
else
	cp="."
	for f in `ls *.jar -1`; do
	        cp=${cp}":"${f}
	done
	$JAVA_HOME/bin/java -classpath $cp $JAVA_OPTS de.ismll.console.Generic $classname input="$input" sparse=$sparse binary=$binary progress=$progress output="$output"  $*	
fi


