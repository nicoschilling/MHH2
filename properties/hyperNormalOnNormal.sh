#!/bin/bash -e

#Fill in the following fields to design a hyperSearch Script!!

proband=1
experimentName="NormalOnNormal"  #Name für das Experiment entweder Inter oder Intra oder was auch immer
annotator="sm"

includeRD=false
laplacian=true
split=1

for stepSize in   0.00001  0.000005 0.000003 0.000001 0.0000007 0.0000005 0.0000003 0.0000001 ;do 
for lambda in  1 0.2 0.1 0.01 0.001   ; do
for window in   30 200  ; do
for smoothReg in  0.5 0.7 0.9  ; do
for smoothWindow in  5 75 
do
qsub -l mem=6G -q acogpr.q,fast.q,all.q -N ${experimentName}-Proband-${proband}-split-${split}-step-${stepSize}-lambda-${lambda}-window-${window}-smoothReg-${smoothReg}-smoothWindow-${smoothWindow} \
run.sh de.ismll.secondversion.StartAlgorithm \
splitFolder=/acogpr/mhh/Splits/AllSwallows/Proband1/split-${split}  \
annotator=${annotator} maxIterations=1000 \
stepSize=${stepSize} \
lambda=${lambda} \
windowExtent=${window} \
columnSelector="33,166" \
laplacian=${laplacian} \
useValidation=true \
descentDirection=logistic modelFunction=linearModel \
annotationBaseDir=/acogpr/mhh/manual_annotations/NormalAnnotations/ \
includeRD=${includeRD} \
runLapTable=run_normal_on_normal \
iterTable=iter_normal_on_normal \
smoothReg=${smoothReg} \
smoothWindow=${smoothWindow} 
done
done
done
done
done
