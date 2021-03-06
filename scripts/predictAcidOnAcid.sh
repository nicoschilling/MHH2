#!/bin/bash -e 

splitsFolder=/acogpr/mhh/Splits

for proband in $(seq 1 2); do
for s in `seq 1 5`; do
for what in bestAcc bestSampleDiff; do
#for what in bestSampleDiff; do

#qsub -N ApplyModel-P-${proband}-S-${swallow}

annotationFile=/acogpr/mhh/manual_annotations/AcidAnnotations/${proband}-sm.tsv
     
for i in `find "/${splitsFolder}/Acid-on-Acid-Intra-Experiment/Proband${proband}/split-${s}/test/"  -mindepth 1 -maxdepth 1`; do
#for i in  `find "/${splitsFolder}/Normal-on-Acid-Inter-Experiment/split-${s}/test/"  -mindepth 1 -maxdepth 1`; do

kk=`echo ${i} | awk -F/ ' {print $NF } '`

modelfolder="/acogpr/mhh/models/Acid-on-Acid-Intra-Experiment/Proband${proband}/split-${s}/${what}"
targetfolder="/acogpr/mhh/Results/Acid-on-Acid-Intra-Experiment/Proband-${proband}/split-${s}/${kk}/${what}/"
#mkdir -p ${targetfolder}

if [ -f ${modelfolder}/model_parameters ]; then
    mv ${modelfolder}/model_parameters ${modelfolder}/parameters
fi

    ./run.sh de.ismll.mhh.methods.ApplyModel \
inputfolder=file:/${i} \
model=file://${modelfolder} \
target=file://${targetfolder}

	# 7 == Parser.ANNOTATION_COL_RESTITUTIONSZEIT_SAMPLE (true annotation)
    ./run.sh de.ismll.secondversion.ExtractMatrixEntry col=7 row=$((${kk}-1)) \
file=${annotationFile} \
target=${targetfolder}/true_sample

done #test folders
done
done
done
