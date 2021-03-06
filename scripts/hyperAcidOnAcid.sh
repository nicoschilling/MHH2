#!/bin/bash -e

#Fill in the following fields to design a hyperSearch Script!!


proband=1
experimentName="AcidOnAcid"  #Name für das Experiment entweder Inter oder Intra oder was auch immer
annotator="sm"





includeRD=false
laplacian=true

for proband in 1  ; do
for stepSize in  0.00003  0.00001  0.000005 0.000003  ;do 
for lambda in  1 0.1 0.01   ; do
for window in  30 100   ; do
for split in $(seq 3 4); do
for smoothReg in  1 2 10   ; do
for smoothWindow in  5 35 75 

	    do
		qsub -N ${experimentName}-Proband-${proband}-split-${split}-step-${stepSize}-window-${window}-reg-${lambda} run.sh de.ismll.secondversion.StartAlgorithm splitFolder=/acogpr/mhh/Splits/Acid-on-Acid-Intra-Experiment/Proband${proband}/split-${split}  annotator=${annotator} maxIterations=1000 stepSize=${stepSize} lambda=${lambda} windowExtent=${window} columnSelector="33,166" laplacian=${laplacian} useValidation=true descentDirection=logistic modelFunction=linearModel annotationBaseDir=/acogpr/mhh/manual_annotations/AcidAnnotations/ includeRD=${includeRD} runLapTable=run_acid_on_acid iterTable=iter_acid_on_acid smoothReg=${smoothReg} smoothWindow=${smoothWindow} 
done
done
done
done
done
done
done
