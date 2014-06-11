#!/bin/bash -e 

splitsFolder=/acogpr/mhh/Splits

modelBaseDir=/home/mhhscratch/models



for model in Acid-on-Acid-Intra-Experiment-Proband1 Acid-on-Acid-Intra-Experiment-Proband2 Normal-On-Acid-Inter-Experiment-bestAcc Normal-On-Acid-Inter-Experiment-bestSampleDiff  ; do
for jobDir in `find  "/home/mhhscratch/jobs/"  -mindepth 1 -maxdepth 1 -mtime -20`; do

modelfolder=${modelBaseDir}/${model}

job=`echo ${jobDir} | awk ' BEGIN {FS="/"} ; {print $5} ' `

targetfolder=/home/mhhscratch/targets/${model}/${job}

mkdir -p ${targetfolder}
#mkdir -p ${targetfolder} Normal-on-Acid-Inter-Experiment

if [ -f ${modelfolder}/model_parameters ]; then
    mv ${modelfolder}/model_parameters ${modelfolder}/parameters
fi

echo    ./run.sh de.ismll.mhh.methods.ApplyModel \
inputfolder=file:/${job} \
model=file://${modelfolder} \
target=file://${targetfolder}

done #test folders
#done
done
