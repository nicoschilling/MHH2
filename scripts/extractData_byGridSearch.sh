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

u_bootstrapclass=${bootstrapclass?Error: no boopstrapclass found. Check properties-file!}
u_splitsdir=${usesplitsdir?Error: no experimentsplitsdir found}

u_descentDirection=${descentDirection?Error: no descentirection found!}
u_modelFunction=${modelFunction?Error: no modelFunction found!}
u_useValidation=${useValidation?Error: no useValidation found!}
u_annotator=${annotator?Error: no annotator found!}
u_includeRD=${includeRD?Error: no includeRD found!}
u_laplacian=${laplacian?Error: no laplacian found!}
u_columnselector=${columnselector?Error: non columnselector found!}

for stepSize in  ${stepsizes[@]} ;do 
for lambda in  ${lambdas[@]}   ; do
for window in   ${windowextents[@]}  ; do
for smoothReg in  ${smoothregularizations[@]}  ; do
for smoothWindow in  ${smoothinwindows[@]}; do 
# qsub ${qsuboptions} -q ${queues} -N ${u_experimentidentifier}-step-${stepSize}-lambda-${lambda}-windowExtent-${window}-smoothReg-${smoothReg}-smoothWindow-${smoothWindow} 
./run.sh ${u_bootstrapclass} \
splitFolder=${u_splitsdir}  \
annotator=${u_annotator} \
maxIterations=1 ${u_maxiterations} \
stepSize=${stepSize} \
lambda=${lambda} \
windowExtent=${window} \
columnSelector="${u_columnselector}" \
laplacian=${u_laplacian} \
useValidation=${u_useValidation} \
descentDirection=${u_descentDirection} \
modelFunction=${u_modelFunction} \
annotationBaseDir=${annotationsbasedir}/NormalAnnotations/ \
includeRD=${u_includeRD} \
runLapTable=run_${u_experimentidentifier} \
iterTable=iter_${u_experimentidentifier} \
smoothReg=${smoothReg} \
smoothWindow=${smoothWindow} 
done
done
done
done
done
