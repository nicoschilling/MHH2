package de.ismll.secondversion;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.database.dao.DataStoreException;
import de.ismll.lossFunctions.LossFunction;
import de.ismll.mhh.io.DataInterpretation;
import de.ismll.mhh.io.SwallowData;
import de.ismll.modelFunctions.ModelFunctions;
import de.ismll.table.IntVector;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultIntVector;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.projections.ColumnSubsetMatrixView;
import de.ismll.table.projections.RowSubsetMatrixView;
import de.ismll.table.projections.RowUnionMatrixView;

public class Algorithm implements Runnable{

	protected Logger log = LogManager.getLogger(getClass());

	private int maxIterations;
	private float stepSize;
	private int batchSize;

	private IntRange columnSelector;

	private int windowExtent;
	private float lambda;
	private String algorithmType;
	private LossFunction lossFunction;
	private ModelFunctions modelFunction;

	private MhhRawData rawData;

	private Matrix trainData;

	private DataInterpretation[] validationFolders;

	private String forWhat = "accuracy";

	private Database database;
	private long runKey;

	private int splitNumber;
	private int probandNumber;

	private boolean laplacian;
	private int smoothWindow;

	private boolean printParameters=false;

	private boolean useDatabase;
	
	private boolean useValidation;


	@Override 
	public void run() {	
		
		Matrix[] rawLearnData;
		Matrix[] rawLearnLabels;
		int[] rawLearnAnnotations;
		
		
		Matrix[] rawApplyData;
		Matrix[] rawApplyLabels;
		int[] rawApplyAnnotations;
		
		
		if (this.useValidation) {
			// train is train and val is val
			rawLearnData = rawData.trainData;
			rawLearnLabels = rawData.trainDataLabels;
			rawLearnAnnotations = rawData.trainDataRelativeAnnotations;
			
			rawApplyData = rawData.validationData;
			rawApplyLabels = rawData.validationDataLabels;
			rawApplyAnnotations = rawData.validationDataRelativeAnnotations;
		}
		else {
			
			rawLearnData = new Matrix[rawData.trainData.length + rawData.validationData.length];
			rawLearnLabels = new Matrix[rawData.trainDataLabels.length + rawData.validationDataLabels.length];
			rawLearnAnnotations = new int[rawData.trainDataRelativeAnnotations.length + rawData.validationDataRelativeAnnotations.length];
			
			// unite!
			
			for (int i = 0; i < rawData.trainData.length ; i++) {
				rawLearnData[i] = rawData.trainData[i];
				rawLearnLabels[i] = rawData.trainDataLabels[i];
				rawLearnAnnotations[i] = rawData.trainDataRelativeAnnotations[i];
			}
			
			for (int i = 0; i < rawData.validationData.length ; i++) {
				rawLearnData[i+rawData.trainData.length] = rawData.validationData[i];
				rawLearnLabels[i+rawData.trainData.length] = rawData.validationDataLabels[i];
				rawLearnAnnotations[i+rawData.trainData.length] = rawData.validationDataRelativeAnnotations[i];
			}
			
			
			rawApplyData = rawData.testData;
			rawApplyLabels = rawData.testDataLabels;
			rawApplyAnnotations = rawData.testDataRelativeAnnotations;
			
		}
		
		Matrix learnData = new RowUnionMatrixView(rawLearnData);

		this.trainData = new DefaultMatrix( new ColumnSubsetMatrixView(learnData, columnSelector.getUsedIndexes()));
		
		Matrix trainLabels = new RowUnionMatrixView(rawLearnLabels);

		// Start iterating!

		Quality qualityOnLearn;
		qualityOnLearn = modelFunction.evaluateModel(rawLearnData, rawLearnLabels, rawLearnAnnotations,
				this.windowExtent, columnSelector);

		Quality qualityOnApply;
		qualityOnApply = modelFunction.evaluateModel(rawApplyData, rawApplyLabels, rawApplyAnnotations,
				 this.windowExtent, columnSelector);
		
		
		// IF USEVALIDATION THEN PREDICT ON Validation, no need to do anything with test!!!
		
		// IF TESTRUN, merge validation with train and PREDICT ON TEST :-)


		log.info("Learn Acc: " + qualityOnLearn.getAccuracy() + " Apply Acc: " + qualityOnApply.getAccuracy()  + " Learn SD: " + 
				qualityOnLearn.getSampleDifference() 
				+ " Apply SD: " + qualityOnApply.getSampleDifference());

		float bestAcc = 0;
		for (int iteration = 0; iteration < maxIterations ; iteration++) {

			int[] randomBatch = lossFunction.computeRandomBatch(trainData.getNumRows(), 100);

			IntVector pointers =  new DefaultIntVector(randomBatch.length);

			for (int i = 0; i < pointers.size() ; i++) {
				pointers.set(i, randomBatch[i]);
			}

			Matrix currentTrainData = new DefaultMatrix( new RowSubsetMatrixView(trainData, pointers) );

			Matrix labelMatrix = new RowSubsetMatrixView(trainLabels, pointers);
			Vector labelsVector = Vectors.col(labelMatrix, 1);

			float[] labels = Vectors.toFloatArray(labelsVector);

			if (isLaplacian()) {
				lossFunction.iterateLap(modelFunction, trainData, randomBatch, labels, this.smoothWindow);
			}
			else {
				lossFunction.iterate(modelFunction, currentTrainData, labels);
			}


			// Evaluate the iteration and store best Parameters so Far



			qualityOnLearn = modelFunction.evaluateModel(rawLearnData, rawLearnLabels, rawLearnAnnotations
					, this.windowExtent, columnSelector);
			
			qualityOnApply = modelFunction.evaluateModel(rawApplyData, rawApplyLabels, rawApplyAnnotations,
					this.windowExtent, columnSelector);

			modelFunction.saveBestParameters(qualityOnApply, "accuracy");

			float accuracy = qualityOnLearn.getAccuracy();
			
			if (accuracy > bestAcc) {
				bestAcc = accuracy;
			}

			log.info("iteration " + iteration + "  Learn Acc: " + qualityOnLearn.getAccuracy() + " Apply Acc: "
					+ qualityOnApply.getAccuracy() + " Learn SD: " + qualityOnLearn.getSampleDifference()
					+ " Apply SD: " + qualityOnApply.getSampleDifference());

			float sampleDifference = qualityOnApply.getSampleDifference();
			float overshootPercentage = qualityOnApply.getOvershootPercentage();

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
		
		System.out.println("bestAccuracy over full run is: " + bestAcc);

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

	public boolean isPrintParameters() {
		return printParameters;
	}


	public void setPrintParameters(boolean printParameters) {
		this.printParameters = printParameters;
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


	public boolean isUseValidation() {
		return useValidation;
	}


	public void setUseValidation(boolean useValidation) {
		this.useValidation = useValidation;
	}

}
