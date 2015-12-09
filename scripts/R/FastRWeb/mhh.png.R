library(logging)
library("FastRWeb")

basicConfig(level=20)

source("/home/bcsanbu/MHH2/scripts/R/iolib.R")

#swallow <- readSwallow("/home/bcsanbu/mhh/mhh.busche-it.de/data/ECDA2014/Splits/intra/Proband1/Split1/train/Schluck2/")

run <- function(swallow=1) {
	add.header("Cache-Control: max-age=1")
	pl<-WebPlot(width=640, height=480, type="png")
	swallow <- readSwallow(paste0("/home/bcsanbu/mhh/mhh.busche-it.de/data/ECDA2014/Splits/intra/Proband1/Split4/train/Schluck",swallow,"/"))
	#plotSwallow(swallow)
	plot(swallow$P5, col="#ff00ff")
	pl
}
