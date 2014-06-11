package de.ismll.blocknewton;

import de.ismll.bootstrap.Parameter;

public class Hyperparameters {

	@Parameter(cmdline="countSwallow")
	public static int countSwallow;

	@Parameter(cmdline="saveDir")
	public static String saveDir;

	@Parameter(cmdline="laplacian")
	public static boolean laplacian;

	@Parameter(cmdline="smoothSize")
	public static int smoothSize;

	@Parameter(cmdline="windowSize")
	public static int windowSize;

	@Parameter(cmdline="smoothReg")
	public static double smoothReg;

	@Parameter(cmdline="fileDir")
	public static String fileDir;

	@Parameter(cmdline="myDir")
	public static String myDir;

	@Parameter(cmdline="annotatorDir")
	public static String annotatorDir;

	@Parameter(cmdline="probandDir")
	public static String probandDir;

	@Parameter(cmdline="baseDir")
	public static String baseDir;

	@Parameter(cmdline="uniqueSamples")
	public static boolean uniqueSamples;

	@Parameter(cmdline="annotatorSwitch")
	public static boolean annotatorSwitch;

	@Parameter(cmdline="windowExtent")
	public static int windowExtent;

	@Parameter(cmdline="sample2Label")
	public static String sample2Label;

	@Parameter(cmdline="proband")
	public static int proband;  // bisher 1 und 2 und 1+2=12!!!

	@Parameter(cmdline="annotator")
	public static int annotator;  // 0 == MJ  else == SM

	@Parameter(cmdline="fold")
	public static int fold;

	@Parameter(cmdline="dataType")
	public static String dataType;

	@Parameter(cmdline="dataSizeFile")
	public static String dataSizeFile;

	@Parameter(cmdline="labelTrainFile")
	public static String labelTrainFile;

	@Parameter(cmdline="labelTestFile")
	public static String labelTestFile;

	@Parameter(cmdline="lossType")
	public static String lossType;

	@Parameter(cmdline="newtonType")
	public static String newtonType;

	@Parameter(cmdline="algorithmType")
	public static String algorithmType;

	@Parameter(cmdline="stepSize")
	public static double stepSize;

	@Parameter(cmdline="lambda")
	public static double lambda;

	@Parameter(cmdline="maxIterations")
	public static int maxIterations;

	@Parameter(cmdline="batchSize")
	public static int batchSize;

	@Parameter(cmdline="variables")             // Immer +1 wegen BIAS TERM!!!
	public static int variables;

	@Parameter(cmdline="trainInstances")
	public static int trainInstances;

	@Parameter(cmdline="testInstances")
	public static int testInstances;

	@Parameter(cmdline="trainfile")
	public static String trainfile;

	@Parameter(cmdline="testfile")
	public static String testfile;

	@Parameter(cmdline="adaptive")
	public static boolean adaptive;

	public static void setCountSwallow() {
		if (Hyperparameters.proband == 1)
		{
			Hyperparameters.countSwallow = 10;
		}
		else if ( Hyperparameters.proband == 2)
		{
			Hyperparameters.countSwallow = 11;
		}
		else if (Hyperparameters.proband == 12)
		{
			Hyperparameters.countSwallow = 10;
		}
	}

	public static String getSaveDir() {
		return saveDir;
	}

	public static void setSaveDir(String saveDir) {
		Hyperparameters.saveDir = saveDir;
	}

	public static boolean isLaplacian() {
		return laplacian;
	}

	public static void setLaplacian(boolean laplacian) {
		Hyperparameters.laplacian = laplacian;
	}

	public static int getWindowSize() {
		return windowSize;
	}

	public static void setWindowSize(int windowSize) {
		Hyperparameters.windowSize = windowSize;
	}

	public static int getSmoothSize() {
		return smoothSize;
	}

	public static double getSmoothReg() {
		return smoothReg;
	}

	public static void setSmoothReg(double smoothReg) {
		Hyperparameters.smoothReg = smoothReg;
	}

	public static void setSmoothSize(int smoothSize) {
		Hyperparameters.smoothSize = smoothSize;
	}

	public static String getFileDir() {
		return fileDir;
	}

	public static void setFileDir(String fileDir) {
		Hyperparameters.fileDir = fileDir;
	}

	public static String getMyDir() {
		return myDir;
	}

	public static void setMyDir(String myDir) {
		Hyperparameters.myDir = myDir;
	}

	public static void setDirectories() {

		setBaseDir("/home/"+myDir+"/Documents/MHH/Datasets");   // DAS NERVT MIT DATASETS2!!!

		switch (Hyperparameters.annotator)
		{
		case 0:
			setAnnotatorDir("/MJ");
			break;

		case 1:
			setAnnotatorDir("/SM");
			break;

		case 2:
			setAnnotatorDir("/gemein");
			break;

		default:
			System.err.println("0 == MJ, 1 == SM, 2 == gemein");
			break;
		}

		switch (Hyperparameters.proband)
		{
		case 1:
			setProbandDir("/Proband1");
			break;
		case 2:
			setProbandDir("/Proband2");
			break;

		default:
			System.err.println("Proband 1 und 2!");
			break;
		}
	}

	public static void setSaveDirectory() {
		if (Hyperparameters.annotatorSwitch == false)
		{
			if (Hyperparameters.laplacian == false)
			{
				Hyperparameters.setSaveDir(Hyperparameters.baseDir+Hyperparameters.annotatorDir+Hyperparameters.probandDir);
			}
			else
			{
				Hyperparameters.setSaveDir(Hyperparameters.baseDir+Hyperparameters.annotatorDir+Hyperparameters.probandDir+"/laplacian");
			}
		}
		else
		{
			if (Hyperparameters.laplacian == false)
			{
				Hyperparameters.setSaveDir(Hyperparameters.baseDir+Hyperparameters.annotatorDir+Hyperparameters.probandDir+"/annotatorSwitch");
			}
			else
			{
				Hyperparameters.setSaveDir(Hyperparameters.baseDir+Hyperparameters.annotatorDir+Hyperparameters.probandDir+"/laplacian/annotatorSwitch");
			}
		}
	}

	public static String getAnnotatorDir() {
		return annotatorDir;
	}

	public static void setAnnotatorDir(String annotatorDir) {
		Hyperparameters.annotatorDir = annotatorDir;
	}

	public static String getProbandDir() {
		return probandDir;
	}

	public static void setProbandDir(String probandDir) {
		Hyperparameters.probandDir = probandDir;
	}

	public static String getBaseDir() {
		return baseDir;
	}

	public static void setBaseDir(String baseDir) {
		Hyperparameters.baseDir = baseDir;
	}

	public static boolean isUniqueSamples() {
		return uniqueSamples;
	}

	public static void setUniqueSamples(boolean uniqueSamples) {
		Hyperparameters.uniqueSamples = uniqueSamples;
	}

	public static boolean isAnnotatorSwitch() {
		return annotatorSwitch;
	}

	public static void setAnnotatorSwitch(boolean annotatorSwitch) {
		Hyperparameters.annotatorSwitch = annotatorSwitch;
	}

	public static int getWindowExtent() {
		return windowExtent;
	}

	public static void setWindowExtent(int windowExtent) {
		Hyperparameters.windowExtent = windowExtent;
	}

	public static String getSample2Label() {
		return sample2Label;
	}

	public static void setSample2Label(String sample2Label) {
		Hyperparameters.sample2Label = sample2Label;
	}

	public static int getProband() {
		return proband;
	}

	public static void setProband(int proband) {
		Hyperparameters.proband = proband;
	}

	public static void setTrainInstancesFiles( int fold, IO input) {
		setTrainInstances(input.dataSizes[fold][1]); 
		setTrainfile(baseDir+annotatorDir+probandDir+"/train-s-"+fold+".csv");
		setLabelTrainFile(baseDir+annotatorDir+probandDir+"/train-s-"+fold+".csv_labels");
	}

	public static void setTestInstancesFiles( int fold, IO input) {
		setTestInstances(input.dataSizes[fold][2]);
		setTestfile(baseDir+annotatorDir+"/Proband3/test-s-"+fold+".csv");
		setLabelTestFile(baseDir+annotatorDir+"/Proband3/test-s-"+fold+".csv_labels");
	}

	public static void setInstancesFiles(int fold, IO input) {



		setTrainInstances(input.dataSizes[fold][1]);   
		setTestInstances(input.dataSizes[fold][2]);

		setTrainfile(baseDir+annotatorDir+probandDir+"/train-s-"+fold+".csv");
		setTestfile(baseDir+annotatorDir+probandDir+"/test-s-"+fold+".csv");
		setLabelTrainFile(baseDir+annotatorDir+probandDir+"/train-s-"+fold+".csv_labels");

		if(Hyperparameters.annotatorSwitch == false)
		{
			setLabelTestFile(baseDir+annotatorDir+probandDir+"/test-s-"+fold+".csv_labels");
		}
		else
		{
			switch (Hyperparameters.annotator)
			{
			case 0: //MJ
				setLabelTestFile(baseDir+"/SM"+probandDir+"/test-s-"+fold+".csv_labels");
				break;

			case 1: //SM
				setLabelTestFile(baseDir+"/MJ"+probandDir+"/test-s-"+fold+".csv_labels");
				break;

			case 2: //gemein
				setLabelTestFile(baseDir+annotatorDir+probandDir+"/test-s-"+fold+".csv_labels");
				break;

			default:
				System.out.println("annotator wrong!!");
				break;
			}
		}

	}

	public static int getAnnotator() {
		return annotator;
	}

	public static void setAnnotator(int annotator) {
		Hyperparameters.annotator = annotator;
	}

	public static int getFold() {
		return fold;
	}

	public static void setFold(int fold) {
		Hyperparameters.fold = fold;
	}

	public static String getDataSizeFile() {
		return dataSizeFile;
	}

	public static void setDataSizeFile(String dataSizeFile) {
		Hyperparameters.dataSizeFile = dataSizeFile;
	}

	public static String getDataType() {
		return dataType;
	}

	public static void setDataType(String dataType) {
		Hyperparameters.dataType = dataType;
	}



	public static String getLabelTrainFile() {
		return labelTrainFile;
	}

	public static void setLabelTrainFile(String labelTrainFile) {
		Hyperparameters.labelTrainFile = labelTrainFile;
	}

	public static String getLabelTestFile() {
		return labelTestFile;
	}

	public static void setLabelTestFile(String labelTestFile) {
		Hyperparameters.labelTestFile = labelTestFile;
	}



	public void setLossType(String lossType) {
		Hyperparameters.lossType = lossType;
	}

	public String getLossType() {
		return lossType;
	}

	public static String getNewtonType() {
		return newtonType;
	}

	public static void setNewtonType(String newtonType) {
		Hyperparameters.newtonType = newtonType;
	}

	public static String getAlgorithmType() {
		return algorithmType;
	}

	public static void setAlgorithmType(String algorithmType) {
		Hyperparameters.algorithmType = algorithmType;
	}

	public static double getStepSize(int update) {
		if (Hyperparameters.adaptive == false)
		{
			// Leave step size as it is!!
		}
		else
		{
			stepSize = stepSize/(update+1);
		}
		return stepSize;
	}

	public static void setStepSize(double stepSize) {
		Hyperparameters.stepSize = stepSize;
	}

	public static double getLambda() {
		return lambda;
	}

	public static void setLambda(double lambda) {
		Hyperparameters.lambda = lambda;
	}

	public static int getMaxIterations() {
		return maxIterations;
	}

	public static void setMaxIterations(int maxIterations) {
		Hyperparameters.maxIterations = maxIterations;
	}

	public static int getBatchSize() {
		return batchSize;
	}

	public static void setBatchSize(int batchSize) {
		Hyperparameters.batchSize = batchSize;
	}

	public static int getVariables() {
		return variables;
	}

	public static void setVariables(int variables) {
		Hyperparameters.variables = variables;
	}

	public static int getTrainInstances() {
		return trainInstances;
	}

	public static void setTrainInstances(int trainInstances) {
		Hyperparameters.trainInstances = trainInstances;
	}

	public static int getTestInstances() {
		return testInstances;
	}

	public static void setTestInstances(int testInstances) {
		Hyperparameters.testInstances = testInstances;
	}

	public static String getTrainfile() {
		return trainfile;
	}

	public static void setTrainfile(String trainfile) {
		Hyperparameters.trainfile = trainfile;
	}

	public static String getTestfile() {
		return testfile;
	}

	public static void setTestfile(String testfile) {
		Hyperparameters.testfile = testfile;
	}

	public static boolean isAdaptive() {
		return adaptive;
	}

	public static void setAdaptive(boolean adaptive) {
		Hyperparameters.adaptive = adaptive;
	}








}
