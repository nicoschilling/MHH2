#!/bin/bash
#
#$ -cwd
#$ -S /bin/bash
#$ -j n
#$ -o logs/evaluateNormalOnNormal.txt
#$ -e logs/output.$JOB_NAME.$TASK_ID.err
#$ -p -100
#$ -m a
#$ -M busche@ismll.de
#$ -R y

# source the properties in
. $1


basedir="/acogpr/mhh"
echo "Proband,Schluck,Modell,sampleDiffError(abs),predictedRestitutionTime,annotatedRestitutionTime,predictedAbsoluteAnnotationTime"

resultFolder=${usepredictionsdir}"/"

for currentFolder in `find "/${resultFolder}/"  -mindepth 1 -maxdepth 1`; do

proband=`cat ${currentFolder}/proband`
schluck=`cat ${currentFolder}/id`

#folder=${resultFolder}"/Proband${p}Schluck${schluck}/"
folder=${currentFolder}

for what in bestSampleDiff bestAcc; do 

pmax=`cat ${folder}/${what}/pmax_sample`
predictedAnnotation=`cat ${folder}/${what}/end_sample`

trueAnnotation=`cat ${folder}/${what}/true_sample`
absolutePredictedAnnotation=`cat ${folder}/${what}/absolute_end_sample`

startData=$((${absolutePredictedAnnotation}-${predictedAnnotation}))
currentDeviation=$((${absolutePredictedAnnotation} - ${trueAnnotation}))

#compute the abs value:

absCurrentDeviation=` echo ${currentDeviation} | awk ' { if ($1>=0) {print $1} else {print $1*-1} } ' `

currentRestitution=$((${predictedAnnotation} - ${pmax}))
groundtruthRestitution=$((${trueAnnotation}-${startData}-${pmax}))

echo "${proband},${schluck},${what},${absCurrentDeviation},${currentRestitution},${groundtruthRestitution},${absolutePredictedAnnotation}"

done #what
done #of currentFolder

exit 0

