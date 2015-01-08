# read a swallow from path and return it as a data.frame
readSwallow <- function (path) {
setwd(path)

sensors<-read.csv("data.csv",header=FALSE)
colnames(sensors)<-c("Sample","P1","P2","P3","P4","P5","P6","P7","P8","P9","P10","P11","P12","P13","P14","P15","P16","P17","P18","P19","P20","Resp1","Resp2","Resp3","Swallow1","Swallow2","Swallow3","Marker","?")
fft<-read.csv("fft.csv",header=FALSE)
data<-cbind(sensors,fft)

sphinctermax<-read.csv("subset-max.csv",header=FALSE)
data$sphinctermaxV1<-sphinctermax$V1
data$sphinctermaxV2<-sphinctermax$V2

# read raw data
rdstart<-read.csv("rdstart",header=FALSE)
rdend<-read.csv("rdend",header=FALSE)
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

return (data)
} # of function readSwallow


