#!/bin/bash

. $1

. java_memlib.sh

for patient in ${patients[@]}; do
for split in ${splits[@]}; do
for what in bestAcc bestSampleDiff; do


u_experimentkind=${experimentkind?Error: no experimentkind found!}
u_experimentidentifier=${experimentidentifier?Error: no experimentidentifier found!}

u_bootstrapclass=${bootstrapclass?Error: no boopstrapclass found. Check properties-file!}
u_splitsdir=${usesplitsdir?Error: no experimentsplitsdir found}

u_descentDirection=${descentDirection?Error: no descentirection found!}
u_modelFunction=${modelFunction?Error: no modelFunction found!}
u_useValidation=${useValidation?Error: no useValidation found!}
u_annotator=${annotator?Error: no annotator found!}
u_maxiterations=${maxiterations?Error: no maxiterations found!}
u_includeRD=${includeRD?Error: no includeRD found!}
u_laplacian=${laplacian?Error: no laplacian found!}
u_columnselector=${columnselector?Error: non columnselector found!}



modelDir=${datadir}/models/${experimentidentifier}/${modelFunction}/Proband${patient}/split${split}/${what}

re_stepSize=`cat ${modelDir}/parameters | awk ' BEGIN {FS="|"} ; {print $1}  ' `
re_reg0=`cat ${modelDir}/parameters | awk ' BEGIN {FS="|"} ; {print $2}  ' `
re_regw=`cat ${modelDir}/parameters | awk ' BEGIN {FS="|"} ; {print $3}  ' `
re_regv=`cat ${modelDir}/parameters | awk ' BEGIN {FS="|"} ; {print $4}  ' `
re_nrlatentfeatures=`cat ${modelDir}/parameters | awk ' BEGIN {FS="|"} ; {print $5}  ' `
re_windowextent=`cat ${modelDir}/parameters | awk ' BEGIN {FS="|"} ; {print $6}  ' `
re_smoothreg=`cat ${modelDir}/parameters | awk ' BEGIN {FS="|"} ; {print $7}  ' `
re_smoothwindow=`cat ${modelDir}/parameters | awk ' BEGIN {FS="|"} ; {print $8}  ' `


#echo "parameters used for ${what} on split ${split} of patient ${patient} are:"
#echo "StepSize: ${re_stepSize}"
#echo "Reg0: ${re_reg0}"
#echo "RegW: ${re_regw}"
#echo "NrLatentFeatures: ${re_nrlatentfeatures}"
#echo "Window Extent: ${re_windowextent}"
#echo "SmoothReg: ${re_smoothreg}"
#echo "SmoothWindow ${re_smoothwindow}"


splitDirFull=${usesplitsdir}/Proband${patient}/split${split}
splitDirName=Proband${patient}split${split}

#echo ${splitDirFull}

#exit 0

#memm=$(get_mem_max /acogpr/meta/ 50 ${queues})
                                                                                                                                                                            
qsub -l mem=5000M ${qsubargs} \
-q ${queues} \
-N relearn-${splitDirName}-${what}-for-${modelFunction} \
-hold_jid bestModels${experimentidentifier} \
run.sh ${u_bootstrapclass} \
splitFolder=${splitDirFull} \
annotator=${u_annotator} \
maxIterations=${u_maxiterations} \
stepSize=${re_stepSize} \
reg0=${re_reg0} \
windowExtent=${re_windowextent} \
columnSelector="${u_columnselector}" \
laplacian=${u_laplacian} \
useValidation="false" \
descentDirection=${u_descentDirection} \
modelFunction=${u_modelFunction} \
annotationBaseDir=${annotationsbasedir}/NormalAnnotations/ \
includeRD=${u_includeRD} \
runTable=run_${u_experimentidentifier}_${modelFunction}_test \
iterTable=iter_${u_experimentidentifier}_${modelFunction}_test \
fm_regV=${re_regv} \
fm_regW=${re_regw} \
fm_numFactors=${re_nrlatentfeatures} \
smoothReg=${re_smoothreg} \
useDatabase=true \
stDev=0.01 \
smoothWindow=${re_smoothwindow}  

done
done
done

echo "reLearn Jobs have been submitted"