#!/bin/bash 

#Fill in the following fields to design a hyperSearch Script!!

# try to source in a configuration
. extractData.properties

u_bootstrapclass=de.ismll.secondversion.PreprocessData
u_splitsdir=${usesplitsdir?Error: no experimentsplitsdir found}

u_annotator=${annotator?Error: no annotator found!}
u_includeRD=${includeRD?Error: no includeRD found!}
u_columnselector=${columnselector?Error: non columnselector found!}

./run.sh ${u_bootstrapclass} \
splitFolder=${u_splitsdir}  \
annotator=${u_annotator} \
columnSelector="${u_columnselector}" \
annotationBaseDir=${annotationsbasedir}/NormalAnnotations/ \
includeRD=${u_includeRD} \
serializationOutput=/tmp
