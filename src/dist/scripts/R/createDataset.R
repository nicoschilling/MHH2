# usage

# configure used packages
#
# logging
# 
# see http://logging.r-forge.r-project.org/sample_session.php
library(logging)
basicConfig(level=20)

# read definitions from this iolibrary
source("./iolib.R")

args <- commandArgs(trailingOnly = TRUE)

for (i in 1:length(args)) {
	spl<-strsplit(args[[i]],   split="=")
	assign(as.character(spl[[1]][[1]]),   as.character(spl[[1]][[2]]))
}

ifelse(exists("datadir"), print("FINE: datadir given"), stop("ERROR: need parameter datadir. pass it like datadir=/some/path"))
ifelse(exists("annotationdir"), print("FINE: annotationdir given"), stop("ERROR: need parameter annotationdir. pass it like annotationdir=/some/path"))
ifelse(exists("outputdir"), print("FINE: outputdir given"), stop("ERROR: need parameter outputdir. pass it like outputdir=/some/path"))
ifelse(exists("annotatorsuffix"), print("FINE: annotatorsuffix given"), stop("ERROR: need parameter annotatorsuffix. pass it like annotatorsuffix=sm"))

#annotationdir="~/mhh/mhh.busche-it.de/data/manual_annotations/ECDA-Annotations"
#datadir="~/mhh/mhh.busche-it.de/data/ECDA2014/Splits/intra/Proband1/Split4"
# wget -r -l 1 --user=busche --ask-password http://mhh.busche-it.de/data/ECDA2014/Splits/intra/Proband1/Split4/test/Schluck7/

# read 1:10 annotation files (Proband IDs are added to the matrices)
annotations<- NULL # Initialise
for (i in 1:10) annotations <- rbind(annotations, readAnnotation(paste0(annotationdir,"/",i,"-", annotatorsuffix, ".tsv"),Proband=i))
# check the number of annotations:
table(annotations$Proband)

# read swallows from the training directory and row-bind those to traningSwallows
trainingSwallows<-NULL
testSwallows<-NULL
validationSwallows<-NULL
for (d in dir(paste0(datadir,"/train/"),full.names=TRUE)) if (file.info(d)$isdir==TRUE) trainingSwallows<-rbind(trainingSwallows,readSwallow(paste0(d, "/")))
for (d in dir(paste0(datadir,"/test/"),full.names=TRUE)) if (file.info(d)$isdir==TRUE) testSwallows<-rbind(testSwallows,readSwallow(paste0(d, "/")))
for (d in dir(paste0(datadir,"/validation/"),full.names=TRUE)) if (file.info(d)$isdir==TRUE) validationSwallows<-rbind(validationSwallows,readSwallow(paste0(d, "/")))


# create dataset by merging swallows and annotations by matching Proband and Swallow
logdebug('Merging trainingdata')
trainingdata<-merge(trainingSwallows, annotations,by=c("Proband","Swallow"))
logdebug('Merging validationdata')
validationdata<-merge(validationSwallows,  annotations, by=c("Proband", "Swallow"))
logdebug('Merging testdata')
testdata<-merge(testSwallows,  annotations, by=c("Proband", "Swallow"))

# infer labels based on the Sample IDs
logdebug('Inferring labels for trainingdata')
labeled_training_dataset<-inferLabels(trainingdata)
logdebug('Inferring labels for validationdata')
labeled_validation_dataset<-inferLabels(validationdata)
logdebug('Inferring labels for testdata')
labeled_test_dataset<-inferLabels(testdata)

# test visualization with:
# s16<-subset(trainingSwallows,Swallow==6 & Proband==1)
# plotSwallow(s16)

logdebug('Writing trainingdata')
write.csv(labeled_training_dataset, file=paste0(outputdir, "/training.csv"),row.names=TRUE)
logdebug('Writing validationdata')
write.csv(labeled_validation_dataset, file=paste0(outputdir, "/validation.csv"),row.names=TRUE)
logdebug('Writing testdata')
write.csv(labeled_test_dataset, file=paste0(outputdir, "/test.csv"),row.names=TRUE)
