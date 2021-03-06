#!/bin/bash -e 

splitsFolder=/acogpr/mhh/Splits

for s in `seq 9 9`; do
for what in bestAcc bestSampleDiff; do
#for what in bestSampleDiff; do

#qsub -N ApplyModel-P-${proband}-S-${swallow}

for i in `find "/${splitsFolder}/Normal-on-Acid-Inter-Experiment/split-${s}/test/"  -mindepth 1 -maxdepth 1`; do
#for i in  `find "/${splitsFolder}/Normal-on-Acid-Inter-Experiment/split-${s}/test/"  -mindepth 1 -maxdepth 1`; do

kk=`echo ${i} | awk -F/ ' {print $NF } '`
proband=`echo ${kk} | awk -F- ' {print $1 } '`
schluck=`echo ${kk} | awk -F- ' {print $2 } '`


annotationFile=/acogpr/mhh/manual_annotations/AcidAnnotations/${proband}-sm.tsv

modelfolder="/acogpr/mhh/models/Normal-on-Acid-Inter-Experiment/split-${s}/${what}"
targetfolder="/acogpr/mhh/Results/Normal-on-Acid-Inter-Experiment/split-${s}/${kk}/${what}/"
#mkdir -p ${targetfolder} Normal-on-Acid-Inter-Experiment

if [ -f ${modelfolder}/model_parameters ]; then
    mv ${modelfolder}/model_parameters ${modelfolder}/parameters
fi

    ./run.sh de.ismll.mhh.methods.ApplyModel \
inputfolder=file:/${i} \
model=file://${modelfolder} \
target=file://${targetfolder}

	# 7 == Parser.ANNOTATION_COL_RESTITUTIONSZEIT_SAMPLE (true annotation)
    ./run.sh de.ismll.secondversion.ExtractMatrixEntry col=7 row=$((${schluck}-1)) \
file=${annotationFile} \
target=${targetfolder}/true_sample

done #test folders
done
done
