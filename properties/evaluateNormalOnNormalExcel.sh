#!/bin/bash -e
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


basedir="/acogpr/mhh"
echo "Proband,Schluck,Modell,sampleDiffError(abs),predictedRestitutionTime,annotatedRestitutionTime,predictedAbsoluteAnnotationTime"
# /home/schilling/mhh/Results/Acid-on-Acid-Intra-Experiment/Proband-1/split-1/5/bestSampleDiff
resultFolder=${basedir}"/Results-battery/AllSwallows/"

for p in `seq 2 16`; do 
for schluck in $(seq 1 10); do

folder=${resultFolder}"/Proband${p}Schluck${schluck}/"

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

#echo "${p},${schluck},${what},${absCurrentDeviation},${currentRestitution},${trueAnnotation},${absolutePredictedAnnotation},${pmax},${predictedAnnotation},${groundtruthRestitution}"
echo "${p},${schluck},${what},${absCurrentDeviation},${currentRestitution},${groundtruthRestitution},${absolutePredictedAnnotation}"

done #what
done #p
done #schluck

exit 0

