#!/bin/bash -e

#Fill in the following fields to design a hyperSearch Script!!



name=OnePatientOut  #Name für das Experiment entweder Inter oder Intra oder was auch immer
#annotator=mj



swallows=10


includeRD=false
laplacian=true

for split in $(seq 2 11); do
for stepSize in 0.00001  0.000005 0.000001 0.0000007 0.0000005 0.0000003   ;do 
for lambda in  0.01 0.1 1 0.001 ; do
for window in  10  75 150 ; do
for smoothReg in  0.5  1  ; do
for smoothWindow in   5  11 22  ; do
for annotator in mj sm ; do

echo		qsub -N ${name}-lap-smReg-${smoothReg}-smWin-${smoothWindow}-split-${split}-step-${stepSize}-reg-${lambda} run.sh de.ismll.secondversion.StartAlgorithm splitFolder=/acogpr/mhh/Splits/${name}/Proband1/split-${split}  annotator=${annotator} maxIterations=3000 stepSize=${stepSize} lambda=${lambda} windowExtent=${window} columnSelector="33,166" laplacian=${laplacian} useValidation=true descentDirection=logistic modelFunction=linearModel annotationBaseDir=/acogpr/mhh/manual_annotations/NormalAnnotations/ includeRD=${includeRD} runLapTable=run_onepatientout iterTable=iter_onepatientout smoothReg=${smoothReg} smoothWindow=${smoothWindow} smallBatch=false 
done
done
done
done
done
done
done

#smoothReg=${smoothReg} smoothWindow=${smoothWindow}
#-smReg-${smoothReg}-smWin-${smoothWindow}