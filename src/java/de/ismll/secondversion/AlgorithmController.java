package de.ismll.secondversion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.bootstrap.BootstrapException;
import de.ismll.bootstrap.CommandLineParser;
import de.ismll.bootstrap.Parameter;
import de.ismll.database.dao.DataStoreException;
import de.ismll.evaluation.Accuracy;
import de.ismll.exceptions.ModelApplicationException;
import de.ismll.lossFunctions.LossFunction;
import de.ismll.mhh.featureExtractors.AllExtractor;
import de.ismll.mhh.featureExtractors.LowerExtractor;
import de.ismll.mhh.featureExtractors.LowerMiddleExtractor;
import de.ismll.mhh.featureExtractors.MiddleExtractor;
import de.ismll.mhh.featureExtractors.UpperExtractor;
import de.ismll.mhh.featureExtractors.UpperMiddleExtractor;
import de.ismll.mhh.io.Parser;
import de.ismll.mhh.io.DataInterpretation;
import de.ismll.mhh.io.SwallowData;
import de.ismll.modelFunctions.LinearRegressionPrediction;
import de.ismll.modelFunctions.ModelFunctions;
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

	@Parameter(cmdline="normalized", description="true, if normalized data should be used, false, otherwise.")
	private boolean normalize=true;

	@Parameter(cmdline="maxIterations", description="number of maximum iterations for train() Method")
	public int maxIterations = 1000;

	@Parameter(cmdline="stepSize", description="Hyperparameter: constant stepSize for gradient based optimization methods")
	public float stepSize = 0.001f;

	@Parameter(cmdline="batchSize", description="Hyperparameter: number of instances used to compute a gradient, i.e. 1 -> stochastic  trainInstances -> full")
	public int batchSize = 0;

	@Parameter(cmdline="laplacian", description="true, if laplacian regularization is wanted, false, otherwise.")
	public boolean laplacian;

	@Parameter(cmdline="smoothWindow" , description="Window, over which the classification is smoothed.")
	private int smoothWindow;

	@Parameter(cmdline="smoothReg" , description="Parameter, that specifies the influence of the Laplacian regularization")
	private float smoothReg;

	@Parameter(cmdline="lambda" , description="Hyperparameter: regularization constant in front of L2 regularizer, measures how strong regularization should be.")
	public float lambda = 0.01f;

	@Parameter(cmdline="descentDirection" , description="Abstract Class. Input a String that specifies the loss function, e.g. \"logisticLoss\".")
	public LossFunction descentDirection;

	@Parameter(cmdline="useValidation" , description="specifies, if validation is used (true) or test (false).")
	public boolean useValidation;

	@Parameter(cmdline="modelFunction", description="Specifies the model Function to be used, i.e. a linear Model, factorizationMachine etc.")
	public ModelFunctions modelFunction;

	@Parameter(cmdline="serializeData", description="Optional. If given, serializes the train/validation/test data into this folder")
	private File serializeData;

	@Parameter(cmdline="columnSelector", description="Optional. If given, selects a subset of columns. WARNING: If given, make sure to exclude, e.g., the sample Index (col 0)!")
	public IntRange columnSelector;

	@Parameter(cmdline="annotation", description="File that contains manual annotations by experts, also contains pMax etc.")
	private File annotation;

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

	public String outputFolder;


	protected Logger log = LogManager.getLogger(getClass());

	public MhhDataset data = new MhhDataset();

	private Matrix annotations;
	private boolean skipLeading=true;
	private boolean skipBetween=true;

	private Vector finalParameters;
	private float[] finalParametersArray;
	private Vector predictions;

	MhhEval sample2Labels;
	MhhRawData rawData;

	private Database database;

	private long runKey;







	// DOES NOT WORK AS PMAX IS COMPUTED INSTEAD OF READ FROM ANNOTATIONS
	public void buildDataforSVMStruct() throws ModelApplicationException {

		System.out.println(readSplit.splitFolder);

		int proband = 11;
		String type = "Inter";
		int nrSplits = 10;

		for (int split = 1; split <= nrSplits ; split++) {
			this.readSplit  = new ReadSplit(new File("/home/nico/acogpr/Splits/"+type+"Proband/Proband"+proband+"/split-" + split) );

			System.out.println(readSplit.splitFolder);

			log.info("Converting data for " + readSplit.splitFolder);
			readSplit.run();

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

			// TRAIN!

			for(int i = 0; i < readSplit.trainFolders.length; i ++) {
				log.info("Training data ... " + readSplit.trainFolders[i].getSwallowId());
				DataInterpretation folder = readSplit.trainFolders[i];

				int annotation = getAnnotation(folder);

				SwallowDS d = preprocessSwallow(folder, annotation, -1, skipLeading, skipBetween);
				rawData.trainData[i]=d.data;
				rawData.trainDataLabels[i]=d.labels;

				for (int row = 0; row < d.data.getNumRows() ; row++) {
					rawData.trainData[i].set(row, COL_SWALLOW_IDX, i);
				}

				Matrix[] sampletoLabels = createSample2Labels(d.data);

				sample2Labels.predictedTrainLabels[i] = sampletoLabels[0];
				sample2Labels.avgTrainLabels[i] = sampletoLabels[1];
			}

			// VALIDATION

			for(int i = 0; i < readSplit.validationFolders.length; i ++) {
				log.info("Validation data ... " + readSplit.validationFolders[i].getSwallowId());
				DataInterpretation folder = readSplit.validationFolders[i];

				int annotation = getAnnotation(folder);

				SwallowDS d = preprocessSwallow(folder, annotation, -1, skipLeading, skipBetween);
				rawData.validationData[i]=d.data;
				rawData.validationDataLabels[i]=d.labels;

				for (int row = 0; row < d.data.getNumRows() ; row++) {
					rawData.validationData[i].set(row, COL_SWALLOW_IDX, i);
				}

				Matrix[] sampletoLabels = createSample2Labels(d.data);

				sample2Labels.predictedValidationLabels[i] = sampletoLabels[0];
				sample2Labels.avgValidationLabels[i] = sampletoLabels[1];
			}

			// TEST

			for(int i = 0; i < readSplit.testFolders.length; i ++) {
				log.info("Test data ... " + readSplit.testFolders[i].getSwallowId());
				DataInterpretation folder = readSplit.testFolders[i];

				int annotation = getAnnotation(folder);

				SwallowDS d = preprocessTestSwallow(folder, annotation, -1 , skipLeading, skipBetween);
				rawData.testData[i]=d.data;
				rawData.testDataLabels[i]=d.labels;
				rawData.testRuhedruck[i]=d.ruheDruck;
				rawData.testRuhedruckLabels[i]=d.ruheDruckLabels;

				// Wird nicht gebraucht da immer nur ein TestSwallow!!! 

				//				for (int row = 0; row < d.data.getNumRows() ; row++) {
				//					rawData.testData[i].set(row, COL_SWALLOW_IDX, i);
				//				}

				Matrix[] sampletoLabels = createSample2Labels(d.data);

				sample2Labels.predictedTestLabels[i] = sampletoLabels[0];
				sample2Labels.avgTestLabels[i] = sampletoLabels[1];

			}

			data.testData = new RowUnionMatrixView(rawData.testData);
			data.trainData = new RowUnionMatrixView(rawData.trainData);
			data.validationData = new RowUnionMatrixView(rawData.validationData);
			data.testDataLabels = new RowUnionMatrixView(rawData.testDataLabels);
			data.trainDataLabels = new RowUnionMatrixView(rawData.trainDataLabels);
			data.validationDataLabels = new RowUnionMatrixView(rawData.validationDataLabels);

			Matrix trainFull;
			Matrix testFull;
			Matrix validationFull;

			Matrix testData = new DefaultMatrix(data.testData);
			Matrix testLabels = new DefaultMatrix(data.testDataLabels);

			Matrix trainData = new DefaultMatrix(data.trainData);
			Matrix trainLabels = new DefaultMatrix(data.trainDataLabels);

			Matrix validationData = new DefaultMatrix(data.validationData);
			Matrix validationLabels = new DefaultMatrix(data.validationDataLabels);

			for (int j = 0 ; j < testLabels.getNumRows() ; j++) {

				if (testLabels.get(j, COL_LABEL_IN_LABELS) == -1) {
					testLabels.set(j, COL_LABEL_IN_LABELS, 2);
				}
			}

			for (int j = 0 ; j < trainLabels.getNumRows() ; j++) {
				if (trainLabels.get(j, COL_LABEL_IN_LABELS) == -1) {
					trainLabels.set(j, COL_LABEL_IN_LABELS, 2);
				}
			}

			for (int j = 0 ; j < validationLabels.getNumRows() ; j++) {
				if (validationLabels.get(j, COL_LABEL_IN_LABELS) == -1) {
					validationLabels.set(j, COL_LABEL_IN_LABELS, 2);
				}
			}

			log.info("-1 Labels have been set to the value 2");

			this.columnSelector = new IntRange("33,166");



			testFull = new ColumnUnionMatrixView(
					new Matrix[] {
							new VectorAsMatrixView(Matrices.col(testLabels, COL_LABEL_IN_LABELS)),
							new VectorAsMatrixView(Matrices.col(testData, COL_SWALLOW_IDX)),
							preprocess(testData, columnSelector),
							new VectorAsMatrixView(Matrices.col(testData, COL_REL_SAMPLE_IDX))
					});

			trainFull = new ColumnUnionMatrixView(
					new Matrix[] {
							new VectorAsMatrixView(Matrices.col(trainLabels, COL_LABEL_IN_LABELS)),
							new VectorAsMatrixView(Matrices.col(trainData, COL_SWALLOW_IDX)),
							preprocess(trainData, columnSelector),
							new VectorAsMatrixView(Matrices.col(trainData, COL_REL_SAMPLE_IDX))
					});
			validationFull = new ColumnUnionMatrixView(
					new Matrix[] {
							new VectorAsMatrixView(Matrices.col(validationLabels, COL_LABEL_IN_LABELS)),
							new VectorAsMatrixView(Matrices.col(validationData, COL_SWALLOW_IDX)),
							preprocess(validationData, columnSelector),
							new VectorAsMatrixView(Matrices.col(validationData, COL_REL_SAMPLE_IDX))

					});


			// for test first

			try {
				FileOutputStream testStream = new FileOutputStream(new File(readSplit.splitFolder.getAbsolutePath()
						+ "/test-P"+proband+"-split"+split+".data"));

				for (int i = 0; i < testFull.getNumRows() ; i++ ) {
					String line = "";
					for (int k = 0; k < testFull.getNumColumns(); k++) {
						if (k == 0) {
							line = line + testFull.get(i, k) + " ";  //gets the label
						}
						else if (k == 1) {
							line = line + "qid:" + (int) (testFull.get(i, k)) + " "; //gets the swallowIDx
						}
						else if (k == testFull.getNumColumns() - 1) {
							line = line + "# " + testFull.get(i, k) + "\n"; //gets the rel Sample as comment 
						}
						else {
							line = line + (k-1) + ":" + testFull.get(i, k) + " "; //gets data
						}
					}
					testStream.write(line.getBytes());
				}
				testStream.flush();
				testStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 

			log.info("TestData has been successfully written!");

			try {
				FileOutputStream trainStream = new FileOutputStream((new File(readSplit.splitFolder.getAbsolutePath()
						+ "/train-P"+proband+"-split"+split+".data")));

				for (int i = 0; i < trainFull.getNumRows() ; i++ ) {
					String line = "";
					for (int k = 0; k < trainFull.getNumColumns(); k++) {
						if (k == 0) {
							line = line + trainFull.get(i, k) + " "; //gets the label
						}
						else if (k == 1) {
							line = line + "qid:" + (int) (trainFull.get(i, k)) + " "; //gets the swallowIDx
						}
						else if (k == trainFull.getNumColumns() - 1) {
							line = line + "# " + trainFull.get(i, k) + "\n"; //gets the rel Sample as comment 
						}
						else {
							line = line + (k-1) + ":" + trainFull.get(i, k) + " "; //gets data
						}
					}
					trainStream.write(line.getBytes());
				}
				trainStream.flush();
				trainStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			log.info("TrainData has been successfully written!");

			try {
				FileOutputStream validationStream = new FileOutputStream((new File(readSplit.splitFolder.getAbsolutePath()
						+ "/validation-P"+proband+"-split"+split+".data")));

				for (int i = 0; i < validationFull.getNumRows() ; i++ ) {
					String line = "";
					for (int k = 0; k < validationFull.getNumColumns(); k++) {
						if (k == 0) {
							line = line + validationFull.get(i, k) + " "; //gets the label
						}
						else if (k == 1) {
							line = line + "qid:" + (int) (validationFull.get(i, k)) + " "; //gets the swallowIDx
						} 
						else if (k == validationFull.getNumColumns() - 1) {
							line = line + "# " + validationFull.get(i, k) + "\n"; //gets the rel Sample as comment 
						}
						else {
							line = line + (k-1) + ":" + validationFull.get(i, k) + " "; //gets data
						}
					}
					validationStream.write(line.getBytes());
				}
				validationStream.flush();
				validationStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			log.info("ValidationData has been successfully written!");

			log.info("Data for Split " + split +" by Proband " + proband +" has been successfully written!" );


		}



		//		this.columnSelector = new IntRange()

		log.info("Data has been built successfully!!!!");
	}

	/**
	 * Method that reads parameters and data and then predicts annotation and computes Accuracy and the likes.
	 * @throws ModelApplicationException 
	 */
	public void apply() throws ModelApplicationException {

		this.modelFunction = new LinearRegressionPrediction();

		this.columnSelector = new IntRange("5,159");

		Matrix[] swallowData;
		Matrix[] swallowLabels;
		Matrix[] predictedLabels;
		Matrix[] avgLabels;

		DataInterpretation swallowFolder = new DataInterpretation();

		swallowFolder.setDataInterpretation(new File(applyFolder));

		swallowFolder.run();

		log.info("Apply Data is Swallow  " + swallowFolder.getSwallowId() + "  by Proband  " + swallowFolder.getProband() );

		int annotation = getAnnotation(swallowFolder);
		annotation = annotation - swallowFolder.getFirstSample();
		int[] trueAnnotations = new int[] {annotation};

		SwallowDS d = preprocessSwallow(swallowFolder, annotation, -1 , skipLeading, skipBetween);


		swallowData = new Matrix[]{ d.data };
		swallowLabels = new Matrix[] {d.labels};



		Matrix[] sampletoLabels = createSample2Labels(swallowData[0]);  // always 1 since we only predict for one swallow

		predictedLabels = new Matrix[] {sampletoLabels[0]};
		avgLabels = new Matrix[] {sampletoLabels[1]};

		Accuracy acc = new Accuracy();

		predictLabels(swallowData, predictedLabels);

		computeSample2avgLabel(windowExtent, predictedLabels, avgLabels);


		int[] swallowAnnotations = predictAnnotations(avgLabels);


		float accuracy = acc.evaluate(Vectors.col(avgLabels[0], COL_LABEL_IN_SAMPLE2LABEL), 
				Vectors.col(swallowLabels[0], COL_LABEL_IN_LABELS));

		predictions = Vectors.col(avgLabels[0], COL_LABEL_IN_SAMPLE2LABEL);

		String writePath;

		writePath = outputFolder + "/P" + swallowFolder.getProband() + "/Schluck" + swallowFolder.getSwallowId();

		File directory = new File(writePath);
		directory.mkdirs();

		try {
			IntVectors.write( DefaultIntVector.wrap(swallowAnnotations), new File(writePath+"/predictedAnnotation" ), false);
			IntVectors.write( DefaultIntVector.wrap(trueAnnotations), new File(writePath+"/trueAnnotation" ), false);
			//Schreibe Sample2AvgLabels
			Matrices.write(new ColumnSubsetMatrixView(avgLabels[0], new int[] { COL_REL_SAMPLE_IDX, COL_LABEL_IN_SAMPLE2LABEL}),
					new File(writePath +"/sample2avgLabels"), false);
			//Schreibe Sample2predictedLabels
			Matrices.write(new ColumnSubsetMatrixView(predictedLabels[0], new int[] { COL_REL_SAMPLE_IDX, COL_LABEL_IN_SAMPLE2LABEL}),
					new File(writePath+"/sample2predictedLabels"), false);


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Everything successfull!");

	}

	@Parameter(cmdline="splitNumber")
	private int splitNumber;

	@Parameter(cmdline="probandNumber")
	private int probandNumber;

	@Override
	public void run() {
		log.info("Working on " + readSplit.getSplitFolder());
		readSplit.run();

		// Stuff for saving in the end Ist aber mittlerweile stale, weil alles in DB geht...


//		String splitPath = readSplit.getSplitFolder().getAbsolutePath();
//		String[] splits = splitPath.split("split-");

		
//		int probandNumber = 0;

//		try {
//			String probandPath = readSplit.getSplitFolder().getParent();
//			String[] probandSplit = probandPath.split("Proband");
//			probandNumber = Integer.parseInt(probandSplit[1]);
//		}
//		catch (IndexOutOfBoundsException e) 
//		{
//			e.printStackTrace();
//		}




		try {
			// Initialize Database
			database = new Database();
			if (laplacian) { database.setRunLapTable(runLapTable);}
			else { database.setRunTable(runTable); }
			database.setIterationTable(iterTable);
			database.init();
		} catch (IOException | DataStoreException e1) {
			log.fatal("Could not connect to database", e1);
			throw new BootstrapException("Could not connect to database", e1);						
		}


		File[] trainList = readSplit.trainList;

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

		// TRAIN!

		for(int i = 0; i < readSplit.trainFolders.length; i ++) {
			log.info("Training data ... " + readSplit.trainFolders[i].getSwallowId());
			DataInterpretation folder = readSplit.trainFolders[i];

			int annotation = getAnnotation(folder);


			int pmax = getPmax(folder);

			SwallowDS d = null;
			try {
				d = preprocessSwallow(folder, annotation, pmax, skipLeading, skipBetween);
			} catch (ModelApplicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			rawData.trainData[i]=d.data;
			rawData.trainDataLabels[i]=d.labels;
			rawData.instanceWeights[i]=d.instanceWeights;

			Matrix[] sampletoLabels = createSample2Labels(d.data);

			sample2Labels.predictedTrainLabels[i] = sampletoLabels[0];
			sample2Labels.avgTrainLabels[i] = sampletoLabels[1];
		}

		// VALIDATION

		for(int i = 0; i < readSplit.validationFolders.length; i ++) {
			log.info("Validation data ... " + readSplit.validationFolders[i].getSwallowId());
			DataInterpretation folder = readSplit.validationFolders[i];

			int annotation = getAnnotation(folder);
			int pmax = getPmax(folder);

			SwallowDS d = null;
			try {
				d = preprocessSwallow(folder, annotation, pmax, skipLeading, skipBetween);
			} catch (ModelApplicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			rawData.validationData[i]=d.data;
			rawData.validationDataLabels[i]=d.labels;

			Matrix[] sampletoLabels = createSample2Labels(d.data);

			sample2Labels.predictedValidationLabels[i] = sampletoLabels[0];
			sample2Labels.avgValidationLabels[i] = sampletoLabels[1];
		}

		// TEST

		for(int i = 0; i < readSplit.testFolders.length; i ++) {
			log.info("Test data ... " + readSplit.testFolders[i].getSwallowId());
			DataInterpretation folder = readSplit.testFolders[i];

			int annotation = getAnnotation(folder);
			int pmax = getPmax(folder);

			SwallowDS d = null;
			try {
				d = preprocessTestSwallow(folder, annotation, pmax, skipLeading, skipBetween);
			} catch (ModelApplicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			rawData.testData[i]=d.data;
			rawData.testDataLabels[i]=d.labels;
			rawData.testRuhedruck[i]=d.ruheDruck;
			rawData.testRuhedruckLabels[i]=d.ruheDruckLabels;

			Matrix[] sampletoLabels = createSample2Labels(d.data);

			sample2Labels.predictedTestLabels[i] = sampletoLabels[0];
			sample2Labels.avgTestLabels[i] = sampletoLabels[1];

		}

		// Hinzufügen des RuheDrucks zur Trainings Daten Matrix!

		for (int i = 0; i < rawData.trainRuhedruck.length ; i++) {
			if (i < readSplit.trainFolders.length) {
				rawData.trainRuhedruck[i] = rawData.trainData[i];
				rawData.trainRuhedruckLabels[i] = rawData.trainDataLabels[i];
			}
			else {
				rawData.trainRuhedruck[i] = rawData.testRuhedruck[i - readSplit.trainFolders.length];
				rawData.trainRuhedruckLabels[i] = rawData.testRuhedruckLabels[i - readSplit.trainFolders.length];
			}
		}




		data.testData = new RowUnionMatrixView(rawData.testData);
		data.trainData = new RowUnionMatrixView(rawData.trainData);
		data.instanceWeights = new RowUnionMatrixView(rawData.instanceWeights);
//		data.ruheDruckTrainData = new RowUnionMatrixView(rawData.trainRuhedruck);
		data.ruheDruckTrainDataLabels = new RowUnionMatrixView(rawData.trainRuhedruckLabels);
		data.validationData = new RowUnionMatrixView(rawData.validationData);
		data.testDataLabels = new RowUnionMatrixView(rawData.testDataLabels);
		data.trainDataLabels = new RowUnionMatrixView(rawData.trainDataLabels);
		data.validationDataLabels = new RowUnionMatrixView(rawData.validationDataLabels);

		log.info("Maximum number of predictors: " + (data.testData.getNumColumns()) + " where we have " + NUM_META_COLUMNS + " Meta Columns.");
		log.info("In total we have " + (data.testData.getNumColumns() - NUM_META_COLUMNS) + " predictors for learning!");

		int trainInstances = data.trainData.getNumRows();

		log.info("Working on " + trainInstances + " Training Instances.");

		int smallBatchSize = (int) ( trainInstances*0.2);

		System.out.println("Small Batch is: " + smallBatchSize + "where Batch Size is: " + trainInstances);

		if (isSmallBatch() == true){ setBatchSize(smallBatchSize);}
		else { setBatchSize(trainInstances);}


		// Save current Run in Database
		if (laplacian) {
			try {
				runKey = database.addRunLaplacian(lambda, stepSize, windowExtent,
						batchSize, readSplit.getSplitFolder().getAbsolutePath(), smoothReg, smoothWindow);
			} catch (DataStoreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else {
			try {
				runKey = database.addRun(lambda, stepSize, windowExtent,
						batchSize, readSplit.getSplitFolder().getAbsolutePath());
			} catch (DataStoreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		// Algorithm Objekt initialisieren
		Algorithm algorithm = new Algorithm();

		// Parameter an den Algorithmus übergeben		
		setDatasets(algorithm);

		algorithm.setSplitNumber(splitNumber);
		algorithm.setProbandNumber(probandNumber);

		algorithm.setValidationFolders(readSplit.validationFolders);

		algorithm.setColumnSelector(columnSelector);

		algorithm.setMaxIterations(maxIterations);
		algorithm.setStepSize(stepSize);
		algorithm.setBatchSize(batchSize);
		algorithm.setLambda(lambda);
		algorithm.setLaplacian(laplacian);
		algorithm.setWindowExtent(windowExtent);
		algorithm.setSmoothReg(smoothReg);
		algorithm.setSmoothWindow(smoothWindow);
		algorithm.setModelFunction(modelFunction);
		algorithm.setLossFunction(descentDirection);
		algorithm.lossFunction.setLambda(lambda);
		algorithm.lossFunction.setLearnRate(stepSize);

		algorithm.setAnnotationBaseDir(annotationBaseDir);
		algorithm.setAnnotator(annotator);

		algorithm.setDatabase(database);
		algorithm.setRunKey(runKey);

		algorithm.run(); 

		finalParametersArray = algorithm.finalParametersArray;

		


	}

	public void predictLabels(Matrix[] data, Matrix[] sample2labels) {

		if (sample2labels.length!=data.length) {
			log.error("Number of swallows does not match!");
		}
		for (int i = 0; i < sample2labels.length ; i++) {
			float[] predict = modelFunction.predictAsClassification(modelFunction.evaluate(Matrices.asArray( 
					preprocess(data[i], columnSelector) )
					, finalParametersArray));
			Vector predictVector = Vectors.floatArraytoVector(predict);
			Vectors.copy(predictVector, Matrices.col(sample2labels[i],COL_LABEL_IN_SAMPLE2LABEL ));
			//			Matrices.setCol(sample2labels[i], COL_LABEL_IN_SAMPLE2LABEL, predictVector);
		}
	}



	public int getPmax(DataInterpretation folder) {
		int pmax = 0;

		int probandId = folder.getProband();
		int swallowId = folder.getSwallowId();

		String annotationPath = annotationBaseDir + probandId + "-" + annotator + ".tsv";
		

		// Parser modifizieren!

		try {
			annotations = Parser.readAnnotations(new File(annotationPath), folder.getSamplerateAsInt());
			pmax = (int) annotations.get(swallowId-1, Parser.ANNOTATION_COL_PMAX_SAMPLE);
			log.info("Pmax is given, will continue...");
		} catch (IOException e) {
			log.info("Pmax has not been provided for:  " + folder.getDataInterpretation().toString());
			log.info("The annotation path is: " + annotationPath);
			pmax = -1;
		} catch (ArrayIndexOutOfBoundsException t){
			log.info("There was an array out of bounds when reading annotations.... did you access the right file?");
			pmax = -1;
		}

		if(pmax == 0) { pmax = -1;}

		return pmax;
	}

	/**
	 * Returns the absolute Annotation for a given Swallow in a Read Folder Object. If there is no annotation, "NaN" will be returned
	 * @param folder
	 * @return
	 */
	public int getAnnotation(DataInterpretation folder) {
		int restitutionszeitSample;

		int probandId = folder.getProband();
		int swallowId = folder.getSwallowId();

		String annotationPath = annotationBaseDir + probandId + "-" + annotator + ".tsv";

		try {
			annotations = Parser.readAnnotations(new File(annotationPath), folder.getSamplerateAsInt());
			restitutionszeitSample = (int) annotations.get(swallowId-1, Parser.ANNOTATION_COL_RESTITUTIONSZEIT_SAMPLE);
			log.info("Annotation is given, will continue...");
		} catch (IOException e) {
			log.info("Annotations have not been provided for:  " + folder.getDataInterpretation().toString());
			log.info("The annotation path is: " + annotationPath);
			restitutionszeitSample = (int) Double.NaN;
		}
		catch (ArrayIndexOutOfBoundsException t){
			log.info("There was an array out of bounds when reading annotations.... did you access the right file?");
			restitutionszeitSample = (int) Double.NaN;
		}

		return restitutionszeitSample;

	}

	/**
	 * Performs a ColumnSubsetMatrixView according to the int[] Array of the given IntRange
	 * @param data
	 * @param columnSelector
	 * @return
	 */
	public static Matrix preprocess(Matrix data, IntRange columnSelector) {
		Matrix ret = new ColumnSubsetMatrixView(data, columnSelector.getUsedIndexes());
		return ret;
	}

	/**
	 * Sets the float Array Datasets for the given Algorithm Object
	 * @param alg
	 */
	public void setDatasets(Algorithm alg) {


		if (includeRD) {
			alg.setTrainInstanceWeights(Matrices.asArray(data.instanceWeights));
			alg.setTrainData( Matrices.asArray( preprocess(data.ruheDruckTrainData, columnSelector) ) );
			alg.setTrainDataLabels(Vectors.toFloatArray(Vectors.col(data.ruheDruckTrainDataLabels, COL_LABEL_IN_LABELS)));
		}
		else {
			alg.setTrainInstanceWeights(Matrices.asArray(data.instanceWeights));
			alg.setTrainData( Matrices.asArray( preprocess(data.trainData, columnSelector) ) );
			alg.setTrainDataLabels(Vectors.toFloatArray(Vectors.col(data.trainDataLabels, COL_LABEL_IN_LABELS)));
		}
	}


	/**
	 * Reads a (training) swallow; preprocessing the data, s.t.:
	 * 
	 * <ol>
	 * <li>Data is aggregated (e.g., concatenating FFT and Pressure samples, adding metadata, see {@link #concatenate(DataInterpretation)})
	 * <li>Data is narrowed (e.g., removing non-informative samples (between rdend and pmaxsample)
	 * <li>Labeling data (based on annotation) 
	 * </ol>
	 * @param colSelector 
	 * 
	 * 
	 * @return 
	 * @throws ModelApplicationException 
	 */
	public SwallowDS preprocessSwallow(DataInterpretation folder, int annotation, int pmax,  boolean skipLeading,
			boolean skipBetween)
					throws ModelApplicationException {
		SwallowDS ret = new SwallowDS();

		Matrix data = concatenate(log, folder, annotation, normalize, pmax);
		int numRows = data.getNumRows();


		int idxMaxSample2 = pmax;

		int rdEndSample = folder.getRdEndSample();
		int rdStartSample = folder.getRdStartSample();

		//get instance Weight Matrix, set all Weights to 1

		Matrix instanceWeights = new DefaultMatrix(numRows, 2);
		Vectors.set(Matrices.col(instanceWeights, 1), 1); // <-------- Macht das was ich will??

		ArrayList<Integer> throwAway = new ArrayList<>();

		DefaultMatrix labels = new DefaultMatrix(data.getNumRows(), /*SampleId, label*/2);
		// default clazz is -9999 (indicator for missing)
		Vectors.set(Matrices.col(labels, 1), -9999);

		//TODO: Add regression values as distance from annotation, depending on whether one wants classification or regression

		for (int j = 0; j< numRows; j++) {
			// copy over sample indizes to label matrix and to instanceWeight Matrix
			float absSampleIdx = data.get(j, COL_ABS_SAMPLE_IDX);
			labels.set(j, 0, (int) absSampleIdx); 
			instanceWeights.set(j, 0, (int) absSampleIdx);


			// now, determine label  TODO: Add regression values as distance from annotation, depending on whether one wants classification or regression
			float currentDataSampleId = absSampleIdx;


			// determine a weight for instance j

			if (Math.abs(currentDataSampleId - annotation) < weightLength) {
				float distance = Math.abs(currentDataSampleId - annotation);

				instanceWeights.set( j, 1,  1/(weightLength - distance));
			}


			if (currentDataSampleId <= rdStartSample) {
				if (skipLeading) {
					throwAway.add(Integer.valueOf(j));
				} else {
					labels.set(j, 1, LABEL_NICHT_SCHLUCK);
				}
				continue;

			}
			if (currentDataSampleId>rdStartSample && currentDataSampleId < rdEndSample) {
				labels.set(j, 1, LABEL_NICHT_SCHLUCK);
				continue;
			}
			if (currentDataSampleId>=rdEndSample && currentDataSampleId <= idxMaxSample2) {
				if (skipBetween) {
					throwAway.add(Integer.valueOf(j));
				} else {
					labels.set(j, 1, LABEL_NICHT_SCHLUCK);
				}
				continue;
			}
			if (currentDataSampleId>idxMaxSample2 && currentDataSampleId < annotation) {
				labels.set(j, 1, LABEL_SCHLUCK);
			}
			if (currentDataSampleId >= annotation) {
				labels.set(j, 1, LABEL_NICHT_SCHLUCK);
			}
		}


		//		for (int throwIndex : throwAway.)

		DefaultBitVector throwVector = new DefaultBitVector(numRows);
		Vectors.set(throwVector, true);
		for (Integer j : throwAway) {
			throwVector.set(j.intValue(), false);
		}


		RowSubsetMatrixView dataFiltered = new RowSubsetMatrixView(data, throwVector, true);
		RowSubsetMatrixView labelsFiltered = new RowSubsetMatrixView(labels, throwVector, true);
		RowSubsetMatrixView instanceWeightsFiltered = new RowSubsetMatrixView(instanceWeights, throwVector, true);

		ret.data=new DefaultMatrix(dataFiltered);
		ret.labels=new DefaultMatrix(labelsFiltered);
		ret.instanceWeights = new DefaultMatrix(instanceWeightsFiltered);
		ret.throwAway = throwAway;

		return ret;
	}

	/**
	 * Reads a (test) swallow; preprocessing the data, s.t.:
	 * 
	 * <ol>
	 * <li>Data is aggregated (e.g., concatenating FFT and Pressure samples, adding metadata, see {@link #concatenate(DataInterpretation)})
	 * <li>Data is narrowed (e.g., removing non-informative samples (between rdend and pmaxsample)
	 * <li>RuheDruck is ectracted
	 * <li>Labeling data (based on annotation) 
	 * </ol>
	 * @param colSelector 
	 * 
	 * 
	 * @return 
	 * @throws ModelApplicationException 
	 */
	public SwallowDS preprocessTestSwallow(DataInterpretation folder, int annotation, int pmax, boolean skipLeading, boolean skipBetween) throws ModelApplicationException {
		SwallowDS ret = new SwallowDS();

		Matrix data = concatenate(log, folder, annotation, normalize, pmax);
		int numRows = data.getNumRows();


		int idxMaxSample2 = pmax;

		int rdEndSample = folder.getRdEndSample();
		int rdStartSample = folder.getRdStartSample();


		ArrayList<Integer> throwAway = new ArrayList<>();
		ArrayList<Integer> ruheDruckSamples = new ArrayList<>();

		DefaultMatrix labels = new DefaultMatrix(data.getNumRows(), /*SampleId, label*/2);
		// default clazz is -9999 (indicator for missing)
		Vectors.set(Matrices.col(labels, 1), -9999);

		//TODO: Add regression values as distance from annotation, depending on whether one wants classification or regression

		if (Double.isNaN(annotation)) {
			for (int j = 0; j< numRows; j++) {
				// copy over sample indizes to label matrix 
				labels.set(j, 0, (int) data.get(j, COL_ABS_SAMPLE_IDX)); 

				// now, determine label  
				float currentDataSampleId = data.get(j, COL_ABS_SAMPLE_IDX);

				if (currentDataSampleId <= rdStartSample) {
					if (skipLeading) {
						throwAway.add(Integer.valueOf(j));
					} else {
						labels.set(j, 1, -9999);
					}
					continue;

				}
				if (currentDataSampleId>rdStartSample && currentDataSampleId < rdEndSample) {
					ruheDruckSamples.add(Integer.valueOf(j));
					labels.set(j, 1, -9999);
					continue;
				}
				if (currentDataSampleId>=rdEndSample && currentDataSampleId <= idxMaxSample2) {
					if (skipBetween) {
						throwAway.add(Integer.valueOf(j));
					} else {
						labels.set(j, 1, -9999);
					}
					continue;
				}
			}
		}
		else {
			for (int j = 0; j< numRows; j++) {
				// copy over sample indizes to label matrix 
				labels.set(j, 0, (int) data.get(j, COL_ABS_SAMPLE_IDX)); 

				// now, determine label  TODO: Add regression values as distance from annotation, depending on whether one wants classification or regression
				float currentDataSampleId = data.get(j, COL_ABS_SAMPLE_IDX);

				if (currentDataSampleId <= rdStartSample) {
					if (skipLeading) {
						throwAway.add(Integer.valueOf(j));
					} else {
						labels.set(j, 1, LABEL_NICHT_SCHLUCK);
					}
					continue;

				}
				if (currentDataSampleId>rdStartSample && currentDataSampleId < rdEndSample) {
					ruheDruckSamples.add(Integer.valueOf(j));
					labels.set(j, 1, LABEL_NICHT_SCHLUCK);
					continue;
				}
				if (currentDataSampleId>=rdEndSample && currentDataSampleId <= idxMaxSample2) {
					if (skipBetween) {
						throwAway.add(Integer.valueOf(j));
					} else {
						labels.set(j, 1, LABEL_NICHT_SCHLUCK);
					}
					continue;
				}
				if (currentDataSampleId>idxMaxSample2 && currentDataSampleId < annotation) {
					labels.set(j, 1, LABEL_SCHLUCK);
				}
				if (currentDataSampleId >= annotation) {
					labels.set(j, 1, LABEL_NICHT_SCHLUCK);
				}
			}

		}

		DefaultBitVector ruheDruckVector = new DefaultBitVector(numRows);
		Vectors.set(ruheDruckVector, false);
		for (Integer j : ruheDruckSamples) {
			ruheDruckVector.set(j.intValue(), true);
		}


		DefaultBitVector throwVector = new DefaultBitVector(numRows);
		Vectors.set(throwVector, true);
		for (Integer j : throwAway) {
			throwVector.set(j.intValue(), false);
		}

		RowSubsetMatrixView ruheDruck = new RowSubsetMatrixView(data, ruheDruckVector, true);
		RowSubsetMatrixView ruheDruckLabels = new RowSubsetMatrixView(labels, ruheDruckVector, true);

		// Hier alle labels des Ruhedrucks auf -1 setzen, da kein Schluck!!!! In neue Matrix giessen weil es sonst nicht geht!!

		Matrix ruheDruckLabels2 = new DefaultMatrix(ruheDruckLabels);

		for (int row = 0; row < ruheDruckLabels.getNumRows() ; row++) {
			ruheDruckLabels2.set(row, COL_LABEL_IN_LABELS, LABEL_NICHT_SCHLUCK);
		}

		RowSubsetMatrixView dataFiltered = new RowSubsetMatrixView(data, throwVector, true);
		RowSubsetMatrixView labelsFiltered = new RowSubsetMatrixView(labels, throwVector, true);

		//		System.out.println(ruheDruck.getNumRows() + " ist gleich " +  ruheDruckLabels2.getNumRows());

		ret.data=dataFiltered;
		ret.labels=labelsFiltered;
		ret.ruheDruck = ruheDruck;
		ret.throwAway = throwAway;
		ret.ruheDruckLabels = ruheDruckLabels2;

		return ret;
	}

	/** 
	 * Computes the column-wise maximum's index of a given Matrix	
	 * @param in 
	 * @return
	 */
	public static int getMax(Matrix in) {
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
	public static Matrix concatenate(Logger log, DataInterpretation folder, int restitutionszeitSample, boolean normalize)
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
	public static Matrix concatenate(Logger log, DataInterpretation folder, int restitutionszeitSample,
			boolean normalize, int pmaxSample) throws ModelApplicationException {

		int idxMaxSampleC;

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
			idxMaxSampleC = (int) druck.get(getMax(sleeveDruck), 0);
		}
		else {
			idxMaxSampleC = pmaxSample;
		}



		int firstSample = folder.getFirstSample();
		int lastSample = folder.getLastSample();

		if (idxMaxSampleC < firstSample || idxMaxSampleC > lastSample) {
			throw new ModelApplicationException("Der angegebene pmax liegt nicht im Schluck. Angegeben: " 
					+ pmaxSample + "\n Erstes Sample im schluck: " + firstSample + " , Letztes Sample im Schluck: " + lastSample 
					+ ", aufgetreten bei dem Schluck " + folder.getSchluckverzeichnis()
					, pmaxSample, lastSample, firstSample);
		}

		//Extract additional Features, Matrix sphincterFeatures is then attached to the final data Matrix

		AllExtractor extractor = new AllExtractor();
		LowerExtractor lower = new LowerExtractor();
		MiddleExtractor middle = new MiddleExtractor();
		UpperExtractor upper = new UpperExtractor();
		LowerMiddleExtractor lowerMiddle = new LowerMiddleExtractor();
		UpperMiddleExtractor upperMiddle = new UpperMiddleExtractor();

		Vector lowerFeatures = lower.extractFeatures(druck, start, end);
		Vector middleFeatures = middle.extractFeatures(druck, start, end);
		Vector upperFeatures = upper.extractFeatures(druck, start, end);
		Vector lowerMiddleFeatures = lowerMiddle.extractFeatures(druck, start, end);
		Vector upperMiddleFeatures = upperMiddle.extractFeatures(druck, start, end);
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
			normalizedDruck = normalize(ColumnSubsetMatrixView.create(folder.getDruck(), new DefaultIntVector(new int[] {0})));
			normalizedFFT = normalize(ColumnSubsetMatrixView.create(folder.getFft(), new DefaultIntVector(new int[] {0})));
			normalizedMaximumPressure = normalize(new VectorAsMatrixView(maximumPressureVector));
			normalizedSphincterFeatures = normalize(sphincterFeatures);
			log.info("The swallow: " + folder.getSwallowId() + " by Proband " + folder.getProband() + " is normalized");
		}

		int numRows = druck.getNumRows();

		//				int idxMaxSample = (int) annotations.get(folder.getIdAsInt()-1, Parser.ANNOTATION_COL_PMAX_SAMPLE);
		//
		//				System.out.println("Gelesenes Pmax Sample: " + idxMaxSample + "  Berechnetes: " + idxMaxSampleC);

		log.info("Using " + normalizedDruck.getNumColumns() + " pressure features, " + normalizedMaximumPressure.getNumColumns()
				+ " maximum Pressure feature, \n "
				+ normalizedFFT.getNumColumns() + " fft features and " + normalizedSphincterFeatures.getNumColumns() + " sphincter features");


		//				log.info("For choosing the right feature subsets:");
		//				log.info("");
		//				log.info("Pressure features start at 5!");
		//				log.info("Pmax is at feature 33!");
		//				log.info("FFT starts at 34!");
		//				log.info("Sphincter Features start at 162! ");

		//		VectorAsMatrixView;
		DefaultMatrix meta = new DefaultMatrix(numRows, 1);
		DefaultMatrix annotationSampleMatrix = new DefaultMatrix(numRows, 1);
		DefaultMatrix pMaxSampleMatrix = new DefaultMatrix(numRows, 1);

		// set swallow as meta 0 column idx
		Vectors.set(Matrices.col(meta, COL_SWALLOW_IDX), folder.getSwallowId());

		// the absolute sample id:
		Vector absSampleId = sampleIdx;

		// compute relative samples:
		DefaultVector relativeSampleId = new DefaultVector(absSampleId);
		Vectors.add(relativeSampleId, -1*absSampleId.get(0));

		// the static annotation sample
		if (restitutionszeitSample < 0)
			Vectors.set(Matrices.col(annotationSampleMatrix, 0), 0); // defaults to 0
		else {
			Vectors.set(Matrices.col(annotationSampleMatrix, 0), restitutionszeitSample);
		}
		// the static calculated pMax Sample
		Vectors.set(Matrices.col(pMaxSampleMatrix, 0), idxMaxSampleC);

		//Concatenate Matrices without sample indexes for fft and druck
		ColumnUnionMatrixView ret1 = new ColumnUnionMatrixView(new Matrix[] {
				meta
				, new VectorAsMatrixView(absSampleId)
				, new VectorAsMatrixView(relativeSampleId)
				, annotationSampleMatrix
				, pMaxSampleMatrix
				, normalizedDruck
				, normalizedMaximumPressure
				, normalizedFFT
				, normalizedSphincterFeatures
		}); 

		Matrix ret = new DefaultMatrix(ret1);

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
		Matrix fft = folder.getFft();
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



		int idxMaxSampleC = (int) druck.get(getMax(sleeveDruck), 0);

		float[] ret = getTheMaxCurve(sleeveDruck);


		Vector sampleIdx = Matrices.col(druck, 0);

		if (normalize) {
			druck = normalize(ColumnSubsetMatrixView.create(folder.getDruck(), new DefaultIntVector(new int[] {0})));
			fft = normalize(ColumnSubsetMatrixView.create(folder.getFft(), new DefaultIntVector(new int[] {0})));
			log.info("The swallow: " + folder.getSwallowId() + " by Proband " + folder.getProband() + " is normalized");
		}

		int numRows = druck.getNumRows();

		//				int idxMaxSample = (int) annotations.get(folder.getIdAsInt()-1, Parser.ANNOTATION_COL_PMAX_SAMPLE);
		//
		//				System.out.println("Gelesenes Pmax Sample: " + idxMaxSample + "  Berechnetes: " + idxMaxSampleC);


		//		VectorAsMatrixView;
		DefaultMatrix meta = new DefaultMatrix(numRows, 1);
		DefaultMatrix annotationSampleMatrix = new DefaultMatrix(numRows, 1);
		DefaultMatrix pMaxSampleMatrix = new DefaultMatrix(numRows, 1);

		// set swallow as meta 0 column idx
		Vectors.set(Matrices.col(meta, COL_SWALLOW_IDX), folder.getSwallowId());

		// the absolute sample id:
		Vector absSampleId = sampleIdx;

		// compute relative samples:
		DefaultVector relativeSampleId = new DefaultVector(absSampleId);
		Vectors.add(relativeSampleId, -1*absSampleId.get(0));

		// the static annotation sample
		if (restitutionszeitSample < 0)
			Vectors.set(Matrices.col(annotationSampleMatrix, 0), 0); // defaults to 0
		else {
			Vectors.set(Matrices.col(annotationSampleMatrix, 0), restitutionszeitSample);
		}
		// the static calculated pMax Sample
		Vectors.set(Matrices.col(pMaxSampleMatrix, 0), idxMaxSampleC);



		//Concatenate Matrices without sample indexes for fft and druck
		ColumnUnionMatrixView ret1 = new ColumnUnionMatrixView(new Matrix[] {
				meta
				, new VectorAsMatrixView(absSampleId)
				, new VectorAsMatrixView(relativeSampleId)
				, annotationSampleMatrix
				, pMaxSampleMatrix
				, druck
				, fft
		}); 
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


		//		float[] estExpectationValues = new float[input.getNumColumns()];
		//		float[] estStandardDeviations = new float[input.getNumColumns()];
		//		
		//		//Compute the estimated Expectation Values
		//		for (int column = 0; column < input.getNumColumns() ; column++) {
		//			float colSum = 0;
		//			for (int row = 0; row < input.getNumRows() ; row++) {
		//				colSum += input.get(row, column);
		//			}
		//			estExpectationValues[column] = colSum/(input.getNumRows());
		//		}
		//		//Compute the estimated Standard Deviations
		//		for (int column = 0; column < input.getNumColumns() ; column++) {
		//			float colVariance = 0;
		//			for (int row = 0; row < input.getNumRows() ; row++) {
		//				colVariance += (input.get(row, column) - estExpectationValues[column])*(input.get(row, column) - estExpectationValues[column]);
		//			}
		//			estStandardDeviations[column] = (float) Math.sqrt(colVariance/(input.getNumRows()-1));   
		//		}
		//		//Fill the scaled Matrix
		//		for (int row = 0; row < ret.getNumRows() ; row++) {
		//			for (int column = 0; column < ret.getNumColumns() ; column++) {
		//				if (estStandardDeviations[column] != 0) {
		//					float scaledValue = (input.get(row, column) - estExpectationValues[column])/(estStandardDeviations[column]);
		//					ret.set(row, column, scaledValue );
		//				}
		//			}
		//		}
		//		return ret;
	}

	//	public static Matrix[] createSample2Labels(SwallowDS input) {
	//
	//
	//		Matrix predictedLabels;
	//		Matrix avgLabels;
	//
	//		Vector swallowIdx = Vectors.col(input, COL_SWALLOW_IDX);
	//		Vector absSamples = Vectors.col(input, COL_ABS_SAMPLE_IDX);
	//		Vector relSamples = Vectors.col(input, COL_REL_SAMPLE_IDX);
	//		Vector annotationSamples = Vectors.col(input, COL_ANNOTATION_SAMPLE_IDX);
	//		Vector pMaxSamples = Vectors.col(input, COL_PMAX_SAMPLE_IDX);
	//		//		Vector labels = new DefaultVector(pMaxSamples.size());
	//		Vector predictedlabels = new DefaultVector(swallowIdx);
	//		Vector avglabels = new DefaultVector(swallowIdx);
	//
	//		predictedLabels = new ColumnUnionMatrixView(new Vector[] { swallowIdx, absSamples, relSamples,
	//				annotationSamples, pMaxSamples, predictedlabels });
	//		avgLabels = new ColumnUnionMatrixView(new Vector[] { swallowIdx, absSamples, relSamples,
	//				annotationSamples, pMaxSamples, avglabels } );
	//
	//		return new Matrix[] {predictedLabels , avgLabels} ;
	//	}


	/**
	 * Creates sample2Labels with metaData, and null for unpredicted values
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



	public void computeSample2avgLabel(int windowExtent, Matrix[] predictedLabels, Matrix[] avgLabels) {
		if (predictedLabels.length!=avgLabels.length) {
			log.error("Dimension mismatch at computing sample2avglabels!");
		}
		for (int i = 0; i < predictedLabels.length ; i++) {
			computeSample2avgLabel(windowExtent, predictedLabels[i], avgLabels[i]);
		}

	}

	private int[] predictAnnotations(Matrix[] sample2labels) {
		int[] ret = new int[sample2labels.length];
		for (int i = 0; i < sample2labels.length ; i++) {
			int annotation = predictAnnotation(sample2labels[i],log);
			ret[i] = annotation;
		}
		return ret;
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
				System.out.println("WindowExtent is too high, will be halved...");
				windowExtent = (int) windowExtent/2;
			}
			else  // default alles falsch :-)
			{
				System.out.println("Something is wrong!!! Go Home Nico, you are drunk!!"); 
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

		int start = 0;
		try{
			while ( (int) (sample2labels.get(start, COL_ABS_SAMPLE_IDX ))!=pMax) {
				start++;
			}
		}
		catch(IndexOutOfBoundsException e) {

		}

		startSearch = start;

		while ( startSearch < sample2labels.getNumRows() && sample2labels.get(startSearch, COL_LABEL_IN_SAMPLE2LABEL) > 0) {
			startSearch++;
		}
		try {
			annotation = (int) sample2labels.get(startSearch, COL_REL_SAMPLE_IDX);
		}
		catch(IndexOutOfBoundsException e){
			log.warn("Annotation Sample could not be computed, will be set to end of swallow!");
			annotation = (int) sample2labels.get(sample2labels.getNumRows()-1, COL_REL_SAMPLE_IDX);
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

	public File getAnnotation() {
		return annotation;
	}

	public void setAnnotation(File annotation) {
		this.annotation = annotation;
	}

	public File getSerializeData() {
		return serializeData;
	}

	public void setSerializeData(File serializeData) {
		this.serializeData = serializeData;
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

	public float getLambda() {
		return lambda;
	}

	public void setLambda(float lambda) {
		this.lambda = lambda;
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

	public LossFunction getDescentDirection() {
		return descentDirection;
	}

	public void setDescentDirection(LossFunction descentDirection) {
		this.descentDirection = descentDirection;
	}

	public ModelFunctions getModelFunction() {
		return modelFunction;
	}

	public void setModelFunction(ModelFunctions modelFunction) {
		this.modelFunction = modelFunction;
	}

	public long getRunKey() {
		return runKey;
	}

	private void setRunKey(long runKey) {
		this.runKey = runKey;
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



	public boolean isNormalize() {
		return normalize;
	}



	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
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




}
