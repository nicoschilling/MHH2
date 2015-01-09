# read a swallow from path and return it as a data.frame
readSwallow <- function (path) {
setwd(path)

sensors<-read.csv("data.csv",header=FALSE)
colnames(sensors)<-c("Sample","P1","P2","P3","P4","P5","P6","P7","P8","P9","P10","P11","P12","P13","P14","P15","P16","P17","P18","P19","P20","Resp1","Resp2","Resp3","Swallow1","Swallow2","Swallow3","Marker","?")
fft<-read.csv("fft.csv",header=FALSE)
data<-cbind(sensors,fft)

#sphinctermax<-read.csv("subset-max.csv",header=FALSE)
#data$sphinctermaxV1<-sphinctermax$V1
#data$sphinctermaxV2<-sphinctermax$V2

# read raw data
rdstart<-read.csv("rdstart",header=FALSE)
rdend<-read.csv("rdend",header=FALSE)
channelstart<-as.integer(read.csv("channelstart",header=FALSE))
channelend<-as.integer(read.csv("channelend",header=FALSE))

#     V1
# 1 04:05
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
data$pmaxSphincer<-apply(data[,grep(paste0("P",channelstart),colnames(data)):grep(paste0("P",channelend),colnames(data))] ,1,max)

# encode in {0,1} whether a sample denotes the "ruhedruck". 0->swallow-something; 1->ruhedruck
data$isrd<-0
data$isrd[ data$Sample>data$rdstartsample & data$Sample<data$rdendsample ]<-1

# extract pmax_manuell
tmp<-strsplit(as.character(read.table("pmax_manuell",header=FALSE)[[1]]),"[:,]")[[1]]
data$pmax<-0
data$pmax<-as.integer(tmp[[1]])*50*60 + as.integer(tmp[[2]])*50+ as.integer(tmp[[3]])/100*50

# again, do binary encoding of the pmax area
data$ispostpmax<-0
data$ispostpmax[ data$Sample>=data$pmax ]<-1

# include swallow id and proband id
data$Swallow<-read.csv("id",header=FALSE)[[1]]
data$Proband<-read.csv("proband",header=FALSE)[[1]]

# adjust data types
data$Sample<-as.integer(data$Sample)
#data$Swallow<-as.factor(data$Swallow)
#data$Proband<-as.factor(data$Proband)
data$pmax<-as.integer(data$pmax)
data$rdendsample<-as.integer(data$rdendsample)
data$rdstartsample<-as.integer(data$rdstartsample)

return (data)
} # of function readSwallow

# read the annotations from the given annotation file using the given proband id.
readAnnotation <-function(annotationfile,Proband,samplerate=50) {
# annotationfile=~/mhh/busche-it.de/data/manual_annotations/ECDA-Annotations/1-sm.tsv
annotations<-read.csv(annotationfile,header=FALSE,sep='\t')
colnames(annotations)<-c("Swallow","RD","Pmax","V4","PmaxZeit","tRestiDuration","V7","tRestiAbsolute")
# store proband as dedicated column
annotations$Proband<-Proband

# convert times to samples
annotations$tRestiAbsoluteSample<-sapply(annotations$tRestiAbsolute,FUN=function(x) {tmp<-strsplit(as.character(x)[[1]],"[:,]")[[1]]; return(as.integer(tmp[[1]])*samplerate*60 + as.integer(tmp[[2]])*samplerate+ as.integer(tmp[[3]])/100*samplerate)})
annotations$PmaxZeitSample<-sapply(annotations$PmaxZeit,FUN=function(x) {tmp<-strsplit(as.character(x)[[1]],"[:,]")[[1]]; return(as.integer(tmp[[1]])*samplerate*60 + as.integer(tmp[[2]])*samplerate+ as.integer(tmp[[3]])/100*samplerate)})

return(annotations)
} # of function readAnnotation

# infers the labels (column y added) from the merged dataset
inferLabels<-function(data_and_annotations){
# infer labels. Unknown labels everywhere:
data_and_annotations$y<-'?'
# non-swallow labels in the "ruhedruck" area and beyond the annotation
data_and_annotations$y[ data_and_annotations$isrd==1 ]<-0
data_and_annotations$y[ data_and_annotations$Sample>= data_and_annotations$tRestiAbsoluteSample]<-0
# swallow-labels between the pmax sample and the annotation
data_and_annotations$y[ data_and_annotations$ispostpmax==1 &  data_and_annotations$Sample< data_and_annotations$tRestiAbsoluteSample]<-1
# or:
data_and_annotations$y[ data_and_annotations$Sample>=data_and_annotations$pmax & data_and_annotations$Sample< data_and_annotations$tRestiAbsoluteSample]<-1
return (data_and_annotations)
}

# plots the swallow from the given data.frame
plotSwallow<-function(swallowdata) {
# the colums from P1..P20
columnrange<-min(grep("P1",colnames(swallowdata))):grep("P20",colnames(swallowdata))

# extract absolute samples and base them on 1
samples<-swallowdata[ ,1]
relativeSamples<-samples - min(samples) + 1
columns<-seq(1,20)

# extract max pressure curve
maxPressures<-sapply(swallowdata$pmaxSphincer,FUN=function(x){(max(x)/500 )*15  +3})

# additional aspects:
# visualization whether we are beyond the pmax area
#		points(relativeSamples, (swallowdata$ispostpmax+2)*2, col="blue",cex=0.25);
# visualization whether we are in the "ruhedruck"
#		points(relativeSamples, (swallowdata$isrd+2)*2, col="blue",cex=0.25);

filled.contour(
	relativeSamples,
	columns,
	log(as.matrix(swallowdata[,columnrange])+100),
	color = terrain.colors,
	plot.axes = { axis(1); axis(2); 
		points(relativeSamples, maxPressures, col="yellow",cex=0.25);
		abline(v=min(swallowdata$rdendsample)-samples[1],col="green",lwd=3);
		abline(v=min(swallowdata$rdstartsample)-samples[1],col="green",lwd=3)
	})
}
