#!/bin/bash -e

#Fill in the following fields to design a hyperSearch Script!!


proband=$1
name=Inter  #Name für das Experiment entweder Inter oder Intra oder was auch immer
annotator=mj


swallows=10

includeRD=false
laplacian=false

for stepSize in   0.00001 0.000005 0.000001    ;do 
for lambda in  1 0.1   ; do
for window in  10  75 150; do
for split in $(seq 1 ${swallows})

	    do
		qsub -N ${name}-Proband-${proband}-split-${split}-step-${stepSize}-reg-${lambda} run.sh de.ismll.secondversion.StartAlgorithm splitFolder=/acogpr/mhh/Splits/${name}Proband/Proband${proband}/split-${split}  annotator=${annotator} maxIterations=2000 stepSize=${stepSize} lambda=${lambda} windowExtent=${window} columnSelector="33,166" laplacian=${laplacian} useValidation=true descentDirection=logistic modelFunction=linearModel annotationBaseDir=/acogpr/mhh/manual_annotations/NormalAnnotations/ includeRD=${includeRD} runTable=run_inter_new iterTable=iter_inter_new  
done
done
done
done

