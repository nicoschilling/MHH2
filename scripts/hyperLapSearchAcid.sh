#!/bin/bash -e

#Fill in the following fields to design a hyperSearch Script!!


proband=1
experimentName="AcidExperiment"  #Name für das Experiment entweder Inter oder Intra oder was auch immer
annotator="sm"





includeRD=false
laplacian=true

for stepSize in 0.00005 0.00003  0.00001  0.000005 0.000003  ;do 
for lambda in  1 0.1 0.01   ; do
for window in   100  200 250  ; do
for split in $(seq 12 16); do
for smoothReg in  0.5 0.7 1  ; do
for smoothWindow in  15 30 

	    do
echo		qsub -N Lap-${experimentName}-Proband-${proband}-split-${split}-step-${stepSize}-window-${window}-reg-${lambda} run.sh de.ismll.secondversion.StartAlgorithm splitFolder=/acogpr/mhh/Splits/${experimentName}/Proband${proband}/split-${split}  annotator=${annotator} maxIterations=2000 stepSize=${stepSize} lambda=${lambda} windowExtent=${window} columnSelector="33,166" laplacian=${laplacian} useValidation=true descentDirection=logistic modelFunction=linearModel annotationBaseDir=/acogpr/mhh/manual_annotations/AcidAnnotations/ includeRD=${includeRD} runLapTable=runacid_lap_full iterTable=iteracid_lap_full smoothReg=${smoothReg} smoothWindow=${smoothWindow} 
done
done
done
done
done
done

