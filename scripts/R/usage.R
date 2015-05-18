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



#annotationdir="~/mhh/mhh.busche-it.de/data/manual_annotations/ECDA-Annotations"
#datadir="~/mhh/mhh.busche-it.de/data/ECDA2014/Splits/intra/Proband1/Split4"
# wget -r -l 1 --user=busche --ask-password http://mhh.busche-it.de/data/ECDA2014/Splits/intra/Proband1/Split4/test/Schluck7/

# read 1:10 annotation files (Proband IDs are added to the matrices)
annotations<- NULL # Initialise
for (i in 1:10) annotations <- rbind(annotations, readAnnotation(paste0(annotationdir,"/",i,"-sm.tsv"),Proband=i))
# check the number of annotations:
table(annotations$Proband)

# read swallows from the training directory and row-bind those to traningSwallows
trainingSwallows<-NULL
for (d in dir(paste0(datadir,"/train/"),full.names=TRUE)) if (file.info(d)$isdir==TRUE) trainingSwallows<-rbind(trainingSwallows,readSwallow(d))

# create dataset by merging swallows and annotations by matching Proband and Swallow
data<-merge(trainingSwallows, annotations,by=c("Proband","Swallow"))

# infer labels based on the Sample IDs
labeled_dataset<-inferLabels(data)

# test visualization with:
# s16<-subset(trainingSwallows,Swallow==6 & Proband==1)
# plotSwallow(s16)

# remove unused / not necessary colums
ld<-cleanData(labeled_dataset)

# currently, labels are encoded 0/1,  whereby -1 denotes 'no label'.
# this filters the data set to only contain those time stamps which denote known labeled time stamps and vectors for those.
ld<-subset(ld, y>=0)


#optional: use weights:
ld$weights<-1
# weight swallow samples larger
ld$weights[ld$y==1]<-2
#ld$weights[ld$relative_sample_to_pmaxsample_manuell>0]<-1/ld$relative_sample_to_pmaxsample_manuell
# TODO: make sth. smarter, e.g., a exponential decay beyond pmax. ... e.g. 1/log(test$x) with x being the "number of samples beyong pmax"
# ... and include those in lm call as weights=ld$weights
ld$weights<-sapply(ld[,match("relative_sample_to_pmaxsample_manuell",colnames(ld))],FUN=function(x){if (x[1]>0){1+1/x}else{1}})

# learn a linear model
mdl <- lm(y ~ max_p_in_sphincter_per_sample + ispost_pmaxmanuell*max_p_in_sphincter_per_sample + isrd*max_p_in_sphincter_per_sample + V2*V3*V4*V5*V6*V7 ,ld)

mdl <- lm(y ~ V3:V5:V6+V2:V4:V7+V4:V5:V7+V2:V4:V9+V4:V7:V9+V2:V3:V4:V6+V2:V3:V5:V6+V2:V4:V5:V6+V3:V4:V5:V6+V2:V4:V5:V7+V3:V4:V5:V7+V2:V4:V6:V7+V2:V3:V5:V8+max_p_in_sphincter_per_sample:V3:V6:V8+V2:V3:V4:V9+V2:V4:V5:V9+V2:V4:V7:V9+V4:V5:V7:V9+V5:V6:V7:V9+V2:V3:V8:V9+V2:V4:V8:V9+V2:V5:V8:V9+V2:V3:V4:V5:V6+V2:V3:V4:V5:V7+V2:V3:V4:V6:V7+V2:V4:V5:V6:V7+max_p_in_sphincter_per_sample:V3:V4:V5:V8+max_p_in_sphincter_per_sample:V2:V3:V6:V8+max_p_in_sphincter_per_sample:V3:V4:V6:V8+max_p_in_sphincter_per_sample:V3:V5:V6:V8+V2:V3:V5:V6:V8+V2:V3:V4:V7:V8+V2:V4:V5:V7:V8+V3:V4:V5:V7:V8+max_p_in_sphincter_per_sample:V2:V6:V7:V8+V2:V4:V6:V7:V8+max_p_in_sphincter_per_sample:V3:V4:V5:V9+V2:V3:V4:V5:V9+V2:V3:V4:V7:V9+max_p_in_sphincter_per_sample:V3:V5:V7:V9+V2:V4:V5:V7:V9+max_p_in_sphincter_per_sample:V5:V6:V7:V9+V2:V5:V6:V7:V9+V3:V5:V6:V7:V9+V4:V5:V6:V7:V9+max_p_in_sphincter_per_sample:V2:V3:V8:V9+max_p_in_sphincter_per_sample:V3:V4:V8:V9+max_p_in_sphincter_per_sample:V3:V5:V8:V9+V2:V3:V5:V8:V9+V2:V4:V5:V8:V9+max_p_in_sphincter_per_sample:V3:V6:V8:V9+V3:V4:V6:V8:V9+V2:V5:V6:V8:V9+V3:V5:V6:V8:V9+max_p_in_sphincter_per_sample:V4:V7:V8:V9+V2:V4:V7:V8:V9+V2:V3:V4:V5:V6:V7+max_p_in_sphincter_per_sample:V2:V3:V4:V6:V8+max_p_in_sphincter_per_sample:V3:V4:V5:V6:V8+max_p_in_sphincter_per_sample:V2:V3:V5:V7:V8+max_p_in_sphincter_per_sample:V3:V4:V5:V7:V8+max_p_in_sphincter_per_sample:V2:V3:V6:V7:V8+V2:V3:V4:V6:V7:V8+max_p_in_sphincter_per_sample:V3:V5:V6:V7:V8+V3:V4:V5:V6:V7:V8+max_p_in_sphincter_per_sample:V3:V4:V5:V6:V9+max_p_in_sphincter_per_sample:V2:V4:V5:V7:V9+max_p_in_sphincter_per_sample:V3:V4:V5:V7:V9+V2:V3:V5:V6:V7:V9+max_p_in_sphincter_per_sample:V4:V5:V6:V7:V9+V3:V4:V5:V6:V7:V9+max_p_in_sphincter_per_sample:V3:V4:V5:V8:V9+max_p_in_sphincter_per_sample:V2:V3:V6:V8:V9+max_p_in_sphincter_per_sample:V3:V4:V6:V8:V9+V2:V3:V5:V6:V8:V9+V3:V4:V5:V6:V8:V9+max_p_in_sphincter_per_sample:V3:V4:V7:V8:V9+V2:V3:V4:V7:V8:V9+V2:V3:V5:V7:V8:V9+max_p_in_sphincter_per_sample:V4:V5:V7:V8:V9+V2:V4:V5:V7:V8:V9+V3:V4:V5:V7:V8:V9+max_p_in_sphincter_per_sample:V4:V6:V7:V8:V9+V2:V4:V6:V7:V8:V9+V3:V4:V6:V7:V8:V9+V3:V5:V6:V7:V8:V9+V4:V5:V6:V7:V8:V9+max_p_in_sphincter_per_sample:V3:V4:V5:V6:V7:V8+max_p_in_sphincter_per_sample:V3:V4:V5:V6:V7:V9+V2:V3:V4:V5:V6:V7:V9+max_p_in_sphincter_per_sample:V3:V4:V5:V6:V8:V9+max_p_in_sphincter_per_sample:V3:V4:V5:V7:V8:V9+V3:V4:V5:V6:V7:V8:V9+max_p_in_sphincter_per_sample:V2:V3:V4:V5:V7:V8:V9+max_p_in_sphincter_per_sample:V3:V4:V5:V6:V7:V8:V9+V2:V3:V4:V5:V6:V7:V8:V9 ,ld)


# predict:
test<-readSwallow(paste0(datadir, "/test/Schluck7/"))
testdata<-merge(test, annotations,by=c("Proband","Swallow"))

labeled_testdataset<-inferLabels(testdata)
ltd<-cleanData(labeled_testdataset)

ltd<-subset(ltd,y>=0)
# predict on test instances and assign those to a new column
ltd$predictions<-predict(mdl,ltd)
# visualize the predictions.
plotSwallow(ltd)

# this is really a FAKE ERROR! just to output some value!
sum ((ltd$predictions-ltd$y)^2)
