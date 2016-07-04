#!/bin/bash -e

what="bestSampleDiff"

basedir="/acogpr/mhh"

# /home/schilling/mhh/Results/Acid-on-Acid-Intra-Experiment/Proband-1/split-1/5/bestSampleDiff
resultFolder=${basedir}"/Results/Acid-on-Acid-Intra-Experiment/"



for proband in 1 2 ; do

deviation=0
restitution=0

for split in $(seq 1 5); do

for folder in  `find "/acogpr/mhh/Results/Acid-on-Acid-Intra-Experiment/Proband-${proband}/split-${split}/" -maxdepth 1 -mindepth 1 ` ; do

pmax=`cat ${folder}/${what}/pmax_sample`
predictedAnnotation=`cat ${folder}/${what}/end_sample`

trueAnnotation=`cat ${folder}/${what}/true_sample`
absolutePredictedAnnotation=`cat ${folder}/${what}/absolute_end_sample`

currentDeviation=$((${absolutePredictedAnnotation} - ${trueAnnotation}))

#compute the abs value:

absCurrentDeviation=` echo ${currentDeviation} | awk ' { if ($1>=0) {print $1} else {print $1*-1} } ' `

currentRestitution=$((${predictedAnnotation} - ${pmax}))

echo -e "${absCurrentDeviation} " >> /tmp/deviationProband${proband}
echo -e "${currentRestitution} " >> /tmp/restitutionProband${proband}

done
done
done


avgDeviationProband1=`cat /tmp/deviationProband1 | awk '{sum+=$1} END {print sum/250}'`
avgDeviationProband2=`cat /tmp/deviationProband2 | awk '{sum+=$1} END {print sum/250}'`

avgRestitutionProband1=`cat /tmp/restitutionProband1 | awk '{sum+=$1} END {print sum/250}'`
avgRestitutionProband2=`cat /tmp/restitutionProband2 | awk '{sum+=$1} END {print sum/250}'`

rm /tmp/deviationProband1
rm /tmp/deviationProband2

rm /tmp/restitutionProband1
rm /tmp/restitutionProband2


echo -e "Abweichung Proband1: \t ${avgDeviationProband1} \t Restitution Proband1: \t ${avgRestitutionProband1} "
echo -e "Abweichung Proband2: \t ${avgDeviationProband2} \t Restitution Proband2: \t ${avgRestitutionProband2} "
