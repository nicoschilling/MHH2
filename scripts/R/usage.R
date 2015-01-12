# usage

# read definitions from this iolibrary
source("~/MHH2/scripts/R/iolib.R")

# wget -r -l 1 --user=busche --ask-password http://mhh.busche-it.de/data/ECDA2014/Splits/intra/Proband1/Split4/test/Schluck7/

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

# test visualization with:
# s16<-subset(trainingSwallows,Swallow==6 & Proband==1)
# plotSwallow(s16)

# remove unused / not necessary colums
labeled_dataset$V7.y<-NULL
labeled_dataset$tRestiDuration<-NULL
labeled_dataset$Pmax<-NULL
labeled_dataset$V4.y<-NULL
labeled_dataset$RD<-NULL
labeled_dataset$tRestiAbsolute<-NULL
labeled_dataset$PmaxZeit<-NULL
labeled_dataset$V4<-labeled_dataset$V4.x
labeled_dataset$V4.x<-NULL
labeled_dataset$V7<-labeled_dataset$V7.x
labeled_dataset$V7.x<-NULL


# clean dataset (remove -Inf, etc.)
labeled_dataset$V18[labeled_dataset$V18=='-Inf']<-median(labeled_dataset$V18[labeled_dataset$V18!='-Inf'])
labeled_dataset$V34[labeled_dataset$V34=='-Inf']<-median(labeled_dataset$V34[labeled_dataset$V34!='-Inf'])
labeled_dataset$V50[labeled_dataset$V50=='-Inf']<-median(labeled_dataset$V50[labeled_dataset$V50!='-Inf'])
labeled_dataset$V66[labeled_dataset$V66=='-Inf']<-median(labeled_dataset$V66[labeled_dataset$V66!='-Inf'])
labeled_dataset$V82[labeled_dataset$V82=='-Inf']<-median(labeled_dataset$V82[labeled_dataset$V82!='-Inf'])
labeled_dataset$V98[labeled_dataset$V98=='-Inf']<-median(labeled_dataset$V98[labeled_dataset$V98!='-Inf'])
labeled_dataset$V114[labeled_dataset$V114=='-Inf']<-median(labeled_dataset$V114[labeled_dataset$V114!='-Inf'])


# go ahead and learn!
# re-assign variable name (less typing)
ld<-labeled_dataset
ld<-subset(labeled_dataset, y>=0)

#optional: use weights:
ld$weights<-1
# weight swallow samples larger
ld$weights[ld$y==1]<-2
# TODO: make sth. smarter, e.g., a exponential decay beyond pmax. ... e.g. 1/log(test$x) with x being the "number of samples beyong pmax"
# ... and include those in lm call as weights=ld$weights


# learn a linear model
mdl <- lm(y ~ pmaxSphincer + ispostpmax*pmaxSphincer + isrd*pmaxSphincer + V2*V3*V4*V5*V6*V7 ,ld)

mdl <- lm(y ~ V3:V5:V6+V2:V4:V7+V4:V5:V7+V2:V4:V9+V4:V7:V9+V2:V3:V4:V6+V2:V3:V5:V6+V2:V4:V5:V6+V3:V4:V5:V6+V2:V4:V5:V7+V3:V4:V5:V7+V2:V4:V6:V7+V2:V3:V5:V8+max_p_in_sphincter_per_sample:V3:V6:V8+V2:V3:V4:V9+V2:V4:V5:V9+V2:V4:V7:V9+V4:V5:V7:V9+V5:V6:V7:V9+V2:V3:V8:V9+V2:V4:V8:V9+V2:V5:V8:V9+V2:V3:V4:V5:V6+V2:V3:V4:V5:V7+V2:V3:V4:V6:V7+V2:V4:V5:V6:V7+max_p_in_sphincter_per_sample:V3:V4:V5:V8+max_p_in_sphincter_per_sample:V2:V3:V6:V8+max_p_in_sphincter_per_sample:V3:V4:V6:V8+max_p_in_sphincter_per_sample:V3:V5:V6:V8+V2:V3:V5:V6:V8+V2:V3:V4:V7:V8+V2:V4:V5:V7:V8+V3:V4:V5:V7:V8+max_p_in_sphincter_per_sample:V2:V6:V7:V8+V2:V4:V6:V7:V8+max_p_in_sphincter_per_sample:V3:V4:V5:V9+V2:V3:V4:V5:V9+V2:V3:V4:V7:V9+max_p_in_sphincter_per_sample:V3:V5:V7:V9+V2:V4:V5:V7:V9+max_p_in_sphincter_per_sample:V5:V6:V7:V9+V2:V5:V6:V7:V9+V3:V5:V6:V7:V9+V4:V5:V6:V7:V9+max_p_in_sphincter_per_sample:V2:V3:V8:V9+max_p_in_sphincter_per_sample:V3:V4:V8:V9+max_p_in_sphincter_per_sample:V3:V5:V8:V9+V2:V3:V5:V8:V9+V2:V4:V5:V8:V9+max_p_in_sphincter_per_sample:V3:V6:V8:V9+V3:V4:V6:V8:V9+V2:V5:V6:V8:V9+V3:V5:V6:V8:V9+max_p_in_sphincter_per_sample:V4:V7:V8:V9+V2:V4:V7:V8:V9+V2:V3:V4:V5:V6:V7+max_p_in_sphincter_per_sample:V2:V3:V4:V6:V8+max_p_in_sphincter_per_sample:V3:V4:V5:V6:V8+max_p_in_sphincter_per_sample:V2:V3:V5:V7:V8+max_p_in_sphincter_per_sample:V3:V4:V5:V7:V8+max_p_in_sphincter_per_sample:V2:V3:V6:V7:V8+V2:V3:V4:V6:V7:V8+max_p_in_sphincter_per_sample:V3:V5:V6:V7:V8+V3:V4:V5:V6:V7:V8+max_p_in_sphincter_per_sample:V3:V4:V5:V6:V9+max_p_in_sphincter_per_sample:V2:V4:V5:V7:V9+max_p_in_sphincter_per_sample:V3:V4:V5:V7:V9+V2:V3:V5:V6:V7:V9+max_p_in_sphincter_per_sample:V4:V5:V6:V7:V9+V3:V4:V5:V6:V7:V9+max_p_in_sphincter_per_sample:V3:V4:V5:V8:V9+max_p_in_sphincter_per_sample:V2:V3:V6:V8:V9+max_p_in_sphincter_per_sample:V3:V4:V6:V8:V9+V2:V3:V5:V6:V8:V9+V3:V4:V5:V6:V8:V9+max_p_in_sphincter_per_sample:V3:V4:V7:V8:V9+V2:V3:V4:V7:V8:V9+V2:V3:V5:V7:V8:V9+max_p_in_sphincter_per_sample:V4:V5:V7:V8:V9+V2:V4:V5:V7:V8:V9+V3:V4:V5:V7:V8:V9+max_p_in_sphincter_per_sample:V4:V6:V7:V8:V9+V2:V4:V6:V7:V8:V9+V3:V4:V6:V7:V8:V9+V3:V5:V6:V7:V8:V9+V4:V5:V6:V7:V8:V9+max_p_in_sphincter_per_sample:V3:V4:V5:V6:V7:V8+max_p_in_sphincter_per_sample:V3:V4:V5:V6:V7:V9+V2:V3:V4:V5:V6:V7:V9+max_p_in_sphincter_per_sample:V3:V4:V5:V6:V8:V9+max_p_in_sphincter_per_sample:V3:V4:V5:V7:V8:V9+V3:V4:V5:V6:V7:V8:V9+max_p_in_sphincter_per_sample:V2:V3:V4:V5:V7:V8:V9+max_p_in_sphincter_per_sample:V3:V4:V5:V6:V7:V8:V9+V2:V3:V4:V5:V6:V7:V8:V9 ,ld)


# predict:
test<-readSwallow("~/mhh/mhh.busche-it.de/data/ECDA2014/Splits/intra/Proband1/Split4/test/Schluck7/")
testdata<-merge(test, annotations,by=c("Proband","Swallow"))
labeled_testdataset<-inferLabels(testdata)
labeled_testdataset$V7.y<-NULL
labeled_testdataset$tRestiDuration<-NULL
labeled_testdataset$Pmax<-NULL
labeled_testdataset$V4.y<-NULL
labeled_testdataset$RD<-NULL
labeled_testdataset$tRestiAbsolute<-NULL
labeled_testdataset$PmaxZeit<-NULL
labeled_testdataset$V4<-labeled_testdataset$V4.x
labeled_testdataset$V4.x<-NULL
labeled_testdataset$V7<-labeled_testdataset$V7.x
labeled_testdataset$V7.x<-NULL

ltd<-labeled_testdataset
ltd<-subset(labeled_testdataset,y>=0)
# predict on test instances and assign those to a new column
ltd$predictions<-predict(mdl,ltd)
# visualize the predictions.
plotSwallow(ltd)


