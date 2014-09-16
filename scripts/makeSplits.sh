#!/bin/bash 

forWhat="Intra"

for proband in $(seq 1 10); do

    for split in $(seq 1 10); do

	mkdir ${forWhat}/Proband${proband}/Split${split}/train
	mkdir ${forWhat}/Proband${proband}/Split${split}/test
	mkdir ${forWhat}/Proband${proband}/Split${split}/validation

	for swallow in `cat ${forWhat}/Proband${proband}/Split${split}/test1` ; do

	    ln -s ../../../../../../ECDA-Swallows/Proband${proband}/Schluck${swallow}/ ${forWhat}/Proband${proband}/Split${split}/test/Schluck${swallow}

	    
	done


	for swallow in `cat ${forWhat}/Proband${proband}/Split${split}/train1` ; do

            ln -s ../../../../../../ECDA-Swallows/Proband${proband}/Schluck${swallow}/ ${forWhat}/Proband${proband}/Split${split}/train/Schluck${swallow}


        done
	
	for swallow in `cat ${forWhat}/Proband${proband}/Split${split}/val1` ; do

            ln -s ../../../../../../ECDA-Swallows/Proband${proband}/Schluck${swallow}/ ${forWhat}/Proband${proband}/Split${split}/validation/Schluck${swallow}


        done


done
done
