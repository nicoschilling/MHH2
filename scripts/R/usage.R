# usage

# read definitions from this iolibrary
source("~/MHH2/scripts/R/iolib.R")


# read 1:10 annotation files (Proband IDs are added to the matrices)
annotations<- NULL # Initialise
for (i in 1:10) annotations <- rbind(annotations, readAnnotation(paste0("~/mhh/mhh.busche-it.de/data/manual_annotations/ECDA-Annotations/",i,"-sm.tsv"),Proband=i))
# check the number of annotations:
table(annotations$Proband)

# read swallows from the training directory and row-bind those to traningSwallows
trainingSwallows<-NULL
for (d in dir("~/mhh/mhh.busche-it.de/data/ECDA2014/Splits/intra/Proband1/Split4/train/",full.names=TRUE)) if (file.info(d)$isdir==TRUE) trainingSwallows<-rbind(trainingSwallows,readSwallow(d))

# create dataset by merging swallows and annotations by matching Proband and Swallow
data<-merge(trainingSwallows, annotations,by=c("Proband","Swallow"))

# infer labels based on the Sample IDs
labeled_dataset<-inferLabels(data)

# go ahead and learn!

