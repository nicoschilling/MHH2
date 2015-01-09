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
labeled_dataset$V7<-labeled_dataset$V7.x


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
# learn a linear model
mdl <- lm(y ~ pmaxSphincer + ispostpmax*pmaxSphincer + isrd*pmaxSphincer + V2*V3*V4*V5*V6*V7 ,ld)
# predict on test instances and assign those to a new column
test$predictions<-predict(mdl,test)
# visualize the predictions.
plotSwallow(test)


