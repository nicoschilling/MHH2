#!/bin/bash 

#Fill in the following fields to design a hyperSearch Script!!
#echo ${stepsizes[1]}

if [ ! 'x'$1 = 'x' ]; then
	# try to source in a configuration
	. $1
fi

if [ 'x'$stepsizes = 'x' ]; then
	echo "Error. The check for the \$stepsizes array failed. You probably have to configure some paramters before using this script ..."
	exit 1
fi

u_experimentkind=${experimentkind?Error: no experimentkind found!}
u_experimentidentifier=${experimentidentifier?Error: no experimentidentifier found!}

u_bootstrapclass=de.ismll.secondversion.ExtractPreprocessedData
u_splitsdir=${usesplitsdir?Error: no experimentsplitsdir found}

u_includeRD=${includeRD?Error: no includeRD found!}
u_laplacian=${laplacian?Error: no laplacian found!}

for window in   ${windowextents[@]}  ; do
# qsub ${qsuboptions} -q ${queues} -N ${u_experimentidentifier}-step-${stepSize}-lambda-${lambda}-windowExtent-${window}-smoothReg-${smoothReg}-smoothWindow-${smoothWindow} 
./run.sh ${u_bootstrapclass} \
splitFolder=${u_splitsdir}  \
annotator=sm \
maxIterations=${u_maxiterations} \
windowExtent=${window} \
columnSelector="33,166" \
laplacian=${u_laplacian} \
useValidation=true \
annotationBaseDir=${annotationsbasedir} \
includeRD=${u_includeRD} \
serializeTheData=/tmp
done
