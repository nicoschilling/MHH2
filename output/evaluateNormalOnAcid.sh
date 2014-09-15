#!/bin/bash -e

what="bestSampleDiff"

# /home/schilling/mhh/Results/Acid-on-Acid-Intra-Experiment/Proband-1/split-1/5/bestSampleDiff
resultFolder="/acogpr/mhh/Results/Normal-on-Acid-Inter-Experiment/"

for split in $(seq 1 8); do

if [ -f /tmp/deviationSplit${split} ];then
	rm /tmp/deviationSplit${split}
	rm /tmp/restitutionSplit${split}
fi

for folder in  `find "/acogpr/mhh/Results/Normal-on-Acid-Inter-Experiment/split-${split}/" -maxdepth 1 -mindepth 1 ` ; do

pmax=`cat ${folder}/${what}/pmax_sample`
predictedAnnotation=`cat ${folder}/${what}/end_sample`

trueAnnotation=`cat ${folder}/${what}/true_sample`
absolutePredictedAnnotation=`cat ${folder}/${what}/absolute_end_sample`

currentDeviation=$((${absolutePredictedAnnotation} - ${trueAnnotation}))

#compute the abs value:

absCurrentDeviation=` echo ${currentDeviation} | awk ' { if ($1>=0) {print $1} else {print $1*-1} } ' `

currentRestitution=$((${predictedAnnotation} - ${pmax}))

echo -e "${absCurrentDeviation} " >> /tmp/deviationSplit${split}
echo -e "${currentRestitution} " >> /tmp/restitutionSplit${split}

done
done

for split in $(seq 1 8); do

avgDeviationSplit[${split}]=`cat /tmp/deviationSplit${split} | awk '{sum+=$1} END {print sum/500}'`


avgRestitutionSplit[${split}]=`cat /tmp/restitutionSplit${split} | awk '{sum+=$1} END {print sum/500}'`

rm /tmp/deviationSplit${split}
rm /tmp/restitutionSplit${split}

echo -e "Abweichung Split${split}: \t ${avgDeviationSplit[${split}]} \t Restitution Split${split}: \t ${avgRestitutionSplit[${split}]} "

done
