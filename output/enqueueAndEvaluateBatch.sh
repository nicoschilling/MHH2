#!/bin/bash

if [ 'x'$1 = 'x' ]; then
	echo "Need one argument: the specific experiment configuration to use"
	echo "abort"
	exit 1
fi 

# define default; they will maybe get overridden in the custom config.
enqueuejobs=y
bestmodel=n
relearn=n
extract=n

echo "Sourcing specific configuration from $1 ..."

. $1

echo "sourcing memory helper functions"

# 2014-05-07: needs to be a dot before to source
. java_memlib.sh

# enqueue hyperparameter search for normal swallows.

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

#echo ${u_splitsdir}

#exit 0

if [ 'x'${enqueuejobs} = 'xy' ]; then 
# requests -Xmx5000M per job 

for patient in ${patients[@]}; do
for split in ${splits[@]}; do
for stepSize in  ${stepsizes[@]} ;do
for reg0 in  ${reg0s[@]}   ; do
for fm_regW in ${fm_regWs[@]};do
for fm_regV in ${fm_regVs[@]}; do
for fm_numFactor in ${fm_numFactors[@]};do
for window in   ${windowextents[@]}  ; do
for smoothReg in  ${smoothregularizations[@]}  ; do
for smoothWindow in  ${smoothinwindows[@]}; do
for stDev in ${stDevs[@]}; do

splitDirFull=${u_splitsdir}/Proband${patient}/split${split}
splitDirName=Proband${patient}split${split}
# if model = lm  dont write all the fm stuff, if yes, do write it!

#memm=$(get_mem_max /acogpr/meta/ 50 ${queues})
#echo $memm
#qsub -l mem=${memm}00M ${qsubargs} \
qsub ${qsubargs} \
-q ${queues} \
-N ${u_experimentidentifier}-${splitDirName}-${modelFunction}-step-${stepSize}-reg0-${reg0}-regV-${reg0}-regW-${reg0}-nrF-${fm_numFactor}-windowExtent-${window}-smoothReg-${smoothReg}-smoothWindow-${smoothWindow} \
run.sh ${u_bootstrapclass} \
splitFolder=${splitDirFull} \
annotator=${u_annotator} \
maxIterations=${u_maxiterations} \
stepSize=${stepSize} \
reg0=${reg0} \
windowExtent=${window} \
columnSelector="${u_columnselector}" \
laplacian=${u_laplacian} \
useValidation=${u_useValidation} \
descentDirection=${u_descentDirection} \
modelFunction=${u_modelFunction} \
annotationBaseDir=${fullAnnotationsDir} \
includeRD=${u_includeRD} \
runTable=run_${u_experimentidentifier}_${modelFunction} \
iterTable=iter_${u_experimentidentifier}_${modelFunction} \
fm_regV=${reg0} \
fm_regW=${reg0} \
fm_numFactors=${fm_numFactor} \
smoothReg=${smoothReg} \
useDatabase=true \
stDev=${stDev} \
smoothWindow=${smoothWindow} >>submission.${modelFunction}-$(basename $0).log && echo -n "."

done
done
done
done
done
done
done 
done
done
done
done

echo "jobs submitted"
fi

# now execute something that finds the best model over a batch...

if [ 'x'${bestmodel} = 'xy' ]; then
qsub -l mem=1G -q ${queues} -N bestModels-${experimentidentifier}-for-${modelFunction} -hold_jid ${experimentidentifier}\*${modelFunction}\* ./bestModelBatch.sh $1
echo "bestModelBatch submitted"
fi


# relearn with best hyperparameters

if [ 'x'${relearn} = 'xy' ]; then

qsub -l mem=3G -q ${queues} -N relearnBatch${experimentidentifier} -hold_jid bestModels-${experimentidentifier}\*${modelFunction}\*  ./relearnBatch.sh $1

fi

# extract the test Quality

if [ 'x'${extract} = 'xy' ]; then

qsub -l mem=1G -q ${queues} -N extractTest${experimentidentifier} -hold_jid relearn-\* ./extractBatch.sh $1

fi


#if [ 'x'${bestmodel} = 'xy' ]; then
# submit script to determine best model for normal on normal. 
# wait for all jobs from above to complete before execute this.
#qsub  -l mem=1G -q ${queues} ${qsubargs} -N bestModel${experimentidentifier} -hold_jid ${experimentidentifier}\*  ./bestModelBatch.sh $1
#fi


# predict on all normal test swallows
# we need 7000M for running this.
#qsub -l mem=$(get_mem_max /acogpr/meta 70 ${queues})00M -q ${queues} -N predict${experimentidentifier} -hold_jid bestModel${experimentidentifier}  ./predictBatch.sh $1

# create an excel table.
# the configuration in the script results in an output to file logs/evaluateNormalOnNormal.txt
#qsub -l mem=1G -q ${queues} -N createExcelTable${experimentidentifier} -hold_jid predict${experimentidentifier} ./evaluateBatch.sh $1


