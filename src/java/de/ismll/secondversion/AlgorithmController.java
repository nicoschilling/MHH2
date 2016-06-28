package de.ismll.secondversion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.bootstrap.BootstrapException;
import de.ismll.bootstrap.CommandLineParser;
import de.ismll.bootstrap.Parameter;
import de.ismll.database.dao.DataStoreException;
import de.ismll.evaluation.Accuracy;
import de.ismll.exceptions.ModelApplicationException;
import de.ismll.lossFunctions.LossFunction;
import de.ismll.mhh.featureExtractors.AcidFeatureExtractor;
import de.ismll.mhh.featureExtractors.AllExtractor;
import de.ismll.mhh.featureExtractors.LowerExtractor;
import de.ismll.mhh.featureExtractors.LowerMiddleExtractor;
import de.ismll.mhh.featureExtractors.MiddleExtractor;
import de.ismll.mhh.featureExtractors.PatientFeatureExtractor;
import de.ismll.mhh.featureExtractors.TimeFeatureExtractor;
import de.ismll.mhh.featureExtractors.UpperExtractor;
import de.ismll.mhh.featureExtractors.UpperMiddleExtractor;
import de.ismll.mhh.io.Parser;
import de.ismll.mhh.io.DataInterpretation;
import de.ismll.mhh.io.SwallowData;
import de.ismll.modelFunctions.FmModel;
import de.ismll.modelFunctions.LinearRegressionPrediction;
import de.ismll.modelFunctions.ModelFunctions;
import de.ismll.myfm.util.Printers;
import de.ismll.processing.Normalizer;
import de.ismll.table.IntMatrix;
import de.ismll.table.IntVector;
import de.ismll.table.IntVectors;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultBitVector;
import de.ismll.table.impl.DefaultIntMatrix;
import de.ismll.table.impl.DefaultIntVector;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.impl.DefaultVector;
import de.ismll.table.projections.ColumnSubsetMatrixView;
import de.ismll.table.projections.ColumnUnionMatrixView;
import de.ismll.table.projections.RowSubsetIntMatrix;
import de.ismll.table.projections.RowSubsetIntVector;
import de.ismll.table.projections.RowSubsetMatrixView;
import de.ismll.table.projections.RowSubsetVector;
import de.ismll.table.projections.RowUnionMatrixView;
import de.ismll.table.projections.VectorAsMatrixView;
import static de.ismll.secondversion.DatasetFormat.*;

/**
 * build data matrices, dependent on ReadSplit
 * 
 * @author Andre Busche
 * 
 */
public class AlgorithmController  implements Runnable{

	final static String LINE_SEPARATOR = System.lineSeparator();

	/**
	 * number of leading meta columns
	 */
	public static final int NUM_META_COLUMNS = META_COLUMNS.length;

	@Parameter(cmdline="applyFolder" , description="the folder to apply a learned model on")
	public String applyFolder;

	@Parameter(cmdline="smallBatch" , description="specifies, whether a small batch is used!")
	private boolean smallBatch = false;

	@Parameter(cmdline="splitFolder", description="the split folder ;-)")
	public ReadSplit readSplit;

	@Parameter(cmdline="weightLength", description="one-sided distance, that specifies, whether instance weights are smaller than 1")
	private float weightLength = 50;

	@Parameter(cmdline="annotator" , description="the annotator, either mj,sm or gemein")
	public String annotator="gemein";

	@Parameter(cmdline="includeRD", description="set to true if RD Data should be included")
	public boolean includeRD;

	@Parameter(cmdline="windowExtent", description="Hyperparameter: The length (one sided) that smoothes the predicted labels")
	public int windowExtent;

	@Parameter(cmdline="maxIterations", description="number of maximum iterations for train() Method")
	public int maxIterations = 1000;

	@Parameter(cmdline="timeOrder")
	private int timeOrder = 3;


	@Parameter(cmdline="batchSize", description="Hyperparameter: number of instances used to compute a gradient, i.e. 1 -> stochastic  trainInstances -> full")
	public int batchSize = 0;

	@Parameter(cmdline="laplacian", description="true, if laplacian regularization is wanted, false, otherwise.")
	public boolean laplacian;

	@Parameter(cmdline="smoothWindow" , description="Window, over which the classification is smoothed.")
	private int smoothWindow;

	@Parameter(cmdline="smoothReg" , description="Parameter, that specifies the influence of the Laplacian regularization")
	private float smoothReg;

	@Parameter(cmdline="descentDirection" , description="Abstract Class. Input a String that specifies the loss function, e.g. \"logisticLoss\".")
	public LossFunction lossFunction;

	@Parameter(cmdline="useValidation" , description="specifies, if validation is used (true) or test (false).")
	public boolean useValidation;

	@Parameter(cmdline="modelFunction", description="Specifies the model Function to be used, i.e. a linear Model, factorizationMachine etc.")
	public ModelFunctions modelFunction;

	@Parameter(cmdline="columnSelector", description="Optional. If given, selects a subset of columns. WARNING: If given, make sure to exclude, e.g., the sample Index (col 0)!")
	public IntRange columnSelector;

	@Parameter(cmdline="runTable", description="Name of the run Table")
	private String runTable = "run";

	@Parameter(cmdline="iterTable", description="Name of the iteration Table")
	private String iterTable = "iter";

	@Parameter(cmdline="runLapTable" , description="Name of the run Table for Laplacian Experiments")
	private String runLapTable = "runlap";

	@Parameter(cmdline="experimentTable")
	private String experimentTable = "experiment";

	@Parameter(cmdline="annotationBaseDir")
	private String annotationBaseDir;

	@Parameter(cmdline="splitNumber")
	private int splitNumber;

	@Parameter(cmdline="probandNumber")
	private int probandNumber;

	@Parameter(cmdline="useDatabase", description="Specifies, whether iterations are being written to the database")
	private boolean useDatabase;


	// Generelle Hyperparameter

	@Parameter(cmdline="stepSize", description="Hyperparameter: constant stepSize for gradient based optimization methods")
	public float stepSize = 0.001f;

	@Parameter(cmdline="reg0" , description="Hyperparameter: regularization constant in front of L2 regularizer, measures how strong regularization should be.")
	public float reg0 = 0.01f;

	@Parameter(cmdline="stDev", description="Hyperparameter: Standard Deviation of the Gaussian where parameters are initialized")
	private float stDev = 0.1f;


	// Modellparameter:

	@Parameter(cmdline="fm_regV", description="Hyperparameter: Specifies the regularization of the V Vector of a Factorization Machine")
	private float fm_regV;

	@Parameter(cmdline="fm_regW", description="Hyperparameter: Specifies the regularization of the W Matrix of a Factorization Machine")
	private float fm_regW;

	@Parameter(cmdline="fm_numFactors", description="Hyperparameter: Specifies the size of the W Matrix of a Factorization Machine, i.e. the number of latent features")
	private int fm_numFactors;
	
	@Parameter(cmdline="useAcidFeatures")
	private boolean useAcidFeatures=true;
	
	@Parameter(cmdline="usePatientFeatures")
	private boolean usePatientFeatures=true;

	public String outputFolder;

	private int nrAttributes;


	protected Logger log = LogManager.getLogger(getClass());

//	private MhhDataset data = new MhhDataset();

	private Vector finalParameters;
	private float[] finalParametersArray;

	private MhhEval sample2Labels;
	private MhhRawData rawData;

	private Database database;

	private long runKey;

	private AllExtractor extractor;

	private LowerExtractor lowerFeatureExtractor;

	private MiddleExtractor middleFeatureExtractor;

	private UpperExtractor upperFeatureExtractor;

	private LowerMiddleExtractor lowerMiddleFeatureExtractor;

	private UpperMiddleExtractor upperMiddleFeatureExtractor;

	@Parameter(cmdline="serializeTheData")
	private boolean serializeTheData;

	public AlgorithmController() {
		extractor = new AllExtractor();
		lowerFeatureExtractor = new LowerExtractor();
		middleFeatureExtractor = new MiddleExtractor();
		upperFeatureExtractor = new UpperExtractor();
		lowerMiddleFeatureExtractor = new LowerMiddleExtractor();
		upperMiddleFeatureExtractor = new UpperMiddleExtractor();
	}


	@Override
	public void run() {
		
		System.out.println("Model Function is: " + modelFunction.getClass());

		log.info("Working on " + readSplit.getSplitFolder());
		readSplit.run();

		if (this.useDatabase) {
			try {
				// Initialize Database
				database = new Database();
				database.setRunTable(runTable); 
				database.setIterationTable(iterTable);
				database.init();
			} catch (IOException | DataStoreException e1) {
				log.fatal("Could not connect to database", e1);
				throw new BootstrapException("Could not connect to database", e1);						
			}
		}
		
		File[] trainList = readSplit.getTrainList();

		log.info("Working on " + trainList.length + " training files");
		log.info("Working on " + columnSelector.getUsedIndexes().length + " predictor variables");

		if (columnSelector.getUsedIndexes() == null) {
			log.warn("Column Selector not yet used!");
		}

		log.info("Raw Data Object will be built with " + readSplit.trainFolders.length + " Training Swallows, "
				+ "with " + readSplit.testFolders.length + " Test Swallows, and with " + readSplit.validationFolders.length 
				+" Validation Swallows!");

		rawData = new MhhRawData(readSplit.testFolders.length,
				readSplit.validationFolders.length, readSplit.trainFolders.length);

		sample2Labels = new MhhEval(readSplit.testFolders.length,
				readSplit.validationFolders.length, readSplit.trainFolders.length);

		
		PreprocessData pd = new PreprocessData();
		// copy properties (members)
		pd.setAnnotationBaseDir(annotationBaseDir);
		pd.setAnnotator(annotator);
		pd.setColumnSelector(columnSelector);
		pd.setIncludeRD(includeRD);
		pd.setReadSplit(readSplit);
		pd.setTimeOrder(timeOrder);
		pd.setWeightLength(weightLength);
		pd.setWindowExtent(windowExtent);
		pd.run();
		
		// TRAIN!

		List<SwallowDS> trainingSwallows = pd.getTrainingSwallows();
		for(int i = 0; i < trainingSwallows.size(); i ++) {
			SwallowDS d = trainingSwallows.get(i);
			
			int absoluteAnnotation = d.getAbsoluteIdxOfAnnotation();
			int relativeAnnotation = absoluteAnnotation - (int) d.data.get(0, 0);

			rawData.trainData[i]=d.data;
			rawData.trainDataLabels[i]=d.labels;
			rawData.instanceWeights[i]=d.instanceWeights;
			// TODO: populate
			rawData.trainDataAbsoluteAnnotations[i]=absoluteAnnotation;
//			rawData.trainDataRelativeAnnotations[i]=relativeAnnotation;

			Matrix[] sampletoLabels = createSample2Labels(d.data);

			sample2Labels.predictedTrainLabels[i] = sampletoLabels[0];
			sample2Labels.avgTrainLabels[i] = sampletoLabels[1];
		}

		// VALIDATION
		List<SwallowDS> validationSwallows = pd.getValidationSwallows();

		for(int i = 0; i < validationSwallows.size(); i ++) {
			SwallowDS d = validationSwallows.get(i);
			int absoluteAnnotation = d.getAbsoluteIdxOfAnnotation();
			int relativeAnnotation = absoluteAnnotation - (int) d.data.get(0, 0);

			rawData.validationData[i]=d.data;
			rawData.validationDataLabels[i]=d.labels;
			rawData.validationDataAbsoluteAnnotations[i] = absoluteAnnotation;
//			rawData.validationDataRelativeAnnotations[i] = relativeAnnotation;

			Matrix[] sampletoLabels = createSample2Labels(d.data);

			sample2Labels.predictedValidationLabels[i] = sampletoLabels[0];
			sample2Labels.avgValidationLabels[i] = sampletoLabels[1];
		}

		// TEST
		List<SwallowDS> testSwallows = pd.getTestSwallows();

		for(int i = 0; i < testSwallows.size(); i ++) {
			SwallowDS d = testSwallows.get(i);
			int absoluteAnnotation = d.getAbsoluteIdxOfAnnotation();
			int relativeAnnotation = absoluteAnnotation - (int) d.data.get(0, 0);

			rawData.testData[i]=d.data;
			rawData.testDataLabels[i]=d.labels;
			rawData.testDataAbsoluteAnnotations[i]=absoluteAnnotation;
//			rawData.testDataRelativeAnnotations[i]=relativeAnnotation;

			Matrix[] sampletoLabels = createSample2Labels(d.data);

			sample2Labels.predictedTestLabels[i] = sampletoLabels[0];
			sample2Labels.avgTestLabels[i] = sampletoLabels[1];

		}

		// Hinzufuegen des RuheDrucks zur Trainings Daten Matrix!

//		for (int i = 0; i < rawData.trainRuhedruck.length ; i++) {
//			if (i < readSplit.trainFolders.length) {
//				rawData.trainRuhedruck[i] = rawData.trainData[i];
//				rawData.trainRuhedruckLabels[i] = rawData.trainDataLabels[i];
//			}
//			else {
//				rawData.trainRuhedruck[i] = rawData.testRuhedruck[i - readSplit.trainFolders.length];
//				rawData.trainRuhedruckLabels[i] = rawData.testRuhedruckLabels[i - readSplit.trainFolders.length];
//			}
//		}


//		data.testData = new RowUnionMatrixView(rawData.testData);
//		data.trainData = new RowUnionMatrixView(rawData.trainData);
//		data.instanceWeights = new RowUnionMatrixView(rawData.instanceWeights);
//		//		data.ruheDruckTrainData = new RowUnionMatrixView(rawData.trainRuhedruck);
////		data.ruheDruckTrainDataLabels = new RowUnionMatrixView(rawData.trainRuhedruckLabels);
//		data.validationData = new RowUnionMatrixView(rawData.validationData);
//		data.testDataLabels = new RowUnionMatrixView(rawData.testDataLabels);
//		data.trainDataLabels = new RowUnionMatrixView(rawData.trainDataLabels);
//		data.validationDataLabels = new RowUnionMatrixView(rawData.validationDataLabels);

		log.info("Maximum number of predictors: " + (rawData.testData[0].getNumColumns()) + " where we have " + NUM_META_COLUMNS + " Meta Columns.");
		log.info("In total we have " + (rawData.testData[0].getNumColumns() - NUM_META_COLUMNS) + " predictors for learning!");

		this.nrAttributes = columnSelector.getUsedIndexes().length;

//		int trainInstances = rawData.trainData[*].getNumRows();

//		log.info("Working on " + trainInstances + " Training Instances.");


		if(this.useDatabase) {
			// Save current Run in Database
			try {
				runKey = database.addRun(stepSize, reg0, fm_regW, fm_regV, fm_numFactors,
						windowExtent, batchSize, readSplit.getSplitFolder().getAbsolutePath(), smoothReg, smoothWindow, timeOrder	);
			} catch (DataStoreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		// Initialize ModelFunction
		System.out.println(modelFunction.getClass());
		System.out.println("initializing model function from " + this.getIterTable());
		modelFunction.initialize(this);


		// Initialize LossFunction

		lossFunction.setLearnRate(stepSize);


		// Algorithm Objekt initialisieren
		Algorithm algorithm = new Algorithm();

		algorithm.setRawData(rawData);

		// Parameter an den Algorithmus uebergeben	


		// Unabhaengige Parameter
		algorithm.setMaxIterations(maxIterations);
		algorithm.setBatchSize(batchSize);
		algorithm.setWindowExtent(windowExtent);
		algorithm.setColumnSelector(columnSelector);

		algorithm.setUseDatabase(this.useDatabase);
		
		algorithm.setUseValidation(this.useValidation);

		algorithm.setLaplacian(this.laplacian);
		algorithm.setSmoothWindow(this.smoothWindow);
		algorithm.setSmoothReg(this.smoothReg);

		algorithm.setAnnotationBaseDir(annotationBaseDir);
		algorithm.setAnnotator(annotator);
		algorithm.setDatabase(database);
		algorithm.setRunKey(runKey);
		algorithm.setSplitNumber(splitNumber);
		algorithm.setProbandNumber(probandNumber);
		algorithm.setValidationFolders(readSplit.validationFolders);


		algorithm.setModelFunction(modelFunction);


		algorithm.setLossFunction(lossFunction);
		//		algorithm.setLaplacian(laplacian);	
		//		algorithm.setSmoothReg(smoothReg);
		//		algorithm.setSmoothWindow(smoothWindow);
		//		algorithm.setStepSize(stepSize);

		algorithm.run(); 

	}

	/** 
	 * Computes the column-wise maximum's index of a given Matrix, returning the lower index if multiple maxima are found.
	 * Beware: excludes the 0.th column
	 * 	
	 * @param in 
	 * @return
	 */
	public static int argmax(Matrix in) {
		int numRows = in.getNumRows();
		int numColumns = in.getNumColumns();

		int[] maxIndexes = new int[numColumns];
		float[] maxValues = new float[numColumns];

		for (int i = 1; i < numColumns ; i++) {
			for (int k = 0; k < numRows ; k++) {
				float value = in.get(k, i);
				if (value > maxValues[i]) {
					maxValues[i] = value;
					maxIndexes[i] = k;
				}
			}
		}

		float maxValue = -9999;
		int maxIndex = 0;
		
		// FIXME: why beginning at idx 1 ???
		for (int i = 1; i < numColumns ; i++) {
			if (maxValues[i] > maxValue) {
				maxValue = maxValues[i];
				maxIndex = maxIndexes[i];
			}
		}

		return maxIndex;
	}

	/** 
	 * Computes the row-wise maximum of a given matrix! Row wise means the maximum for each row is computed, a vector with length being equal to numrows is returned!
	 * @param in 
	 * @return
	 */
	public static float[] getTheMaxCurve(Matrix in) {
		int numRows = in.getNumRows();
		int numColumns = in.getNumColumns();

		float[] maxValues2 = new float[numRows];

		for (int row = 0; row < numRows ; row++) {
			maxValues2[row] = 0;
			// FIXME: Why does this loop start with 1 ???
			for (int col = 1 ; col < numColumns ; col++) {
				float value = in.get(row, col);
				if (value > maxValues2[row]) {
					maxValues2[row] = value;
				}
			}
		}

		return maxValues2;
	}

	/**
	 * Concatenates the Data of a given Read Folder Object, normalizes the data and includes meta data!
	 * @param folder
	 * @param restitutionszeitSample if <0, ignore the annotation and return a dummy (const) column
	 * @param normalize 
	 * @return Matrix of normalized pressure, fft and meta
	 * @throws ModelApplicationException 
	 */
	public  Matrix concatenate(Logger log, DataInterpretation folder, int restitutionszeitSample, boolean normalize)
			throws ModelApplicationException {

		return concatenate(log, folder, restitutionszeitSample, normalize, -1);

	}

	/**
	 * Concatenates the Data of a given Read Folder Object, normalizes the data and includes meta data!
	 * @param folder
	 * @param restitutionszeitSample if <0, ignore the annotation and return a dummy (const) column
	 * @param normalize 
	 * @param pmaxSample if < 0 wont be used but will be computed, otherwise it will be used!
	 * @return Matrix of normalized pressure, fft and meta
	 * @throws ModelApplicationException 
	 */
	public  Matrix concatenate(Logger log, DataInterpretation folder, 
			int restitutionszeitSample,
			boolean normalize, int pmaxSample) throws ModelApplicationException {

		int sampleIdxOfpMaxSample;

		Matrix druck = folder.getDruck();
		Matrix fft = folder.getFft();
		Matrix normalizedMaximumPressure = null;
		String channelstart = folder.getChannelstart();
		if (channelstart.startsWith("P"))
			channelstart=channelstart.substring(1);
		int start = Integer.parseInt(channelstart);
		String channelend = folder.getChannelend();
		if (channelend.startsWith("P"))
			channelend = channelend.substring(1);
		int end = Integer.parseInt(channelend);

		IntRange eSleeve = IntRange.convert(start + "," + end);

		Matrix sleeveDruck = new ColumnSubsetMatrixView(druck, eSleeve.getUsedIndexes());

		float[] maximumPressure = getTheMaxCurve(sleeveDruck);

		Vector maximumPressureVector = Vectors.floatArraytoVector(maximumPressure);

		if (pmaxSample < 1) {
			sampleIdxOfpMaxSample = (int) druck.get(argmax(sleeveDruck), 0);
		}
		else {
			sampleIdxOfpMaxSample = pmaxSample;
		}

		int firstSample = folder.getFirstSample();
		int lastSample = folder.getLastSample();

		if (sampleIdxOfpMaxSample < firstSample || sampleIdxOfpMaxSample > lastSample) {
			throw new ModelApplicationException("Der angegebene pmax liegt nicht im Schluck. Angegeben: " 
					+ pmaxSample + "\n Erstes Sample im schluck: " + firstSample + " , Letztes Sample im Schluck: " + lastSample 
					+ ", aufgetreten bei dem Schluck " + folder.getSchluckverzeichnis()
					, pmaxSample, lastSample, firstSample);
		}

		//Extract additional Features, Matrix sphincterFeatures is then attached to the final data Matrix
		Vector lowerFeatures = lowerFeatureExtractor.extractFeatures(druck, start, end);
		Vector middleFeatures = middleFeatureExtractor.extractFeatures(druck, start, end);
		Vector upperFeatures = upperFeatureExtractor.extractFeatures(druck, start, end);
		Vector lowerMiddleFeatures = lowerMiddleFeatureExtractor.extractFeatures(druck, start, end);
		Vector upperMiddleFeatures = upperMiddleFeatureExtractor.extractFeatures(druck, start, end);
		Vector allFeatures = extractor.extractFeatures(druck, start, end);

		Matrix sphincterFeatures = new DefaultMatrix(new ColumnUnionMatrixView(new Vector[] {
				lowerFeatures,
				middleFeatures,
				upperFeatures,
				lowerMiddleFeatures,
				upperMiddleFeatures,
				allFeatures }));

		Matrix normalizedDruck = null;
		Matrix normalizedFFT = null;
		Matrix normalizedSphincterFeatures = null;

		Vector sampleIdx = Matrices.col(druck, 0);

		if (normalize) {
			normalizedDruck = normalize(ColumnSubsetMatrixView.create(druck, new DefaultIntVector(new int[] {0})));
			normalizedFFT = normalize(ColumnSubsetMatrixView.create(fft, new DefaultIntVector(new int[] {0})));
			normalizedMaximumPressure = normalize(new VectorAsMatrixView(maximumPressureVector));
			normalizedSphincterFeatures = normalize(sphincterFeatures);
			log.info("The swallow: " + folder.getSwallowId() + " by Proband " + folder.getProband() + " is normalized");
		}

		int numRows = druck.getNumRows();

		log.info("Using " + normalizedDruck.getNumColumns() + " pressure features, " + normalizedMaximumPressure.getNumColumns()
				+ " maximum Pressure feature, \n "
				+ normalizedFFT.getNumColumns() + " fft features and " + normalizedSphincterFeatures.getNumColumns() + " sphincter features");

		//				log.info("For choosing the right feature subsets:");
		//				log.info("");
		//				log.info("Pressure features start at 5!");
		//				log.info("Pmax is at feature 32!");
		//				log.info("FFT starts at 33!");
		//				log.info("Sphincter Features start at 161! ");

		Matrix dataBeforeTimeExtraction;

		dataBeforeTimeExtraction = new ColumnUnionMatrixView(new Matrix[] {
				normalizedDruck
				, normalizedMaximumPressure
				, normalizedFFT
				, normalizedSphincterFeatures
		});

		

		TimeFeatureExtractor timeFeatureExtractor = new TimeFeatureExtractor();

		Matrix timeFeatures = timeFeatureExtractor.extractFeatures(dataBeforeTimeExtraction, getTimeOrder());

		log.info("Using approximates of the first derivatives of order: "+ getTimeOrder());
		log.info("Using " + timeFeatures.getNumColumns() + " additional temporal features!");
		
		
		AcidFeatureExtractor acidFeatureExtractor = new AcidFeatureExtractor();
		
		Matrix acidFeatures = acidFeatureExtractor.extractFeatures(folder);
		
		log.info("Using " + acidFeatures.getNumColumns() + " additional categorical features for acid");
		
		PatientFeatureExtractor patientFeatureExtractor = new PatientFeatureExtractor();
		
		Matrix patientFeatures = patientFeatureExtractor.extractFeatures(folder);
		
		log.info("Using " + patientFeatures.getNumColumns() + " additional categorical patient features");

		DefaultMatrix swallowId = new DefaultMatrix(numRows, 1);
		DefaultMatrix annotationSampleMatrix = new DefaultMatrix(numRows, 1);
		DefaultMatrix pMaxSampleMatrix = new DefaultMatrix(numRows, 1);

		// set swallow as meta 0 column idx
		Vectors.set(Matrices.col(swallowId, COL_SWALLOW_IDX), folder.getSwallowId());

		// the absolute sample id:
		Vector absSampleId = sampleIdx;

		// compute relative samples:
		DefaultVector relativeSampleId = new DefaultVector(absSampleId);
		Vectors.add(relativeSampleId, -1*absSampleId.get(0));

		// the static annotation sample
		if (restitutionszeitSample < 0) {
			Vectors.set(Matrices.col(annotationSampleMatrix, 0), 0); // defaults to 0
		} else {
			Vectors.set(Matrices.col(annotationSampleMatrix, 0), restitutionszeitSample);
		}
		// the static calculated pMax Sample
		Vectors.set(Matrices.col(pMaxSampleMatrix, 0), sampleIdxOfpMaxSample);

		//Concatenate Matrices without sample indexes for fft and druck
		ColumnUnionMatrixView ret1 = new ColumnUnionMatrixView(new Matrix[] {
				swallowId
				, new VectorAsMatrixView(absSampleId)
				, new VectorAsMatrixView(relativeSampleId)
				, annotationSampleMatrix
				, pMaxSampleMatrix
				, normalizedDruck
				, normalizedMaximumPressure
				, normalizedFFT
				, normalizedSphincterFeatures
				, timeFeatures
				, acidFeatures
				, patientFeatures
		}); 

		Matrix ret = new DefaultMatrix(ret1);

		if (isSerializeTheData()) {
			
			File dataInterpretation = folder.getDataInterpretation();
			File output = new File (dataInterpretation, "preprocessed_" + annotator + ".csv");
		
			try {
				Matrices.write(ret, output);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return ret;


	}

	/**
	 * Concatenates the Data of a given Read Folder Object, normalizes the data and includes meta data!
	 * @param folder
	 * @param restitutionszeitSample if <0, ignore the annotation and return a dummy (const) column
	 * @param normalize 
	 * @return Matrix of normalized pressure, fft and meta
	 */
	public static float[] concatenateForPmax(Logger log, DataInterpretation folder, int restitutionszeitSample,
			boolean normalize) {

		Matrix druck = folder.getDruck();
//		Matrix fft = folder.getFft();
		int start;
		int end;

		if (folder.getChannelstart().contains("P")) {
			start = Integer.parseInt(folder.getChannelstart().substring(1));
			end = Integer.parseInt(folder.getChannelend().substring(1));
		}
		else {
			start = Integer.parseInt(folder.getChannelstart());
			end = Integer.parseInt(folder.getChannelend());
		}
		IntRange eSleeve = IntRange.convert(start + "," + end);

		Matrix sleeveDruck = new ColumnSubsetMatrixView(druck, eSleeve.getUsedIndexes());



//		int idxMaxSampleC = (int) druck.get(argmax(sleeveDruck), 0);

		float[] ret = getTheMaxCurve(sleeveDruck);


//		Vector sampleIdx = Matrices.col(druck, 0);
//
//		if (normalize) {
//			druck = normalize(ColumnSubsetMatrixView.create(folder.getDruck(), new DefaultIntVector(new int[] {0})));
//			fft = normalize(ColumnSubsetMatrixView.create(folder.getFft(), new DefaultIntVector(new int[] {0})));
//			log.info("The swallow: " + folder.getSwallowId() + " by Proband " + folder.getProband() + " is normalized");
//		}
//
//		int numRows = druck.getNumRows();
//
//		//				int idxMaxSample = (int) annotations.get(folder.getIdAsInt()-1, Parser.ANNOTATION_COL_PMAX_SAMPLE);
//		//
//		//				System.out.println("Gelesenes Pmax Sample: " + idxMaxSample + "  Berechnetes: " + idxMaxSampleC);
//
//
//		//		VectorAsMatrixView;
//		DefaultMatrix meta = new DefaultMatrix(numRows, 1);
//		DefaultMatrix annotationSampleMatrix = new DefaultMatrix(numRows, 1);
//		DefaultMatrix pMaxSampleMatrix = new DefaultMatrix(numRows, 1);
//
//		// set swallow as meta 0 column idx
//		Vectors.set(Matrices.col(meta, COL_SWALLOW_IDX), folder.getSwallowId());
//
//		// the absolute sample id:
//		Vector absSampleId = sampleIdx;
//
//		// compute relative samples:
//		DefaultVector relativeSampleId = new DefaultVector(absSampleId);
//		Vectors.add(relativeSampleId, -1*absSampleId.get(0));
//
//		// the static annotation sample
//		if (restitutionszeitSample < 0)
//			Vectors.set(Matrices.col(annotationSampleMatrix, 0), 0); // defaults to 0
//		else {
//			Vectors.set(Matrices.col(annotationSampleMatrix, 0), restitutionszeitSample);
//		}
//		// the static calculated pMax Sample
//		Vectors.set(Matrices.col(pMaxSampleMatrix, 0), idxMaxSampleC);
//
//
//
//		//Concatenate Matrices without sample indexes for fft and druck
//		ColumnUnionMatrixView ret1 = new ColumnUnionMatrixView(new Matrix[] {
//				meta
//				, new VectorAsMatrixView(absSampleId)
//				, new VectorAsMatrixView(relativeSampleId)
//				, annotationSampleMatrix
//				, pMaxSampleMatrix
//				, druck
//				, fft
//		}); 
		return ret;


	}


	/**
	 * Static Method that normalizes a given Matrix and outputs the normalized Matrix, having values in [-1,1]
	 * @param input Matrix to be normalized
	 * @return normalized Matrix
	 */
	public static Matrix normalize(Matrix input) {
		Matrix ret = new DefaultMatrix(input);


		for (int c = 0; c < input.getNumColumns(); c++) {
			Vector col = Matrices.col(ret, c);
			Normalizer normalize = Vectors.normalize(col);
			normalize.normalizeInPlace(col);
		}

		return ret;

	}

	/**
	 * Creates two container Matrices by doing the following:
	 * 
	 * . Extract the following columns from the input (in order) :
	 * COL_SWALLOW_IDX, COL_ABS_SAMPLE_IDX, COL_REL_SAMPLE_IDX, COL_ANNOTATION_SAMPLE_IDX, COL_PMAX_SAMPLE_IDX
	 * . Create a return Matrix of size 2:
	 * .. create a new matrix (view) at return position 0 with all columns above PLUS a new column containing the predicted Labels
	 * .. create a new matrix (view) at return position 1 with all columns above PLUS a new column containing the averaged Labels
	 * . return the matrix array  

	 * Old doc: Creates sample2Labels with metaData, and null for unpredicted values
	 */
	public static Matrix[] createSample2Labels(Matrix input) {

		Matrix predictedLabels;
		Matrix avgLabels;

		Vector swallowIdx = Vectors.col(input, COL_SWALLOW_IDX);
		Vector absSamples = Vectors.col(input, COL_ABS_SAMPLE_IDX);
		Vector relSamples = Vectors.col(input, COL_REL_SAMPLE_IDX);
		Vector annotationSamples = Vectors.col(input, COL_ANNOTATION_SAMPLE_IDX);
		Vector pMaxSamples = Vectors.col(input, COL_PMAX_SAMPLE_IDX);
		//		Vector labels = new DefaultVector(pMaxSamples.size());
		Vector predictedlabels = new DefaultVector(swallowIdx);
		Vector avglabels = new DefaultVector(swallowIdx);

		predictedLabels = new ColumnUnionMatrixView(new Vector[] { swallowIdx, absSamples, relSamples,
				annotationSamples, pMaxSamples, predictedlabels });
		avgLabels = new ColumnUnionMatrixView(new Vector[] { swallowIdx, absSamples, relSamples,
				annotationSamples, pMaxSamples, avglabels } );

		return new Matrix[] {predictedLabels , avgLabels} ;
	}

	/**
	 * Computes average Labels according to windowExtent. Can then be used for deducing the annotation
	 * @param windowExtent, predictedLabels, avgLabels
	 */
	@SuppressWarnings("cast")
	public static void computeSample2avgLabel( int windowExtent, Matrix predictedLabels, Matrix avgLabels) {

		// TODO: Andre added

		predictedLabels = new DefaultMatrix(predictedLabels);
		int leftSide;
		int rightSide;
		int numPredictedLabelsRows = predictedLabels.getNumRows();
		
		for (int sample = 0; sample < numPredictedLabelsRows ; sample++)
		{
			if (predictedLabels.get(sample, COL_LABEL_IN_SAMPLE2LABEL) == 0) {
				avgLabels.set(sample, COL_LABEL_IN_SAMPLE2LABEL, 0);
				continue;
			}
			leftSide = sample - windowExtent;
			rightSide = sample + windowExtent;
			float avgSum = 0;
			if (leftSide >= 0 && rightSide < numPredictedLabelsRows)  // alles okay!!
			{
				float divisor = (2*windowExtent +1);
				for (int sampleIdx = -windowExtent ; sampleIdx <= windowExtent ; sampleIdx++)
				{
					float nextTerm = predictedLabels.get(sample + sampleIdx, COL_LABEL_IN_SAMPLE2LABEL);

					if (nextTerm == 0 ) {
						divisor = divisor  - 1;
					}
					avgSum += nextTerm;
				}
				if (divisor == 0) {
					// do nothing as avgSum would be divided by 1
				}
				else {
					avgSum = (avgSum/divisor);
				}
				avgLabels.set(sample, COL_LABEL_IN_SAMPLE2LABEL, avgSum);
			}
			else if (leftSide < 0 && rightSide < numPredictedLabelsRows)  // links zu kurz
			{
				int leftSide2 = windowExtent + leftSide;
				float divisor = (windowExtent+1+leftSide2);
				for (int sampleIdx = -leftSide2 ; sampleIdx <= windowExtent ; sampleIdx++)
				{
					float nextTerm = predictedLabels.get(sample + sampleIdx, COL_LABEL_IN_SAMPLE2LABEL);

					if (nextTerm == 0 ) {
						divisor = divisor  - 1;
					}
					avgSum +=nextTerm;
				}
				if (divisor == 0) {
					// do nothing as avgSum would be divided by 1
				}
				else {
					avgSum = (avgSum/divisor);
				}
				avgLabels.set(sample, COL_LABEL_IN_SAMPLE2LABEL, avgSum);
			}
			else if (leftSide >= 0 && rightSide >= numPredictedLabelsRows) // rechts zu kurz
			{
				int lastIdx = (numPredictedLabelsRows - 1);
				int rightSide2 = (lastIdx - sample);

				float divisor = (windowExtent+1+rightSide2);
				for (int sampleIdx = -windowExtent; sampleIdx <= rightSide2 ; sampleIdx++)
				{
					float nextTerm = predictedLabels.get(sample + sampleIdx, COL_LABEL_IN_SAMPLE2LABEL);

					if (nextTerm == 0 ) {
						divisor = divisor  - 1;
					}
					avgSum += nextTerm;
				}
				if (divisor == 0) {
					// do nothing as avgSum would be divided by 1
				}
				else {
					avgSum = (avgSum/divisor);
				}
				avgLabels.set(sample, COL_LABEL_IN_SAMPLE2LABEL, avgSum);
			}
			else if ( leftSide < 0 && rightSide >= numPredictedLabelsRows) // beidseitig zu kurz
			{
				//				System.out.println("WindowExtent is too high, will be halved...");
				windowExtent = (int) windowExtent/2;
			}
			else  // default alles falsch :-)
			{
				System.out.println("Something is wrong!!! Go Home Nico, you are drunk!!"); 
			}
		}



	}
	
	/**
	 * Computes average Labels according to windowExtent. Can then be used for deducing the annotation
	 * @param windowExtent, predictedLabels, avgLabels
	 */
	@SuppressWarnings("cast")
	public static void newComputeSample2avgLabel( int windowExtent, Matrix predictedLabels, Matrix avgLabels) {

		// TODO: Andre added

		predictedLabels = new DefaultMatrix(predictedLabels);
		int numPredictedLabelsRows = predictedLabels.getNumRows();
		
		
		float currentSampleSum = 0;
		
		int firstSample = 0;
		float firstLabel = predictedLabels.get(firstSample, COL_LABEL_IN_SAMPLE2LABEL);
		float lastLabel = predictedLabels.get(firstSample+windowExtent, COL_LABEL_IN_SAMPLE2LABEL);
		
		float divisor = windowExtent+1;
		
		for (int i = 0; i <= windowExtent; i++) {
			currentSampleSum += predictedLabels.get(firstSample+i, COL_LABEL_IN_SAMPLE2LABEL);
			avgLabels.set(firstSample, COL_LABEL_IN_SAMPLE2LABEL, currentSampleSum/divisor);
		}
		
		for (int sample = 1; sample < numPredictedLabelsRows ; sample++)
		{
			if (sample <= windowExtent) { // links zu kurz
				lastLabel = predictedLabels.get(sample+windowExtent, COL_LABEL_IN_SAMPLE2LABEL);
				currentSampleSum += lastLabel;
				divisor++;
				avgLabels.set(sample, COL_LABEL_IN_SAMPLE2LABEL, currentSampleSum/divisor);
			}
			else if (sample > numPredictedLabelsRows-1 -windowExtent) { // rechts zu kurz
				currentSampleSum -= firstLabel;
				firstLabel = predictedLabels.get(sample-windowExtent, COL_LABEL_IN_SAMPLE2LABEL);
				divisor--;
				avgLabels.set(sample, COL_LABEL_IN_SAMPLE2LABEL, currentSampleSum/divisor);
			}
			else {  // alles okay!!
				currentSampleSum -= firstLabel;
				lastLabel = predictedLabels.get(sample+windowExtent, COL_LABEL_IN_SAMPLE2LABEL);
				currentSampleSum += lastLabel;
				firstLabel = predictedLabels.get(sample-windowExtent, COL_LABEL_IN_SAMPLE2LABEL);
				avgLabels.set(sample, COL_LABEL_IN_SAMPLE2LABEL, currentSampleSum/divisor);
			}
			
		}

	}

	/**
	 * Computes for a given sample2Label Matrix the Annotation!
	 * @param labels
	 * @return
	 */
	public static int predictAnnotation(Matrix sample2labels, Logger log) {
		int annotation = 0;

		int pMax = (int) (sample2labels.get(0, COL_PMAX_SAMPLE_IDX) + 1);
		int startSearch;

		// seek to the relative index in the data where the pMax sample is stored in. (discards all data before the pMax sample) 
		int start = 0;
		try{
			while ( (int) (sample2labels.get(start, COL_ABS_SAMPLE_IDX ))!=pMax) {
				start++;
			}
		}
		catch(IndexOutOfBoundsException e) {

		}

		startSearch = start;

		int s2l_numRows = sample2labels.getNumRows();
		/* sample2labels.get(startSearch, COL_LABEL_IN_SAMPLE2LABEL) > 0 means: is swallow; seek for transition to negative (non-swallow)*/
		while ( startSearch < s2l_numRows && sample2labels.get(startSearch, COL_LABEL_IN_SAMPLE2LABEL) > 0) {
			startSearch++;
		}
		try {
			annotation = (int) sample2labels.get(startSearch, COL_REL_SAMPLE_IDX);
		}
		catch(IndexOutOfBoundsException e){
			log.warn("Annotation Sample could not be computed, will be set to end of swallow!");
			// FIXME: Potential bug found: shall the idx be one more ("after" the swallow, rather than 'on' the last index?
			annotation = (int) sample2labels.get(s2l_numRows-1, COL_REL_SAMPLE_IDX) + 1;
		}

		return annotation;
	}

	public static int predictAnnotation(Matrix sample2labels, Logger log, int pmaxSample) {
		int annotation = 0;
		//		TODO: Andre added this for testing
		//		sample2labels=new DefaultMatrix(sample2labels);

		int pMax = pmaxSample + 1;
		int startSearch;

		int start = 0;
		while ( (int) (sample2labels.get(start, COL_ABS_SAMPLE_IDX ))!=pMax) {
			start++;
		}
		startSearch = start;

		while ( startSearch < sample2labels.getNumRows() && sample2labels.get(startSearch, COL_LABEL_IN_SAMPLE2LABEL) > 0) {
			startSearch++;
		}
		try {
			annotation = (int) sample2labels.get(startSearch, COL_REL_SAMPLE_IDX);
		}
		catch(IndexOutOfBoundsException e){
			log.warn("Annotation Sample could not be computed, will be set to zero!");
		}

		return annotation;
	}


	// Getters and Setters
	public ReadSplit getReadSplit() {
		return readSplit;
	}

	public void setReadSplit(ReadSplit readSplit) {
		this.readSplit = readSplit;
	}

	public IntRange getColumnSelector() {
		return columnSelector;
	}

	public void setColumnSelector(IntRange columnSelector) {
		this.columnSelector = columnSelector;
	}

	public int getWindowExtent() {
		return windowExtent;
	}

	public void setWindowExtent(int windowExtent) {
		this.windowExtent = windowExtent;
	}

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

	public float getReg0() {
		return reg0;
	}

	public void setReg0(float lambda) {
		this.reg0 = lambda;
	}

	public boolean isUseValidation() {
		return useValidation;
	}

	public void setUseValidation(boolean useValidation) {
		this.useValidation = useValidation;
	}

	public Vector getFinalParameters() {
		return finalParameters;
	}

	public void setFinalParameters(Vector finalParameters) {
		this.finalParameters = finalParameters;
	}

	public LossFunction getLossFunction() {
		return lossFunction;
	}

	public void setLossFunction(LossFunction descentDirection) {
		this.lossFunction = descentDirection;
	}

	public ModelFunctions getModelFunction() {
		return modelFunction;
	}

	public void setModelFunction(ModelFunctions modelFunction) {
		this.modelFunction = modelFunction;
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

	public String getRunTable() {
		return runTable;
	}

	public void setRunTable(String runTable) {
		this.runTable = runTable;
	}

	public String getIterTable() {
		return iterTable;
	}

	public void setIterTable(String iterTable) {
		this.iterTable = iterTable;
	}

	public boolean isIncludeRD() {
		return includeRD;
	}

	public void setIncludeRD(boolean includeRD) {
		this.includeRD = includeRD;
	}



	public String getApplyFolder() {
		return applyFolder;
	}



	public void setApplyFolder(String applyFolder) {
		this.applyFolder = applyFolder;
	}



	public float[] getFinalParametersArray() {
		return finalParametersArray;
	}



	public void setFinalParametersArray(float[] finalParametersArray) {
		this.finalParametersArray = finalParametersArray;
	}



	public String getOutputFolder() {
		return outputFolder;
	}



	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}




	public String getRunLapTable() {
		return runLapTable;
	}



	public void setRunLapTable(String runLapTable) {
		this.runLapTable = runLapTable;
	}

	public float getWeightLength() {
		return weightLength;
	}

	public void setWeightLength(float weightLength) {
		this.weightLength = weightLength;
	}

	public boolean isSmallBatch() {
		return smallBatch;
	}

	public void setSmallBatch(boolean smallBatch) {
		this.smallBatch = smallBatch;
	}

	public int getSplitNumber() {
		return splitNumber;
	}

	public void setSplitNumber(int splitNumber) {
		this.splitNumber = splitNumber;
	}

	public int getProbandNumber() {
		return probandNumber;
	}

	public void setProbandNumber(int probandNumber) {
		this.probandNumber = probandNumber;
	}

	public float getStDev() {
		return stDev;
	}

	public void setStDev(float stDev) {
		this.stDev = stDev;
	}

	public float getFm_regV() {
		return fm_regV;
	}

	public void setFm_regV(float fm_regV) {
		this.fm_regV = fm_regV;
	}

	public float getFm_regW() {
		return fm_regW;
	}

	public void setFm_regW(float fm_regW) {
		this.fm_regW = fm_regW;
	}

	public int getFm_numFactors() {
		return fm_numFactors;
	}

	public void setFm_numFactors(int fm_numFactors) {
		this.fm_numFactors = fm_numFactors;
	}

	public int getNrAttributes() {
		return nrAttributes;
	}

	public void setNrAttributes(int nrAttributes) {
		this.nrAttributes = nrAttributes;
	}





	public boolean isUseDatabase() {
		return useDatabase;
	}





	public void setUseDatabase(boolean useDatabase) {
		this.useDatabase = useDatabase;
	}





	public boolean isExtractAcidFeatures() {
		return isUseAcidFeatures();
	}





	public void setExtractAcidFeatures(boolean extractAcidFeatures) {
		this.setUseAcidFeatures(extractAcidFeatures);
	}





	public boolean isExtractPatientFeatures() {
		return isUsePatientFeatures();
	}





	public void setExtractPatientFeatures(boolean extractPatientFeatures) {
		this.setUsePatientFeatures(extractPatientFeatures);
	}





	public int getTimeOrder() {
		return timeOrder;
	}





	public void setTimeOrder(int timeOrder) {
		this.timeOrder = timeOrder;
	}





	public boolean isUsePatientFeatures() {
		return usePatientFeatures;
	}





	public void setUsePatientFeatures(boolean usePatientFeatures) {
		this.usePatientFeatures = usePatientFeatures;
	}





	public boolean isUseAcidFeatures() {
		return useAcidFeatures;
	}





	public void setUseAcidFeatures(boolean useAcidFeatures) {
		this.useAcidFeatures = useAcidFeatures;
	}





	public String getExperimentTable() {
		return experimentTable;
	}





	public void setExperimentTable(String experimentTable) {
		this.experimentTable = experimentTable;
	}


	public boolean isSerializeTheData() {
		return serializeTheData;
	}


	public void setSerializeTheData(boolean serializeTheData) {
		this.serializeTheData = serializeTheData;
	}




}
