#!/bin/bash

classname=de.ismll.converter.Ismll2Arff
progress=100000
sparse=false
binary=false

while [ -n "$*" ]
do
   echo "This is parameter number $count $1"
	case $1 in
	-p)
		progress=$2		
		shift
	;;
	-i)
		input=$2
		shift
	;;
	-o)
		output=$2
		shift
	;;
	-s)
		sparse=true
	;;
	-b)
		binary=true
	;;
	-h)
		./run.sh $classname --help
		exit 0
	;;
	*)
		echo "Unknown command: $1"
	esac

   shift
done


if [ 'w'"$input" = 'w' ]; then
	echo "Error! Need input file. Please use switch -i, or -input!"
	exit 2
fi

if [ 'w'"$output" = 'w' ]; then
	echo "Error! Need output file. Please use switch -o, or -output!"
	exit 2
fi


./run.sh $classname input="$input" output="$output" progress=$progress binary=$binary sparse=$sparse "$@"



