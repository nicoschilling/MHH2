#!/bin/bash 

#Fill in the following fields to design a hyperSearch Script!!

# read the references and functions from this file
. readProplib.sh

# read the properties from file mhh.general.properties
sourcein 'general' mhh.general.properties

#
# the path section
#
# where is the data read from and stored to?
#

# which split directory to use?
usesplitsdir=${splitsdir?Warning! Could not get the base splitsdir}/

# which columns to use?
columnselector="33,166"

includeRD=false

annotator="sm"

u_bootstrapclass=de.ismll.secondversion.PreprocessData
u_splitsdir=${usesplitsdir?Error: no experimentsplitsdir found}

./run.sh ${u_bootstrapclass} \
splitFolder=${u_splitsdir}  \
annotator=${annotator} \
columnSelector="${columnselector}" \
annotationBaseDir=${annotationsbasedir}/NormalAnnotations/ \
includeRD=${includeRD} \
serializationOutput=/tmp/columns_${columnselector}_includeRD_${includeRD}_annotator_${annotator}_normalannotations


