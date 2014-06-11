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
import de.ismll.modelFunctions.ModelFunctions;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultVector;
import de.ismll.table.projections.ColumnSubsetMatrixView;
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

	// Values for randomly initializing the parameters
	private float variance = 1;
	private float expectationValue = 0;
	private Random random = new Random(0);

	private float[][] trainData;
	private float[] trainDataLabels;
	private float[][] trainInstanceWeights;
	
	private float[][] holdData;
	private float[] holdDataLabels;
	

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



	@Override 
	public void run() {
		if (trainData.length != trainDataLabels.length) {
			log.fatal("Train Data and Train Labels have not the same length!!");
		}
		
		// Intialize model function parameters depending on datasize and hyperparameters
		
		float[] parameters = initializeParameters(trainData); // TODO

		if (parameters==null) {log.warn("Parameters have not been initialized!"); }
		else { log.debug("Parameters have been initialized"); }

		this.finalParametersArray = new float[parameters.length];
		
		
		
		// Set the validation data for evaluating iterations
		
		try {
			apply = new ApplyMHHModelImpl();
			apply.setColumnSelector(columnSelector);
			apply.setAnnotationBaseDir(annotationBaseDir);
			apply.setAnnotator(annotator);
			apply.setValidation(validationFolders);
			apply.setWindowExtent(windowExtent);
		} catch (ModelApplicationException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		// Start iterating!
		float[] history = new float[10];
		int historyIdx=0;
		double oldRSS=Double.MAX_VALUE;

		for (int iteration = 0; iteration < maxIterations ; iteration++) {
			
			// Compute a batch of instances

			int[] idxInstances = computeRandomBatch();
			
			idxInstances=new int[trainData.length];
			for (int i = 0; i < trainData.length; i++)
				idxInstances[i]=i;
				
					
//			float[] instanceWeights = new float[idxInstances.length];
//			
//			for (int i = 0 ; i < idxInstances.length ; i++ ) {
//				int instance = idxInstances[i];
//				
//				for ( int j = 0; j < trainInstanceWeights.length ; j++) {
//					if (instance == trainInstanceWeights[j][0]) {
//						instanceWeights[j] = trainInstanceWeights[j][1];
//					}
//				}
//			}
			
			// Do the iteration

			if (laplacian){
				lossFunction.iterateLaplacian(parameters, idxInstances, trainData, trainDataLabels,
						modelFunction, smoothWindow, smoothReg);
			}
			else {
				lossFunction.iterate(parameters, idxInstances, trainData, trainDataLabels, modelFunction);
			}
			
			// Evaluate the iteration and store best Parameters so Far
			
			Quality quality = null;
			try {
				quality = storeParametersBestSoFar(parameters, getForWhat());
			} catch (ModelApplicationException e1) {
				e1.printStackTrace();
				System.exit(1);
			}
			double rss = 0;
			for (float f : parameters)
				rss += f*f;
			if (rss > oldRSS) {
				throw new RuntimeException("RSS did not decrease (stepsize=" + stepSize + " too large?) - early stopping.");
			}
			oldRSS = rss;
			
			float accuracy = quality.getAccuracy();
			history[historyIdx++] = accuracy;
			if (historyIdx>=history.length)
				historyIdx=0;
			if (iteration>=history.length-1) {
				double accuracyVariance = Vectors.variance(DefaultVector.wrap(history));
				log.info("Variance: " + accuracyVariance);
				if (accuracyVariance > 0.03) { // uggh - hard-coded number :-(
					throw new RuntimeException("Converence variance too high - early stopping.");
				}
			}
			
			float sampleDifference = quality.getSampleDifference();
			float overshootPercentage = quality.getOvershootPercentage();
			
			// Write the evaluation into the Database Table
			
			try {
				database.addIteration(iteration, accuracy, sampleDifference, overshootPercentage, parameters,
							splitNumber, probandNumber, runKey );
				} catch (DataStoreException e) {
					log.fatal("Unable to connect to database!");
					e.printStackTrace();
				}
			}
		
		// End with final best parameters
		this.finalParameters = DefaultVector.wrap(finalParametersArray);
	}


	/** 
	 * Evaluates current parameters on holdData, keeps track of the best Parameters on the holdData!!
	 * @param parameters, forWhat
	 * @throws ModelApplicationException 
	 */
	private Quality storeParametersBestSoFar(float[] parameters, String forWhat) throws ModelApplicationException {
		
		// Use the output of apply on validation for storing best parameters
		
		Quality quality = apply.predictForValidation(Vectors.floatArraytoVector(parameters));
		double rss = 0;
		for (float f : parameters)
			rss += f*f;
		
		log.info(" Average Accuracy on Validation is: " + quality.getAccuracy() + " Average SampleDifference is: " + 
					quality.getSampleDifference() + " Average Overshoot is: " + quality.getOvershootPercentage() + " RSS: " + rss + ((printParameters)?" Parameters: " + Arrays.toString(parameters):""));
		
		if( getForWhat() == "accuracy") {
			if (quality.getAccuracy() > bestAccuracySoFar) {
				bestAccuracySoFar = quality.getAccuracy();
				finalParametersArray = parameters;
			}
		}
		else if (getForWhat() == "sampleDifference") {
			if (quality.getSampleDifference() < bestSampleDifference) {
				bestSampleDifference = quality.getSampleDifference();
				finalParametersArray = parameters;
			}
		}
		else {
			log.error("The Algorithm does not know what to optimize for!! accuracy or sampleDifference?");
			System.exit(1);
		}
		
		return quality;
	}

	public float[] initializeParameters(float[][] data) {
		float[] ret = new float[data[0].length];

		for (int i = 0; i < ret.length; i++) {
			ret[i] = (float) (random.nextGaussian() * variance + expectationValue);
		}

		return ret;
	}


	public int[] computeRandomBatch() {
		int[] batch = new int[batchSize];
		for (int dimension = 0; dimension < batchSize ; dimension++)
		{
			batch[dimension] =  (int) (random.nextDouble()*trainData.length);	}
		return batch;
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

	public Vector getFinalParameters() {
		return finalParameters;
	}

	public void setFinalParameters(Vector finalParameters) {
		this.finalParameters = finalParameters;
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

	public float[][] getTrainData() {
		return trainData;
	}

	public void setTrainData(float[][] trainData) {
		this.trainData = trainData;
	}

	public float[] getTrainDataLabels() {
		return trainDataLabels;
	}

	public void setTrainDataLabels(float[] trainDataLabels) {
		this.trainDataLabels = trainDataLabels;
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


	public float[][] getTrainInstanceWeights() {
		return trainInstanceWeights;
	}


	public void setTrainInstanceWeights(float[][] trainInstanceWeights) {
		this.trainInstanceWeights = trainInstanceWeights;
	}


	public float[][] getHoldData() {
		return holdData;
	}


	public void setHoldData(float[][] holdData) {
		this.holdData = holdData;
	}


	public float[] getHoldDataLabels() {
		return holdDataLabels;
	}


	public void setHoldDataLabels(float[] holdDataLabels) {
		this.holdDataLabels = holdDataLabels;
	}


	public boolean isPrintParameters() {
		return printParameters;
	}


	public void setPrintParameters(boolean printParameters) {
		this.printParameters = printParameters;
	}

}
