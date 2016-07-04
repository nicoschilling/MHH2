#!/bin/bash

#
#  utility functions for calculating memory request and constraints between ulimit and Xmx
#
# one may use get_mem to 'forward-calculate' from Xmx to an ulimit value, and
# get_xmx to 'backward-calculate' from an ulimit value to an approximate suitable Xmx value.
#
# both functions take two arguments:
#  (1) the lookup file name
#  (2) the xmx / ulimit value to be sougt in the file
#
# see the document memory_in_sge for further information.
#

function get_mem() {
#       echo "calculating memory for $1 at a required minimum of $2"
        ret=""
        while read key value; do

        	#read the line
#        	read key value <<<$line
       		if [ $key -gt $2 ]; then
	        	ret=$value
                	break
        	fi

        done < $1 # of while

        if [ 'x'$ret = 'x' ]; then
                # fallback if no mem value was found.
                ret=130
        fi
        echo $((ret+12))
}

function get_mem_max() {
	local ret="000"
	for q in `echo $3 |  awk ' BEGIN{RS=","}; {  print $1 } '`; do
		c=$(get_mem $1/$q $2)
        	[[ $c -gt $ret ]] && ret=$c
	done
	echo "$ret"
}

function get_xmx() {
#       echo "calculating memory for $1 at a required minimum of $2"
        ret=""
        while read key value; do

        	#read the line
#        	read key value <<<$line
       		if [ $value -gt $2 ]; then
	        	ret=$key
                	break
        	fi

        #done < <(cat $1) # of while
	done < $1 # of while

        if [ 'x'$ret = 'x' ]; then
                # fallback if no mem value was found.
                ret=130
        fi
        echo $((ret-12))
}

function get_xmx_max() {
	local ret="000"
	for q in `echo $3 |  awk ' BEGIN{RS=","}; {  print $1 } '`; do
		c=$(get_xmx $1/$q $2)
        	[[ $c -gt $ret ]] && ret=$c
	done
	echo "$ret"
}


#mem=$(get_mem "$1" $2)
#echo mem is $mem

