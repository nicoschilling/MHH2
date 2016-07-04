#!/usr/bin/Rscript

#TODO: Maybe include a second parameter for the data interpretation as not all values are in the jobDirectory (SwallowData)

#include all necessary packages

suppressMessages(library(gplots))

#at first, read the input job directory from where the data will be read!

args <- commandArgs(trailingOnly = TRUE)

jobDirectory = args[1]
resultDirectory = args[2]

cat("Reading Data from the following job Directory:",jobDirectory, " \n")
cat("Reading Results from the following Directory:",resultDirectory, " \n")

# Read datafile as jobDirectory/data.csv

dataFile = paste(jobDirectory,"/data.csv", sep="")
data = read.csv(dataFile,header=FALSE)

# Get the dimensions

nrRows = dim(data)[1]
nrColumns = dim(data)[2]

# Convert to Matrix for being able to do a contour plot

data = as.matrix(data)

# Extract samples and pressure values, there are 20 sensors for pressure!!
samples = data[ ,1]
relativeSamples = samples - min(samples) + 1
columns = seq(1,22)
pressures = data[ , seq(2,23)]

# Print data statistics

cat("First sample in the swallow is:",min(relativeSamples), " Last sample in the swallow is:", max(relativeSamples), " \n")

#Read Samplerate

sampleRateFile = paste(jobDirectory,"/samplerate",sep="")
sampleRate = scan(sampleRateFile,quiet=TRUE)

cat("SampleRate is:" , sampleRate, "\n")

#Read Resting Pressure Start and End

rdStartFile = paste(jobDirectory,"/rdstart",sep="")
rdEndFile = paste(jobDirectory,"/rdend",sep="")

rdStart = scan(rdStartFile,what="character",sep=":",quiet=TRUE)
rdEnd = scan(rdEndFile,what="character",sep=":",quiet=TRUE)

rdStartSample = ( as.numeric(rdStart[1])*60 + as.numeric(rdStart[2]) )*sampleRate
rdEndSample = ( as.numeric(rdEnd[1])*60 + as.numeric(rdEnd[2]) )*sampleRate

relativeRdStartSample = rdStartSample - min(samples) + 1
relativeRdEndSample = rdEndSample - min(samples) + 1

cat("Resting Pressure starts at sample:" , relativeRdStartSample, "\n")
cat("Resting Pressure ends at sample:" , relativeRdEndSample, "\n")

#Now read predicted and true Annotations!

trueAnnotationFile = paste(resultDirectory,"/true_sample", sep="")
trueAnnotation = scan(trueAnnotationFile,quiet=TRUE)
relativeTrueAnnotation=trueAnnotation- min(samples) +1

predictedAnnotationFile = paste(resultDirectory,"/end_sample", sep="")
predictedAnnotation = scan(predictedAnnotationFile,quiet=TRUE)

cat("Annotated Restitution is at sample:", relativeTrueAnnotation, " Predicted Restitution is at sample:", predictedAnnotation, " \n")

if (trueAnnotation > max(relativeSamples) ) {
	cat("Annotated Restitution exceeds Swallow length! Will be set to:", max(relativeSamples), " \n")
	trueAnnotation = max(relativeSamples)
}

# And also read the Sphincters

channelStartFile = paste(jobDirectory,"/channelstart",sep="")
channelEndFile = paste(jobDirectory,"/channelend",sep="")

channelStart = scan(channelStartFile,quiet=TRUE)
channelEnd = scan(channelEndFile,quiet=TRUE)

cat("Upper Sphincter sensor is:" , channelEnd, "\n")
cat("Lower Sphincter sensor is:" , channelStart, "\n")

#Last but not least, read the sample2avgLabels

sample2avgLabelsFile = paste(resultDirectory, "/sample2avgLabels",sep="")

sample2avgLabels = as.matrix(read.csv(sample2avgLabelsFile,header=FALSE))
avgLabels = sample2avgLabels[ , 2]
labeledSamples = sample2avgLabels[ , 1]
avgLabels = (avgLabels + 2) * 5 

if (length(labeledSamples) != length(relativeSamples) ) {
	cat("Predictions have not been computed for every sample, will interpolate in empty regions...", " \n")
	
}

#Now the hard stuff, plot this damn thing

#Compute log of the pressures

logPressures = log(pressures + 100)

#Compute the maximumPressureCurve, over the subset of Sphincter Pressures

sphincterPressures = pressures[ , seq(channelStart,channelEnd)]
maxPressures = rep(0,nrRows)

for (i in 1:nrRows) {
	maxPressures[i] = (max( sphincterPressures[ i , ]/500 )*15 ) +3
}

#Define a range of colors

colors = colorpanel(25,low="#0B0B61",high="#E6E0F8")

savePlotFile = paste(resultDirectory, "/visualization.jpg", sep="")

jpeg(savePlotFile)

filled.contour(relativeSamples,columns,logPressures,nlevels=15,col=colors,plot.title= title(main="Swallow Diagram",xlab="Samples",ylab="Sensors"),
 plot.axes = { axis(1); axis(2); points(relativeSamples, maxPressures, col="yellow",cex=0.25); points(labeledSamples,avgLabels,col="orange",cex=0.5) ;
 abline(v=relativeRdStartSample,col="green",lwd=3) ; abline(v=relativeRdEndSample,col="green",lwd=3) ; abline(v=relativeTrueAnnotation,col="red",lwd=3) ;
 abline(v=predictedAnnotation,col="blue",lwd=3) } ,
 key.axes = { }
)

garbage = dev.off()


cat("Plot has been successfully created!  \n")


