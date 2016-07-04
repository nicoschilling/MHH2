#!/bin/bash -e

basedir="/acogpr/mhh"

# /home/schilling/mhh/Results/Acid-on-Acid-Intra-Experiment/Proband-1/split-1/5/bestSampleDiff
resultFolder=${basedir}"/Results/Normal-on-Acid-Inter-Experiment/"
echo "Proband,Schluck,Modell,sampleDiffError(abs),predictedRestitutionTime,annotatedRestitutionTime,predictedAbsoluteAnnotationTime"

for split in $(seq 9 9); do

for folder in  `find ${basedir}"/Results/Normal-on-Acid-Inter-Experiment/split-${split}/" -maxdepth 1 -mindepth 1 ` ; do

for what in bestAcc bestSampleDiff; do 

kk=`echo ${folder} | awk -F/ ' {print $NF } '`
p=`echo ${kk} | awk -F- ' {print $1 } '`
schluck=`echo ${kk} | awk -F- ' {print $2 } '`

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

echo "${p},${schluck},${what},${absCurrentDeviation},${currentRestitution},${groundtruthRestitution},${absolutePredictedAnnotation}"

done #what
done #folder
done #split

