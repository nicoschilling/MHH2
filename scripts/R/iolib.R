library(logging)
basicConfig(level=10)
loginfo('about to define functions for I/O ...')

# read a swallow from path and return it as a data.frame. Rows denote individual data tuples. Named columns corresponding fields
#
# @param in path to a directory containing a single swallow. It should contain files like data.csv,  fft.csv, rdstart, etc.
# @return a data frame of the swallow, containing the following fields:
# 	Sample the sample ID
# 	P1 ... P20 pressure sensor raw values
# 	Resp1 ... Resp3 unknown data fields from the measurement
# 	Swallow1 ... Swallow3 unknown data fields from the measurement
# 	V2 ... V129 FFT values
# 	rdstartsample constant \forall Sample: sample ID when the rd ('Ruhedruck') starts
# 	rdendsample constant \forall Sample: sample ID when the rd ('Ruhedruck') ends
# 	max_p_in_sphincter_per_sample the 'pmax' curve. max(P ... P) \forall Sample
# 	isrd derived: 0/1 encoding whether the 'current' sample is within the rd ('Ruhedruck'),  excluding bounds (0-> not within; 1 -> within)
# 	pmaxsample_manuell optional: constant \forall Sample: manual encoding of the pmax sample (human annotation)
# 	ispost_pmaxmanuell optional derived: 0/1 encoding whether the 'current' sample is after or equal to the pmaxsample_manuell sample (0->before sample,  1 -> after sample)
# 	Swallow unique swallow ID
# 	Proband unique proband ID
# 	valid boolean (TRUE/FALSE) flag whether the data is valid (related sensor IDs make sense)
# 	relative_sample_to_pmaxsample_manuell derived: (current sample - pmaxsample_manuell)
#
readSwallow <- function (path) {
loginfo('In Directory %s', path)
setwd(path)

logdebug('Reading data.csv')
sensors<-read.csv("data.csv",header=FALSE)
colnames(sensors)<-c("Sample","P1","P2","P3","P4","P5","P6","P7","P8","P9","P10","P11","P12","P13","P14","P15","P16","P17","P18","P19","P20","Resp1","Resp2","Resp3","Swallow1","Swallow2","Swallow3","Marker","misc")
logdebug('Reading fft.csv')
fft<-read.csv("fft.csv",header=FALSE)
data<-cbind(sensors,fft)
# was supposed to be the Sample; errorously removed.
data$V1<-NULL
data$misc<-NULL
data$Marker<-NULL

#sphinctermax<-read.csv("subset-max.csv",header=FALSE)
#data$sphinctermaxV1<-sphinctermax$V1
#data$sphinctermaxV2<-sphinctermax$V2

# read raw data
logdebug('Reading rdstart')
rdstart<-read.csv("rdstart",header=FALSE)
logdebug('Reading rdend')
rdend<-read.csv("rdend",header=FALSE)
logdebug('Reading channelstart')
channelstart<-as.integer(read.csv("channelstart",header=FALSE))
logdebug('Reading channelend')
channelend<-as.integer(read.csv("channelend",header=FALSE))

#     V1
# 1 04:05
logdebug('Reading samplerate')
samplerate<-as.integer(read.csv("samplerate",header=FALSE))

# split time values
tmp<-strsplit(as.character(rdstart[[1]]),":")[[1]]
# >tmp
# [1] "04" "05"

#compute rdstart sample
#as.integer(tmp[[1]])*60*as.integer(samplerate)+as.integer(tmp[[2]])*as.integer(samplerate)

# create a vector of the length of the swallow
# data$rdstartsample<-c(1:dim(sensors)[[1]])
data$rdstartsample<-0
# assign it with the start sample value
data$rdstartsample<-as.integer(tmp[[1]])*60*as.integer(samplerate)+as.integer(tmp[[2]])*as.integer(samplerate)
# the same for rdend

tmp<-strsplit(as.character(rdend[[1]]),":")[[1]]

data$rdendsample<-as.integer(tmp[[1]])*60*as.integer(samplerate)+as.integer(tmp[[2]])*as.integer(samplerate)

# extract maximum for each sample from sphincter region
data$max_p_in_sphincter_per_sample<-apply(data[,grep(paste0("P",channelstart),colnames(data)):grep(paste0("P",channelend),colnames(data))] ,1,max)

# encode in {0,1} whether a sample denotes the "ruhedruck". 0->swallow-something; 1->ruhedruck
data$isrd<-0
data$isrd[ data$Sample>data$rdstartsample & data$Sample<data$rdendsample ]<-1

# extract pmax_manuell
if (TRUE == file.exists("pmax_manuell")) {
	logdebug('Reading pmax_manuell')
	tmp<-strsplit(as.character(read.table("pmax_manuell",header=FALSE)[[1]]),"[:,]")[[1]]
	data$pmaxsample_manuell<-0
	data$pmaxsample_manuell<-as.integer(tmp[[1]])*50*60 + as.integer(tmp[[2]])*50+ as.integer(tmp[[3]])/100*50
} else {
	data$pmaxsample_manuell<-which.max(data$max_p_in_sphincter_per_sample)+data$Sample[[1]]
	logwarn('File pmax_manuell does not exist. Computed pmaxsample_manuell value (%i)', which.max(data$max_p_in_sphincter_per_sample)+data$Sample[[1]])
}

# again, do binary encoding of the pmax area
data$ispost_pmaxmanuell<-0
data$ispost_pmaxmanuell[ data$Sample>=data$pmaxsample_manuell ]<-1

# include swallow id and proband id
logdebug('Reading id')
data$Swallow<-read.csv("id",header=FALSE)[[1]]
logdebug('Reading proband')
data$Proband<-read.csv("proband",header=FALSE)[[1]]

# adjust data types
data$Sample<-as.integer(data$Sample)
#data$Swallow<-as.factor(data$Swallow)
#data$Proband<-as.factor(data$Proband)
data$pmaxsample_manuell<-as.integer(data$pmaxsample_manuell)
data$rdendsample<-as.integer(data$rdendsample)
data$rdstartsample<-as.integer(data$rdstartsample)

# some sanity checks:
data$valid<-TRUE
data$valid<-min(data$Sample)<max(data$Sample)
data$valid<-min(data$Sample)<max(data$pmaxsample_manuell)
data$valid<-max(data$pmaxsample_manuell)<max(data$Sample)

data$relative_sample_to_pmaxsample_manuell<-apply(data[,c(match("Sample",colnames(data)),match("pmaxsample_manuell",colnames(data)))],1,FUN=function(x){x[1] - x[2]})

return (data)
} # of function readSwallow

# read the annotations from the given annotation file using the given proband id. A row encodes an annotation tuple for a specific swallow; the named columns their attributes
#
# @param annotationfile path to an annotation file
# @param Proband ID of the proband (encoded as an ID in the return data frame)
# @param samplerate the samplerate which is used to decode time information to samples
# @return a data frame with the following fields:
# 	Swallow the Swallow ID of the annotation
# 	RD optional: the annotated resting pressure ('Ruhedruck') extracted from the computer program
# 	Pmax optional: maximal pressure of the pmax curve, automatically calculated by the computer program
# 	V4 optional: pmax sensor, on which the maximal pressure was measured
# 	PmaxZeit timestamp encoding the time when the maximal pressure was measured
# 	tRestiDuration optional: duration after pmax denoting the duration of the restitution time (aka. the actual label, given the restitution estimation task)
# 	V7 optional: ???
# 	tRestiAbsolute: absolute timestamp encoding the end of the restitution time of the current swallow (aka. when labeling, the timestamp when the annotation changes from 'swallowing' to 'non-swallowing')
# 	Proband proband ID (same as input parameter)
# 	tRestiAbsoluteSample calculated from tRestiAbsolute: the sample id of XXX,  when using input parameter samplerate during the conversion
# 	pmaxsample_manuell_from_annotationfile calculated from PmaxZeit: Individual annotation in time units when reaching the resting pressure again (aka. the point in time when the restitution is reached) (human override of Pmax)
readAnnotation <-function(annotationfile,Proband,samplerate=50) {
# annotationfile=~/mhh/busche-it.de/data/manual_annotations/ECDA-Annotations/1-sm.tsv
loginfo('Reading %s', annotationfile)
annotations<-read.csv(annotationfile,header=FALSE,sep='\t')
colnames(annotations)<-c("Swallow","RD","Pmax","V4","PmaxZeit","tRestiDuration","V7","tRestiAbsolute")
# store proband as dedicated column
annotations$Proband<-Proband

# convert times to samples
annotations$tRestiAbsoluteSample<-sapply(annotations$tRestiAbsolute,FUN=function(x) {tmp<-strsplit(as.character(x)[[1]],"[:,]")[[1]]; return(as.integer(tmp[[1]])*samplerate*60 + as.integer(tmp[[2]])*samplerate+ as.integer(tmp[[3]])/100*samplerate)})
annotations$pmaxsample_manuell_from_annotationfile<-sapply(annotations$PmaxZeit,FUN=function(x) {tmp<-strsplit(as.character(x)[[1]],"[:,]")[[1]]; return(as.integer(tmp[[1]])*samplerate*60 + as.integer(tmp[[2]])*samplerate+ as.integer(tmp[[3]])/100*samplerate)})

return(annotations)
} # of function readAnnotation

# infers the labels (column y added) from the merged dataset
#
# Initialize all labels to -1. Then, give a label 0 or 1 to those samples denoting the resting pressure and the swallowing period, resp.
inferLabels<-function(data_and_annotations){
# infer labels. Unknown labels everywhere:
data_and_annotations$y<- -1
# non-swallow labels in the "ruhedruck" area and beyond the annotation
data_and_annotations$y[ data_and_annotations$isrd==1 ]<-0
data_and_annotations$y[ data_and_annotations$Sample>= data_and_annotations$tRestiAbsoluteSample]<-0
# swallow-labels between the pmax sample and the annotation
data_and_annotations$y[ data_and_annotations$ispostpmaxmanuell==1 &  data_and_annotations$Sample< data_and_annotations$tRestiAbsoluteSample]<-1
# or:
data_and_annotations$y[ data_and_annotations$Sample>=data_and_annotations$pmaxsample_manuell & data_and_annotations$Sample< data_and_annotations$tRestiAbsoluteSample]<-1
return (data_and_annotations)
}

# Removes several unused columns from the given dataset and heals '-Inf' entries from the FFT calculations to be replaced by 0.0
#
cleanData<-function(dataset) {
dataset$V7.y<-NULL
dataset$tRestiDuration<-NULL
dataset$Pmax<-NULL
dataset$V4.y<-NULL
dataset$RD<-NULL
dataset$tRestiAbsolute<-NULL
dataset$PmaxZeit<-NULL
dataset$V4<-dataset$V4.x
dataset$V4.x<-NULL
dataset$V7<-dataset$V7.x
dataset$V7.x<-NULL


# clean dataset (remove -Inf, etc.)
#dataset$V18[dataset$V18=='-Inf']<-median(dataset$V18[dataset$V18!='-Inf'])
#dataset$V34[dataset$V34=='-Inf']<-median(dataset$V34[dataset$V34!='-Inf'])
#dataset$V50[dataset$V50=='-Inf']<-median(dataset$V50[dataset$V50!='-Inf'])
#dataset$V66[dataset$V66=='-Inf']<-median(dataset$V66[dataset$V66!='-Inf'])
#dataset$V82[dataset$V82=='-Inf']<-median(dataset$V82[dataset$V82!='-Inf'])
#dataset$V98[dataset$V98=='-Inf']<-median(dataset$V98[dataset$V98!='-Inf'])
#dataset$V114[dataset$V114=='-Inf']<-median(dataset$V114[dataset$V114!='-Inf'])

#dataset$V1[dataset$V1=='-Inf']<-0
dataset$V2[dataset$V2=='-Inf']<-0
dataset$V3[dataset$V3=='-Inf']<-0
dataset$V4[dataset$V4=='-Inf']<-0
dataset$V5[dataset$V5=='-Inf']<-0
dataset$V6[dataset$V6=='-Inf']<-0
dataset$V7[dataset$V7=='-Inf']<-0
dataset$V8[dataset$V8=='-Inf']<-0
dataset$V9[dataset$V9=='-Inf']<-0
dataset$V10[dataset$V10=='-Inf']<-0
dataset$V11[dataset$V11=='-Inf']<-0
dataset$V12[dataset$V12=='-Inf']<-0
dataset$V13[dataset$V13=='-Inf']<-0
dataset$V14[dataset$V14=='-Inf']<-0
dataset$V15[dataset$V15=='-Inf']<-0
dataset$V16[dataset$V16=='-Inf']<-0
dataset$V17[dataset$V17=='-Inf']<-0
dataset$V18[dataset$V18=='-Inf']<-0
dataset$V19[dataset$V19=='-Inf']<-0
dataset$V20[dataset$V20=='-Inf']<-0
dataset$V21[dataset$V21=='-Inf']<-0
dataset$V22[dataset$V22=='-Inf']<-0
dataset$V23[dataset$V23=='-Inf']<-0
dataset$V24[dataset$V24=='-Inf']<-0
dataset$V25[dataset$V25=='-Inf']<-0
dataset$V26[dataset$V26=='-Inf']<-0
dataset$V27[dataset$V27=='-Inf']<-0
dataset$V28[dataset$V28=='-Inf']<-0
dataset$V29[dataset$V29=='-Inf']<-0
dataset$V30[dataset$V30=='-Inf']<-0
dataset$V31[dataset$V31=='-Inf']<-0
dataset$V32[dataset$V32=='-Inf']<-0
dataset$V33[dataset$V33=='-Inf']<-0
dataset$V34[dataset$V34=='-Inf']<-0
dataset$V35[dataset$V35=='-Inf']<-0
dataset$V36[dataset$V36=='-Inf']<-0
dataset$V37[dataset$V37=='-Inf']<-0
dataset$V38[dataset$V38=='-Inf']<-0
dataset$V39[dataset$V39=='-Inf']<-0
dataset$V40[dataset$V40=='-Inf']<-0
dataset$V41[dataset$V41=='-Inf']<-0
dataset$V42[dataset$V42=='-Inf']<-0
dataset$V43[dataset$V43=='-Inf']<-0
dataset$V44[dataset$V44=='-Inf']<-0
dataset$V45[dataset$V45=='-Inf']<-0
dataset$V46[dataset$V46=='-Inf']<-0
dataset$V47[dataset$V47=='-Inf']<-0
dataset$V48[dataset$V48=='-Inf']<-0
dataset$V49[dataset$V49=='-Inf']<-0
dataset$V50[dataset$V50=='-Inf']<-0
dataset$V51[dataset$V51=='-Inf']<-0
dataset$V52[dataset$V52=='-Inf']<-0
dataset$V53[dataset$V53=='-Inf']<-0
dataset$V54[dataset$V54=='-Inf']<-0
dataset$V55[dataset$V55=='-Inf']<-0
dataset$V56[dataset$V56=='-Inf']<-0
dataset$V57[dataset$V57=='-Inf']<-0
dataset$V58[dataset$V58=='-Inf']<-0
dataset$V59[dataset$V59=='-Inf']<-0
dataset$V60[dataset$V60=='-Inf']<-0
dataset$V61[dataset$V61=='-Inf']<-0
dataset$V62[dataset$V62=='-Inf']<-0
dataset$V63[dataset$V63=='-Inf']<-0
dataset$V64[dataset$V64=='-Inf']<-0
dataset$V65[dataset$V65=='-Inf']<-0
dataset$V66[dataset$V66=='-Inf']<-0
dataset$V67[dataset$V67=='-Inf']<-0
dataset$V68[dataset$V68=='-Inf']<-0
dataset$V69[dataset$V69=='-Inf']<-0
dataset$V70[dataset$V70=='-Inf']<-0
dataset$V71[dataset$V71=='-Inf']<-0
dataset$V72[dataset$V72=='-Inf']<-0
dataset$V73[dataset$V73=='-Inf']<-0
dataset$V74[dataset$V74=='-Inf']<-0
dataset$V75[dataset$V75=='-Inf']<-0
dataset$V76[dataset$V76=='-Inf']<-0
dataset$V77[dataset$V77=='-Inf']<-0
dataset$V78[dataset$V78=='-Inf']<-0
dataset$V79[dataset$V79=='-Inf']<-0
dataset$V80[dataset$V80=='-Inf']<-0
dataset$V81[dataset$V81=='-Inf']<-0
dataset$V82[dataset$V82=='-Inf']<-0
dataset$V83[dataset$V83=='-Inf']<-0
dataset$V84[dataset$V84=='-Inf']<-0
dataset$V85[dataset$V85=='-Inf']<-0
dataset$V86[dataset$V86=='-Inf']<-0
dataset$V87[dataset$V87=='-Inf']<-0
dataset$V88[dataset$V88=='-Inf']<-0
dataset$V89[dataset$V89=='-Inf']<-0
dataset$V90[dataset$V90=='-Inf']<-0
dataset$V91[dataset$V91=='-Inf']<-0
dataset$V92[dataset$V92=='-Inf']<-0
dataset$V93[dataset$V93=='-Inf']<-0
dataset$V94[dataset$V94=='-Inf']<-0
dataset$V95[dataset$V95=='-Inf']<-0
dataset$V96[dataset$V96=='-Inf']<-0
dataset$V97[dataset$V97=='-Inf']<-0
dataset$V98[dataset$V98=='-Inf']<-0
dataset$V99[dataset$V99=='-Inf']<-0
dataset$V100[dataset$V100=='-Inf']<-0
dataset$V101[dataset$V101=='-Inf']<-0
dataset$V102[dataset$V102=='-Inf']<-0
dataset$V103[dataset$V103=='-Inf']<-0
dataset$V104[dataset$V104=='-Inf']<-0
dataset$V105[dataset$V105=='-Inf']<-0
dataset$V106[dataset$V106=='-Inf']<-0
dataset$V107[dataset$V107=='-Inf']<-0
dataset$V108[dataset$V108=='-Inf']<-0
dataset$V109[dataset$V109=='-Inf']<-0
dataset$V120[dataset$V120=='-Inf']<-0
dataset$V121[dataset$V121=='-Inf']<-0
dataset$V122[dataset$V122=='-Inf']<-0
dataset$V123[dataset$V123=='-Inf']<-0
dataset$V124[dataset$V124=='-Inf']<-0
dataset$V125[dataset$V125=='-Inf']<-0
dataset$V126[dataset$V126=='-Inf']<-0
dataset$V127[dataset$V127=='-Inf']<-0
#dataset$V18[dataset$V128=='-Inf']<-0
#dataset$V18[dataset$V129=='-Inf']<-0

#dataset$V18[dataset$V18=='-Inf']<-0
#dataset$V34[dataset$V34=='-Inf']<-0
#dataset$V50[dataset$V50=='-Inf']<-0
#dataset$V66[dataset$V66=='-Inf']<-0
#dataset$V82[dataset$V82=='-Inf']<-0
#dataset$V98[dataset$V98=='-Inf']<-0
#dataset$V114[dataset$V114=='-Inf']<-0


return(dataset)

}

# plots the swallow to the current graphical device from the given data.frame
#
# supports the following optional columns:
#  y <- the actual label, assumed in {0,1}
#  predictions <- the sample-wise predictions
plotSwallow<-function(swallowdata) {
# the colums from P1..P20
columnrange<-min(grep("P1",colnames(swallowdata))):grep("P20",colnames(swallowdata))

# extract absolute samples and base them on 1
samples<-swallowdata$Sample
relativeSamples<-samples - min(samples) + 1
columns<-seq(1,20)

# extract max pressure curve
maxPressures<-sapply(swallowdata$max_p_in_sphincter_per_sample,FUN=function(x){(max(x)/500 )*15  +3})

# additional aspects:
# visualization whether we are beyond the pmax area
#		points(relativeSamples, (swallowdata$ispost_pmaxmanuell+2)*2, col="blue",cex=0.25);
# visualization whether we are in the "ruhedruck"
#		points(relativeSamples, (swallowdata$isrd+2)*2, col="blue",cex=0.25);

labels<-swallowdata$y
if (is.null(labels)){
	labels<-rep(0,length(samples))
} else {
	labels<-labels*10+5
}

predictions<-swallowdata$predictions
if (is.null(predictions)){
	predictions<-rep(0,length(samples))
} else {
	predictions<-predictions*10+5
}


filled.contour(
	relativeSamples,
	columns,
	log(as.matrix(swallowdata[,columnrange])+100),
	color = terrain.colors,
	plot.axes = { axis(1); axis(2); 
		points(relativeSamples, maxPressures, col="yellow",cex=0.25);
		points(relativeSamples, labels, col="blue",cex=0.25);
		points(relativeSamples, predictions, col="pink",cex=0.25);
		abline(v=min(swallowdata$rdendsample)-samples[1],col="green",lwd=3);
		abline(v=min(swallowdata$rdstartsample)-samples[1],col="green",lwd=3)
	})
}


# computes the sample error for the labeled test data set ltd
#
# ltd <- the labeled test data set including columns y (the actual label) and predictions (the predicted value per time sample)
# 
# returns the error sum
computeSampleError<-function(ltd) {

# initialize return value to 0 (no error)
errorSum<-0

numSwallows=length(unique(ltd$Swallow))
logdebug('Predicting labels for %i swallows.', numSwallows)

# iterate through the unique values of the labeled test dataset ltd
for (SwallowId in unique(ltd$Swallow)) {

	# obtain the subset of the labeled test data set for Swallow ID 28 and store it in hh
	hh<-subset(ltd,Swallow==SwallowId)

	# as an information: this is the annotated pmax swallow
	#hh$pmaxsample_manuell[1]

	# as an information: These are the labels:
	#subset(hh, Sample>=hh$pmaxsample_manuell[1])$y

	remainder=subset(hh, Sample>=hh$pmaxsample_manuell[1])

	# the first element below 0:
	relative_length<-which(remainder$predictions<0.5)[1]

	# if the value could not be computed (e.g., no end transition found), set it to the last sample
	if (is.na(relative_length)) {
		relative_length=remainder$Sample[length(hh$Sample)]
		logwarn('No state transition from swallowing to non-swallowing was found for Swallow id %i. Using last sample (relative length=%i)', SwallowId, relative_length)
	}

	logdebug('Predicted restitution time for swallow %i is %i samples.', SwallowId, relative_length)

	# absolute end sample (the absolute time Sample)
	absolute_predicted_end_of_restitution_time<-remainder$pmaxsample_manuell[1]+relative_length

	logdebug('Predicted restitution time for swallow %i is %i samples (actual: %i).', SwallowId, relative_length, as.integer(remainder$tRestiAbsoluteSample[1]-remainder$pmaxsample_manuell[1]))
	# error:
	errorSum<-errorSum+abs(hh$tRestiAbsoluteSample[1]-absolute_predicted_end_of_restitution_time)
}

# return the computed errorsum:
return(errorSum/numSwallows)
}
