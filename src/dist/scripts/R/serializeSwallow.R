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

ifelse(exists("annotationdir"), print("FINE: annotationdir given"), stop("ERROR: need parameter annotationdir. pass it like annotationdir=/some/path"))
ifelse(exists("swallow"), print("FINE: swallow given"), stop("ERROR: need parameter swallow. pass it like swallow=/some/path/to/swallow"))
ifelse(exists("outputfile"), print("FINE: outputfile given"), stop("ERROR: need parameter output. pass it like outputfile=/some/path/to/file"))
ifelse(exists("annotatorsuffix"), print("FINE: annotatorsuffix given"), stop("ERROR: need parameter annotatorsuffix. pass it like annotatorsuffix=sm"))

# read 1:10 annotation files (Proband IDs are added to the matrices)
annotations<- NULL # Initialise
for (i in 1:10) annotations <- rbind(annotations, readAnnotation(paste0(annotationdir,"/",i,"-", annotatorsuffix, ".tsv"),Proband=i))
# check the number of annotations:
#table(annotations$Proband)

# read swallows from the training directory and row-bind those to traningSwallows
swallow<-readSwallow(swallow)

# create dataset by merging swallows and annotations by matching Proband and Swallow
data<-merge(swallow, annotations,by=c("Proband","Swallow"))

# infer labels based on the Sample IDs
labeled_dataset<-inferLabels(data)


labeled_dataset<-cleanData(labeled_dataset)

write.csv(labeled_dataset,  file=outputfile, row.names=FALSE)

