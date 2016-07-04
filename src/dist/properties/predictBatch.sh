#!/bin/bash 
#
#$ -cwd
#$ -S /bin/bash
#$ -j n
#$ -o logs/output.predictBatch.$JOB_NAME.txt
#$ -e logs/output.predictBatch.$JOB_NAME.err
#$ -p 0
#$ -m a
#$ -M busche@ismll.de
#$ -R y

#echo $PATH

PATH=$PATH:.
#echo $PATH

# source the properties in
. $1 

# include function definitions for running java programs
. target_java.sh

# include lifecycle utilities
. lifecycleutils.sh

# include traputils to get notified upon traps
. traputils.sh

function on_run() {
u_usesplitsdir=${usesplitsdir?Error: no usesplitsdir defined!}
u_usemodelsdir=${usemodelsdir?Error: no usemodelsdir defined!}
u_predictionsdir=${usepredictionsdir?Error: no predictionsdir defined!}

#ls
#pwd
#echo ""
#echo ""


# folders containing one single swallow
for currentFolder in `find "/${u_usesplitsdir}/test/"  -mindepth 1 -maxdepth 1`; do

# extracts last part of the path (the folder)
swallowfoldername=`echo ${currentFolder} | awk -F/ ' {print $NF } '`

# read contents from files (proband and swallow id):
proband=`cat ${currentFolder}/proband`
schluck=`cat ${currentFolder}/id`

if [ ! -d ${u_predictionsdir}/${swallowfoldername} ]; then
mkdir -p ${u_predictionsdir}/${swallowfoldername}
fi

# link swallow and proband id to target folder
ln ${currentFolder}/proband ${u_predictionsdir}/${swallowfoldername}/proband
ln ${currentFolder}/id ${u_predictionsdir}/${swallowfoldername}/id

annotationFile=/acogpr/mhh/manual_annotations/NormalAnnotations/${proband}-sm.tsv

# cannot be extracted to common properties-file: the SQL Query in bestModelBatch.sh requires two variables.
for what in bestAcc bestSampleDiff; do

# which model to use?
modelfolder="${u_usemodelsdir}/${what}"
targetfolder="${u_predictionsdir}/${swallowfoldername}/${what}/"

#mkdir -p ${targetfolder}

if [ -f ${modelfolder}/model_parameters ]; then
    mv ${modelfolder}/model_parameters ${modelfolder}/parameters
fi

	run_bootstrap_java de.ismll.mhh.methods.ApplyModel \
inputfolder=file:/${currentFolder} \
model=file://${modelfolder} \
target=file://${targetfolder}

if [ ! 'x'${schluck} = 'x' ]; then
	# 7 == Parser.ANNOTATION_COL_RESTITUTIONSZEIT_SAMPLE (true annotation)
	run_bootstrap_java de.ismll.secondversion.ExtractMatrixEntry col=7 row=$((${schluck}-1)) \
file=${annotationFile} \
target=${targetfolder}/true_sample
#echo $proband > ${targetfolder}/proband

else
	echo "\$schluck not defined. Cannot extract true_sample!"
fi # $schluck defined

done #test folders
done # of model

}

guarded_run

