#!/bin/bash -e

basedir="/acogpr/mhh"

resultFolder=${basedir}"/Results/Acid-on-Acid-Intra-Experiment/"
echo "Proband,Schluck,Modell,sampleDiffError(abs),predictedRestitutionTime,annotatedRestitutionTime,predictedAbsoluteAnnotationTime"

for proband in 1 2 ; do

deviation=0
restitution=0

for split in $(seq 1 5); do

for folder in  `find ${basedir}"/Results/Acid-on-Acid-Intra-Experiment/Proband-${proband}/split-${split}/" -maxdepth 1 -mindepth 1 ` ; do

schluck=`echo ${folder} | awk -F/ ' {print $NF } '`

for what in bestAcc bestSampleDiff; do

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

done # what
done # folder
done # split
done # proband

