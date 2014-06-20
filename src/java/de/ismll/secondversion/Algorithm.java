package de.ismll.secondversion;

import java.util.Arrays;
import java.util.Random;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.database.dao.DataStoreException;
import de.ismll.database.dao.Entity;
import de.ismll.database.dao.Table;
import de.ismll.evaluation.Accuracy;
import de.ismll.exceptions.ModelApplicationException;
import de.ismll.lossFunctions.LossFunction;
import de.ismll.mhh.io.DataInterpretation;
import de.ismll.mhh.io.SwallowData;
import de.ismll.modelFunctions.LinearRegressionPrediction;
import de.ismll.modelFunctions.ModelFunctions;
import de.ismll.myfm.util.Printers;
import de.ismll.table.IntVector;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.impl.DefaultVector;
import de.ismll.table.projections.ColumnSubsetMatrixView;
import de.ismll.table.projections.IntVectorView;
import de.ismll.table.projections.RowSubsetMatrixView;
import de.ismll.table.projections.RowUnionMatrixView;
import de.ismll.utilities.Assert;

public class Algorithm implements Runnable{


	protected Logger log = LogManager.getLogger(getClass());

	private int maxIterations;
	private float stepSize;
	private int batchSize;
	
	private IntRange columnSelector;

	private int windowExtent;
	private float lambda;
	private String algorithmType;
	public LossFunction lossFunction;
	private ModelFunctions modelFunction;
	
	private String annotator;
	private String annotationBaseDir;

	private static final int COL_SAMPLE_IN_LABELS = 0;
	private static final int COL_LABEL_IN_LABELS = 1;



	private MhhDataset data;
	private MhhRawData rawData;
	
	private Matrix trainData;
	

	private DataInterpretation[] validationFolders;

	private String forWhat = "accuracy";


	private float bestAccuracySoFar = 0;
	private float bestSampleDifference = Integer.MAX_VALUE;
	
	
	private Database database;
	private long runKey;

	private int splitNumber;
	private int probandNumber;


	private boolean laplacian;
	private int smoothWindow;
	private float smoothReg;
	
	public ApplyMHHModelImpl apply;

	private boolean printParameters=false;
	private boolean useDatabase;



	@Override 
	public void run() {		
		Matrix rawTrainData = new RowUnionMatrixView(rawData.trainData);
		
		this.trainData = new DefaultMatrix( AlgorithmController.preprocess(rawTrainData, columnSelector));
		
		// Start iterating!
		float[] history = new float[10];
		int historyIdx=0;
		double oldRSS=Double.MAX_VALUE;
		
		
		Quality qualityOnVal;
		qualityOnVal = modelFunction.evaluateModel(rawData, this.windowExtent, columnSelector, "validation");
		
		Quality qualityOnTrain;
		qualityOnTrain = modelFunction.evaluateModel(rawData, this.windowExtent, columnSelector, "train");
		
		Vector allLabelsVector = Vectors.col(data.trainDataLabels, COL_LABEL_IN_LABELS);
		float major = modelFunction.computeMajorityClassAccuracy(allLabelsVector);
		
		log.info("ValAcc: " + qualityOnVal.getAccuracy() + " TrainAcc: " + qualityOnTrain.getAccuracy()  + " ValSD: " + 
				qualityOnVal.getSampleDifference() 
				+ " TrainSD: " + qualityOnTrain.getSampleDifference());
		
		

		for (int iteration = 0; iteration < maxIterations ; iteration++) {
			
			int[] randomBatch = lossFunction.computeRandomBatch(trainData.getNumRows(), 1000);
			
			Vector pointers1 =  new DefaultVector(randomBatch.length);
			
			for (int i = 0; i < pointers1.size() ; i++) {
				pointers1.set(i, randomBatch[i]);
			}
			
			IntVector pointers = new IntVectorView(pointers1);
		    
			Matrix currentTrainData = new DefaultMatrix( new RowSubsetMatrixView(trainData, pointers) );
			
			
			Matrix labelMatrix = new RowSubsetMatrixView(data.trainDataLabels, pointers);
			Vector labelsVector = Vectors.col(labelMatrix, 1);
			
			float[] allLabels = Vectors.toFloatArray(Vectors.col(data.trainDataLabels, 1));
			
//			Printers.printFloatArray(allLabels);
			
			
			float[] labels = Vectors.toFloatArray(labelsVector);
			lossFunction.iterate(modelFunction, currentTrainData, labels);
	
			
			// Evaluate the iteration and store best Parameters so Far

			
			
			qualityOnVal = modelFunction.evaluateModel(rawData, this.windowExtent, columnSelector, "validation");
			
			modelFunction.saveBestParameters(qualityOnVal, "accuracy");
						
			float accuracy = qualityOnVal.getAccuracy();
			
//			history[historyIdx++] = accuracy;
//			if (historyIdx>=history.length)
//				historyIdx=0;
//			if (iteration>=history.length-1) {
//				double accuracyVariance = Vectors.variance(DefaultVector.wrap(history));
////				log.info("Variance: " + accuracyVariance);
//				if (accuracyVariance > 0.03) { // uggh - hard-coded number :-(
//					throw new RuntimeException("Convergence variance too high - early stopping.");
//				}
//			}
			
			
			qualityOnTrain = modelFunction.evaluateModel(rawData, this.windowExtent, columnSelector, "train");
			
			log.info("iteration " + iteration  + "  ValAcc: " + qualityOnVal.getAccuracy() + " TrainAcc: " + qualityOnTrain.getAccuracy()  + " ValSD: " + 
					qualityOnVal.getSampleDifference() 
					+ " TrainSD: " + qualityOnTrain.getSampleDifference());
			
			float sampleDifference = qualityOnVal.getSampleDifference();
			float overshootPercentage = qualityOnVal.getOvershootPercentage();
			
			// Write the evaluation into the Database Table
			if (this.useDatabase) {
				try {
					database.addIteration(iteration, accuracy, sampleDifference, overshootPercentage, new float[] {1.5f},
								splitNumber, probandNumber, runKey );
					} catch (DataStoreException e) {
						log.fatal("Unable to connect to database!");
						e.printStackTrace();
					}
				
				
				}
			}
		
		
			
		
		
	}


	public float computeSigmoid(float value) {
		float result;
		result = (float) (1/(1 + Math.exp(-value)));
		return result;
	}

	public float computeSquaredSigmoid(float value) {
		float result;
		result = (float) ((Math.exp(-value))/((1 + Math.exp(-value))*(1 + Math.exp(-value))));
		return result;
	}


	//Getters and Setters!

	public int getMaxIterations() {
		return maxIterations;
	}

	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}

	public float getStepSize() {
		return stepSize;
	}

	public void setStepSize(float stepSize) {
		this.stepSize = stepSize;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public boolean isLaplacian() {
		return laplacian;
	}

	public void setLaplacian(boolean laplacian) {
		this.laplacian = laplacian;
	}

	public float getLambda() {
		return lambda;
	}

	public void setLambda(float in) {
		this.lambda = in;
	}

	public String getAlgorithmType() {
		return algorithmType;
	}

	public void setAlgorithmType(String algorithmType) {
		this.algorithmType = algorithmType;
	}


	public ModelFunctions getModelFunction() {
		return modelFunction;
	}

	public void setModelFunction(ModelFunctions modelFunction2) {
		this.modelFunction = modelFunction2;
	}

	public LossFunction getLossFunction() {
		return lossFunction;
	}

	public void setLossFunction(LossFunction descentDirection) {
		this.lossFunction = descentDirection;
	}

	public void setDatabase(Database db) {
		this.database = db;
	}


	public long getRunKey() {
		return runKey;
	}


	public void setRunKey(long runKey) {
		this.runKey = runKey;
	}


	public int getSplitNumber() {
		return splitNumber;
	}


	public void setSplitNumber(int splitNumber) {
		this.splitNumber = splitNumber;
	}


	public int getSmoothWindow() {
		return smoothWindow;
	}


	public void setSmoothWindow(int smoothWindow) {
		this.smoothWindow = smoothWindow;
	}


	public float getSmoothReg() {
		return smoothReg;
	}


	public void setSmoothReg(float smoothReg) {
		this.smoothReg = smoothReg;
	}


	public int getProbandNumber() {
		return probandNumber;
	}


	public void setProbandNumber(int probandNumber) {
		this.probandNumber = probandNumber;
	}


	public int getWindowExtent() {
		return windowExtent;
	}


	public void setWindowExtent(int windowExtent) {
		this.windowExtent = windowExtent;
	}


	public SwallowData[] getValidationFolders() {
		return validationFolders;
	}


	public void setValidationFolders(DataInterpretation[] validationFolders) {
		this.validationFolders = validationFolders;
	}


	public String getForWhat() {
		return forWhat;
	}


	public void setForWhat(String forWhat) {
		this.forWhat = forWhat;
	}


	public IntRange getColumnSelector() {
		return columnSelector;
	}


	public void setColumnSelector(IntRange columnSelector) {
		this.columnSelector = columnSelector;
	}


	public String getAnnotator() {
		return annotator;
	}


	public void setAnnotator(String annotator) {
		this.annotator = annotator;
	}


	public String getAnnotationBaseDir() {
		return annotationBaseDir;
	}


	public void setAnnotationBaseDir(String annotationBaseDir) {
		this.annotationBaseDir = annotationBaseDir;
	}


	public boolean isPrintParameters() {
		return printParameters;
	}


	public void setPrintParameters(boolean printParameters) {
		this.printParameters = printParameters;
	}


	public MhhDataset getData() {
		return data;
	}


	public void setData(MhhDataset data) {
		this.data = data;
	}


	public MhhRawData getRawData() {
		return rawData;
	}


	public void setRawData(MhhRawData rawData) {
		this.rawData = rawData;
	}


	public boolean isUseDatabase() {
		return useDatabase;
	}


	public void setUseDatabase(boolean useDatabase) {
		this.useDatabase = useDatabase;
	}

}
