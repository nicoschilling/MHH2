#
#  MAKEFILE for R-Scripts
#
#  merely a shortcut for longer calls on the command line
#
#  defined targets:
#  * t testing target. Used in-development
#
#

DATA=/home/bcsanbu/mhh/mhh.busche-it.de/data/
t:
	Rscript usage.R datadir=$(DATA)/ECDA2014/Splits/intra/Proband1/Split4 annotationdir=$(DATA)/manual_annotations/ECDA-Annotations

createSwallowDataset:
	Rscript serializeSwallow.R \
	  swallow=$(DATA)/ECDA2014/Splits/intra/Proband1/Split1/train/Schluck2/ \
	  annotationdir=$(DATA)/manual_annotations/ECDA-Annotations \
	  outputfile=/tmp/blub \
	  annotatorsuffix=sm

serializeDataset:
	mkdir /tmp/blub 
	Rscript createDataset.R \
	  datadir=$(DATA)/ECDA2014/Splits/intra/Proband1/Split1/ \
	  annotationdir=$(DATA)/manual_annotations/ECDA-Annotations \
	  outputdir=/tmp/blub \
	  annotatorsuffix=sm
