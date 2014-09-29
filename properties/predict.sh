#!/bin/bash -e 

splitsFolder=/acogpr/mhh/Splits

for proband in $(seq 1 1); do
for s in `seq 1 1`; do
for swallow in $(seq 1 1); do
for model in $(seq 12 12); do
#qsub -N ApplyModel-P-${proband}-S-${swallow}

annotationFile=/acogpr/mhh/manual_annotations/AcidAnnotations/${proband}-sm.tsv
     
for i in `find "/${splitsFolder}/Acid-on-Acid-Intra-Experiment/Proband${proband}/split-${s}/test/"  -mindepth 1 -maxdepth 1`; do
#for i in  `find "/${splitsFolder}/Normal-on-Acid-Inter-Experiment/split-${s}/test/"  -mindepth 1 -maxdepth 1`; do

modelfolder="/acogpr/mhh/models/Acid-on-Acid-Intra-Experiment/Proband${proband}/split-${s}/bestAcc"
targetfolder="/tmp/model${model}/Proband-${proband}/split-${s}/Schluck-${swallow}"
if [ -f ${modelfolder}/model_parameters ]; then
    mv ${modelfolder}/model_parameters ${modelfolder}/parameters
fi

    ./run.sh de.ismll.mhh.methods.ApplyModel \
inputfolder=file:/${i} \
model=file://${modelfolder} \
target=file://${targetfolder}

	# 7 == Parser.ANNOTATION_COL_RESTITUTIONSZEIT_SAMPLE (true annotation)
    ./run.sh de.ismll.secondversion.ExtractMatrixEntry col=7 row=$((${proband}-1)) \
file=${annotationFile} \
target=${targetfolder}/true_sample

#    ./run.sh de.ismll.mhh.methods.ApplyModel \
#inputfolder=file:///${splitFolder}/Acid-on-Acid-Intra-Experiment/Proband${proband}/split-${s}/test/${swallow} \
#model=file:///acogpr/mhh/models/acidModels/Proband${proband}/model${model} \
#target=file:///tmp/model${model}/Proband-${proband}/Schluck-${swallow}

done #test folders

done
done
done
done
