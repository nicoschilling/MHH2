#!/bin/bash
#for i in `awk '{ print $5"\t"$1 }' pmax_manuell`
#IFS=$'\011'
#for i in `cat pmax_manuell2`
file=`awk '{ print $5"\t"$1 }' pmax_manuell`
while read line
do 
  set -- $line
#  echo $2
 echo $2 > /home/mhhscratch/jobs/job${1}/pmax_manuell
done <pmax_manuell2
#done <$file


