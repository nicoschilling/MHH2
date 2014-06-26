#!/bin/bash

. $1


for patient in ${patients[@]}; do
for split in ${splits[@]}; do
for what in bestAcc bestSampleDiff; do

modelDir=${datadir}/models/${experimentidentifier}/Proband${patient}/split${split}/${what}

re_stepSize=`cat ${modelDir}/parameters | awk ' BEGIN {FS="|"} ; {print $1}  ' `
re_reg0=`cat ${modelDir}/parameters | awk ' BEGIN {FS="|"} ; {print $2}  ' `
re_regw=`cat ${modelDir}/parameters | awk ' BEGIN {FS="|"} ; {print $3}  ' `
re_regv=`cat ${modelDir}/parameters | awk ' BEGIN {FS="|"} ; {print $4}  ' `
re_nrlatentfeatures=`cat ${modelDir}/parameters | awk ' BEGIN {FS="|"} ; {print $5}  ' `
re_windowextent=`cat ${modelDir}/parameters | awk ' BEGIN {FS="|"} ; {print $6}  ' `
re_smoothreg=`cat ${modelDir}/parameters | awk ' BEGIN {FS="|"} ; {print $7}  ' `
re_smoothwindow=`cat ${modelDir}/parameters | awk ' BEGIN {FS="|"} ; {print $8}  ' `


splitDirFull=${u_splitsdir}/Proband${patient}/split${split}
splitDirName=Proband${patient}split${split}

memm=$(get_mem_max /acogpr/meta/ 50 ${queues})
#echo $memm                                                                                                                                                                       \
                                                                                                                                                                            
qsub -l mem=${memm}00M ${qsubargs} \
-q ${queues} \
-N relearn-${splitDirName} \
-hold_jid bestModels${experimentidentifier}
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
runTable=run_${u_experimentidentifier}_test \
iterTable=iter_${u_experimentidentifier}_test \
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