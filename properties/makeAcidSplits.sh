#! /bin/bash -e

#This script should be executed in /home/schilling/mhh/bin
#This script creates folders in /home/schilling/mhh/Splits/IntraProband/ and subsequent directories!
#This script uses Data in /home/schilling/mhh/Probandi

# NOTE THAT WHEN USING RELATIVE PATHS WITH SYMBOLIC LINKS THE RELATIVE PATH IS RELATIVE TO THE PATH OF THE LOCATION WHERE THE LINK SHOULD BE!!!

proband=2



swallows=10

firstAcidSwallow=6

nrAcidSwallows=5

root=../../../../..

splitNumber=1

while [ ${splitNumber} -le $(( 1+${nrAcidSwallows}+${nrAcidSwallows}+${nrAcidSwallows} )) ]; do

	testdir=../Splits/AcidExperiment/Proband${proband}/split-${splitNumber}/test
	valdir=../Splits/AcidExperiment/Proband${proband}/split-${splitNumber}/validation
	traindir=../Splits/AcidExperiment/Proband${proband}/split-${splitNumber}/train

	mkdir -p ${testdir}
	mkdir -p ${valdir}
	mkdir -p ${traindir}

	# Assign TrainSwallows and Validation out of the Non-Acid Swallows
	
	validationFolder=$(( RANDOM%(${firstAcidSwallow}-1)+1 ))

	

	# Symbolic Link to the valfolders

	ln -s ${root}/AcidSwallows/Proband${proband}/${validationFolder} ${valdir}/${validationFolder}

	# Assign all non-acid Swallows as Training swallows except for the validation Swallow

	for trainFolder in $(seq 1 $(( (${firstAcidSwallow}-1) )) ); do

	   # echo "${trainFolder} und ${validationFolder}" 

	    if ( [ ${trainFolder} -ne ${validationFolder} ] ); then
		
		ln -s ${root}/AcidSwallows/Proband${proband}/${trainFolder} ${traindir}/${trainFolder}

	    fi

	done

	# Now, what to do with all the acid swallows? Depends on the splitNumber!!
	# First, all acid go to test, then for #AcidSwallows many splits, one goes to validation, then one goes to train!
	
	#Let all go to test

	if ( [ ${splitNumber} == 1 ] ); then

	    for testFolder in $(seq ${firstAcidSwallow} ${swallows}) ; do

		ln -s ${root}/AcidSwallows/Proband${proband}/${testFolder} ${testdir}/${testFolder}

	    done

	fi

	# One goes to validation

	if ( [ ${splitNumber} -gt 1 ] &&  [ ${splitNumber} -le $(( ${nrAcidSwallows}+1 )) ] ); then

	    #Compute a random additional Validation Folder, add the rest to test!

	    addValidationFolder=$(( RANDOM%(${nrAcidSwallows})+${firstAcidSwallow} ))

	    ln -s ${root}/AcidSwallows/Proband${proband}/${addValidationFolder} ${valdir}/${addValidationFolder}

	    for testFolder in $(seq ${firstAcidSwallow} ${swallows}) ; do

		if ( [ ${testFolder} -ne ${addValidationFolder} ] ); then

		    ln -s ${root}/AcidSwallows/Proband${proband}/${testFolder} ${testdir}/${testFolder}

		fi    

	    done

	fi

	#One goes to train

	if ( [ ${splitNumber} -gt $(( ${nrAcidSwallows}+1 )) ] && [ ${splitNumber} -le $(( ${nrAcidSwallows}+${nrAcidSwallows}+1 )) ] ); then

	    #Compute a random additional train Folder, add the rest to test!

	    addTrainFolder=$(( RANDOM%(${nrAcidSwallows})+${firstAcidSwallow} ))

	    ln -s ${root}/AcidSwallows/Proband${proband}/${addTrainFolder} ${traindir}/${addTrainFolder}

            for testFolder in $(seq ${firstAcidSwallow} ${swallows}) ; do

                if ( [ ${testFolder} -ne ${addTrainFolder} ] ); then

                    ln -s ${root}/AcidSwallows/Proband${proband}/${testFolder} ${testdir}/${testFolder}

                fi

	    done

        fi

	# One goes to train, one goes to validation, rest will be test

	if ( [ ${splitNumber} -gt $(( ${nrAcidSwallows}+${nrAcidSwallows}+1 )) ] && [ ${splitNumber} -le $(( ${nrAcidSwallows}+${nrAcidSwallows}+${nrAcidSwallows}+1 )) ] ); then

	    #Compute an additional Train Folder

	    addTrainFolder=$(( RANDOM%(${nrAcidSwallows})+${firstAcidSwallow} ))
	    
	    ln -s ${root}/AcidSwallows/Proband${proband}/${addTrainFolder} ${traindir}/${addTrainFolder}

	    #Compute an additional Validation Folder

	    addValFolder=$(( RANDOM%(${nrAcidSwallows})+${firstAcidSwallow} ))

	    while ( [ ${addValFolder} -eq ${addTrainFolder} ] ); do

		addValFolder=$(( RANDOM%(${nrAcidSwallows})+${firstAcidSwallow} ))

	    done

	    ln -s ${root}/AcidSwallows/Proband${proband}/${addValFolder} ${valdir}/${addValFolder}

	    #let the others go to test!!

	    for testFolder in $(seq ${firstAcidSwallow} ${swallows}) ; do

		if ( [ ${addValFolder} -ne ${testFolder} ] && [ ${addTrainFolder} -ne ${testFolder} ] ); then

		    ln -s ${root}/AcidSwallows/Proband${proband}/${testFolder} ${testdir}/${testFolder}

		fi
		
	    done

	fi


	let splitNumber=splitNumber+1


done