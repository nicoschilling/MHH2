#!/bin/bash -e

#Fill in the following fields to design a hyperSearch Script!!


proband=1
experimentName="AcidExperiment"  #Name für das Experiment entweder Inter oder Intra oder was auch immer
annotator="sm"





includeRD=false
laplacian=false

for stepSize in 0.00005 0.00003  0.00001  0.000007 0.000005 0.000003  ;do 
for lambda in  1 0.1 0.01 0.001  ; do
for window in  75 100 150  ; do
for split in $(seq 12 16)

	    do
		qsub -N ${experimentName}-Proband-${proband}-split-${split}-step-${stepSize}-window-${window}-reg-${lambda} run.sh de.ismll.secondversion.StartAlgorithm splitFolder=/acogpr/mhh/Splits/${experimentName}/Proband${proband}/split-${split}  annotator=${annotator} maxIterations=4000 stepSize=${stepSize} lambda=${lambda} windowExtent=${window} columnSelector="33,166" laplacian=${laplacian} useValidation=true descentDirection=logistic modelFunction=linearModel annotationBaseDir=/acogpr/mhh/manual_annotations/AcidAnnotations/ includeRD=${includeRD} runTable=runacid_full iterTable=iteracid_full  
done
done
done
done

