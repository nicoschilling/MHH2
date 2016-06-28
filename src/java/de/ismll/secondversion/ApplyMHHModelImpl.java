package de.ismll.secondversion;

import java.io.File;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;

import de.ismll.bootstrap.CommandLineParser;
import de.ismll.evaluation.Accuracy;
import de.ismll.exceptions.ModelApplicationException;
import de.ismll.mhh.io.Parser;
import de.ismll.mhh.io.DataInterpretation;
import de.ismll.modelFunctions.LinearRegressionPrediction;
import de.ismll.modelFunctions.ModelFunctions;
import de.ismll.secondversion.SwallowDS;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultBitVector;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.impl.DefaultVector;
import de.ismll.table.projections.ColumnSubsetMatrixView;
import de.ismll.table.projections.RowSubsetMatrixView;
import static de.ismll.secondversion.DatasetFormat.*;

public class ApplyMHHModelImpl implements ApplyMHHModel {


	private static final String FILENAME_PARAMETERS = "parameters";

	private static final String FILENAME_WINDOW_EXTENT = "window_extent";

	private File[] modelFiles;

	private ModelFunctions modelFunction = new LinearRegressionPrediction();

	private int windowExtent=75;

	private Vector[] parameters;

	private int[] windowExtents;


	private IntRange columnSelector;

	private Matrix averagePredictions;


	private boolean skipLeading = true;
	private boolean skipBetween = true;

	private Logger log = LogManager.getLogger(getClass());

	
	// HARD GECODET.... IIIH!
	
	private String annotationBaseDir = "/acogpr/mhh/manual_annotations/NormalAnnotations/";
//	private String annotationBaseDir = "/acogpr/mhh/manual_annotations/NormalAnnotations/";
	//	private String annotationBaseDir = "/home/nico/acogpr/manual_annotations/AcidAnnotations/";

	private String annotator="sm";

	private Matrix[] validationData;
	private Matrix[] validationLabels;
	private float[] validationAnnotations;
	
	public AlgorithmController algcon;



	public void setValidation(DataInterpretation[] folders) throws ModelApplicationException {
		// SETS VALIDATION DATA ONLY ONCE

		this.validationData = new Matrix[folders.length];
		this.validationLabels = new Matrix[folders.length];
		this.validationAnnotations = new float[folders.length];

		for (int i = 0; i < folders.length ; i++) {
			int pmax = getPmax(folders[i]);
			SwallowDS val = preprocessValidationSwallow(folders[i], skipLeading, skipBetween, pmax);
			int annotation = getAnnotation(folders[i])-folders[i].getFirstSample();
			validationData[i] = val.data;
			validationLabels[i] = val.labels;
			validationAnnotations[i] = annotation;

		}
	}

	public Quality predictForValidation(Vector parameters) {
		Quality ret = new Quality();

		double[] accuracies = new double[validationData.length];
		double[] sampleDifferences = new double[validationData.length];
		double[] overshootPercentages = new double[validationData.length]; 


		//DAten sind eingelesen!

		// Parameter werden uebergeben


		if (columnSelector == null) {
			//			columnSelector = new IntRange(offset + "," + (parameters.size()+offset-1));
			//			columnSelector = new IntRange("5,24;26,"+ (parameters.size()+offset));  // PMAX FIX FOR OLD MODELS!
			columnSelector = new IntRange("33,166"); // All features
		}

		for (int val = 0; val < validationData.length ; val++) {


			Matrix[] sampleToLabels = createSample2Labels(validationData[val]);

			Matrix predictedLabels = sampleToLabels[0];
			Matrix avgLabels = sampleToLabels[1];

//			predictLabels(validationData[val], parameters, predictedLabels);

			AlgorithmController.computeSample2avgLabel(getWindowExtent(), predictedLabels, avgLabels);

			int predictedAnnotation = AlgorithmController.predictAnnotation(avgLabels, log);

			Accuracy accuracy = new Accuracy();

			double currentAcc = accuracy.evaluate(new DefaultVector(Matrices.col(validationLabels[val], COL_LABEL_IN_LABELS)),
					new DefaultVector(Matrices.col(predictedLabels, COL_LABEL_IN_SAMPLE2LABEL)));

			double currentSampleDiff = Math.abs(predictedAnnotation-validationAnnotations[val]);

			double overshootPercentage = 0;

			accuracies[val] = currentAcc;
			sampleDifferences[val] = currentSampleDiff;
			overshootPercentages[val] = overshootPercentage;
		}

		double accuracySum = 0;
		double sampleDifferenceSum = 0;
		double overshootPercentagesSum = 0;


		for (int i = 0; i < accuracies.length ; i++) {
			accuracySum += accuracies[i];
			sampleDifferenceSum += sampleDifferences[i];
			overshootPercentagesSum += overshootPercentages[i];
		}

		float avgAccuracy = (float) (accuracySum/accuracies.length);
		float avgSampleDiff = (float) (sampleDifferenceSum/accuracies.length);
		float avgOvershootPercentage = (float) (overshootPercentagesSum/accuracies.length);

		ret.setAccuracy(avgAccuracy);
		ret.setSampleDifference(avgSampleDiff);
		ret.setOvershootPercentage(avgOvershootPercentage);


		return ret;
	}
	
	public Quality predictForValidation(ModelFunctions modelFunction) {
		Quality ret = new Quality();

		double[] accuracies = new double[validationData.length];
		double[] sampleDifferences = new double[validationData.length];
		double[] overshootPercentages = new double[validationData.length]; 


		//DAten sind eingelesen!

		// Parameter werden uebergeben


		if (columnSelector == null) {
			int offset=5;
			//			columnSelector = new IntRange(offset + "," + (parameters.size()+offset-1));
			//			columnSelector = new IntRange("5,24;26,"+ (parameters.size()+offset));  // PMAX FIX FOR OLD MODELS!
			columnSelector = new IntRange("33,166"); // All features
		}

		for (int val = 0; val < validationData.length ; val++) {


			Matrix[] sampleToLabels = createSample2Labels(validationData[val]);

			Matrix predictedLabels = sampleToLabels[0];
			Matrix avgLabels = sampleToLabels[1];

//			predictLabels(validationData[val], modelFunction, predictedLabels);

			AlgorithmController.computeSample2avgLabel(getWindowExtent(), predictedLabels, avgLabels);

			int predictedAnnotation = AlgorithmController.predictAnnotation(avgLabels, log);

			Accuracy accuracy = new Accuracy();

			double currentAcc = accuracy.evaluate(new DefaultVector(Matrices.col(validationLabels[val], COL_LABEL_IN_LABELS)),
					new DefaultVector(Matrices.col(predictedLabels, COL_LABEL_IN_SAMPLE2LABEL)));

			double currentSampleDiff = Math.abs(predictedAnnotation-validationAnnotations[val]);

			double overshootPercentage = 0;

			accuracies[val] = currentAcc;
			sampleDifferences[val] = currentSampleDiff;
			overshootPercentages[val] = overshootPercentage;
		}

		double accuracySum = 0;
		double sampleDifferenceSum = 0;
		double overshootPercentagesSum = 0;


		for (int i = 0; i < accuracies.length ; i++) {
			accuracySum += accuracies[i];
			sampleDifferenceSum += sampleDifferences[i];
			overshootPercentagesSum += overshootPercentages[i];
		}

		float avgAccuracy = (float) (accuracySum/accuracies.length);
		float avgSampleDiff = (float) (sampleDifferenceSum/accuracies.length);
		float avgOvershootPercentage = (float) (overshootPercentagesSum/accuracies.length);

		ret.setAccuracy(avgAccuracy);
		ret.setSampleDifference(avgSampleDiff);
		ret.setOvershootPercentage(avgOvershootPercentage);


		return ret;
	}


	@Override
	public AnalysisResult predict(DataInterpretation rf) throws ModelApplicationException {

		/*
		 * uggh - dieser Test ist wirklich nicht schoen (aus Wiederverwendungssicht) ... funktioniert aber.
		 */
		if (rf.getPmaxManuell() != DataInterpretation.PMAX_MANUAL_DEFAULT) {
			return predict(rf, rf.getPmaxManuell());
		}
			
			
//		boolean isAcid = false;
		if (null != rf.getDataInterpretation() && rf.getDataInterpretation().getAbsolutePath().toLowerCase().contains("acid")) {
			this.annotationBaseDir="/acogpr/mhh/manual_annotations/AcidAnnotations/";
			this.annotator="sm";
		}
		
		//Read RF
		if (!rf.initialized())
			rf.run();
		log.info("Predictions will be computed for Swallow " + rf.getSwallowId() + "  for Proband " + rf.getProband());

		int pmax = getPmax(rf);

		// Preprocess the readFolder
		SwallowDS swallow = preprocessSwallow(rf, isSkipLeading(), isSkipBetween(), pmax);

		Matrix swallowData = swallow.data;

		for (int i = 0; i < getParameters().length ; i++) {

			if (getParameters()[i] == null) {
				try {
					parameters[i] = (Vectors.readDense(modelFiles[i]));
				} catch (IOException e) {
					log.error("Parameters can not be read from file: " + modelFiles[i] + " and are not set. ");
					e.printStackTrace();
				}
			}
		}


		if (columnSelector == null) {
			columnSelector = new IntRange("33,166");
		}

		// apply parameters from all modelFiles to data in rf

		Matrix[][] allSampleToLabels = new Matrix[getParameters().length][2];

		for (int i = 0; i < getParameters().length ; i++) {
			allSampleToLabels[i] = createSample2Labels(swallowData);
			predictLabels(swallow, getParameters()[i], allSampleToLabels[i][0]);
			AlgorithmController.computeSample2avgLabel(getWindowExtents()[i], allSampleToLabels[i][0], allSampleToLabels[i][1]);
		}

		Matrix avgLabels = allSampleToLabels[0][1];

		for (int i = 0; i < allSampleToLabels[0][0].getNumRows(); i++) {
			float rowSum = 0;
			float divisor = getParameters().length;
			for (int j = 0; j < getParameters().length ; j++) {
				if (allSampleToLabels[j][1].get(i, COL_LABEL_IN_SAMPLE2LABEL) == 0) {
					rowSum += allSampleToLabels[j][1].get(i, COL_LABEL_IN_SAMPLE2LABEL);
					divisor = divisor - 1;
				}
				else {
					rowSum += allSampleToLabels[j][1].get(i, COL_LABEL_IN_SAMPLE2LABEL);
				}

			}
			if (divisor == 0) {
				// Nothing happens because rowSum is divided by 1
			}
			else {
				rowSum = rowSum/( divisor);
			}
			
			avgLabels.set(i, COL_LABEL_IN_SAMPLE2LABEL, rowSum);
		}

		// That was all the ensemble nonsense!

		//Evaluate the certainty

		float modelCertainty=0;

		float countChanges = (float) countChanges(Vectors.col(avgLabels, COL_LABEL_IN_SAMPLE2LABEL));

		modelCertainty = 1/(countChanges-1);

		log.info("Everything successfull, will return Annotation and Predictions now...");

		int annotation = AlgorithmController.predictAnnotation(avgLabels, log);

		this.averagePredictions = new DefaultMatrix(new ColumnSubsetMatrixView(avgLabels,
				new int[] {COL_REL_SAMPLE_IDX, COL_LABEL_IN_SAMPLE2LABEL}));

		Vector predictions = Vectors.col(avgLabels, COL_LABEL_IN_SAMPLE2LABEL);

		int absoluteAnnotation = annotation + rf.getFirstSample();
		String absoluteAnnotationTime = Parser.sample2Time(absoluteAnnotation, 50);
		String relativeAnnotationTime = Parser.sample2Time(annotation, 50);

		int pmaxSample = (int) avgLabels.get(0, COL_PMAX_SAMPLE_IDX);

		int relativePmaxSample = pmaxSample - rf.getFirstSample();
		System.out.println("Pmax is at sample: " + relativePmaxSample);

//		int trueSample = getAnnotation(rf) - rf.getFirstSample();


		AnalysisResult ret = new AnalysisResult();
		ret.setSample2AvgLabels(averagePredictions);
		ret.setPmaxSample(relativePmaxSample);
		ret.setModelCertainty(modelCertainty);
		ret.setAbsoluteAnnotationTime(absoluteAnnotationTime);
		ret.setAnnotationTime(relativeAnnotationTime);
		ret.setAbsoluteEndSample(absoluteAnnotation);
		ret.setEndSample(annotation);
//		ret.setTrueSample(trueSample);
		ret.setPredictions(predictions);

		return ret;
	}

	

	@Override
	public AnalysisResult predict(DataInterpretation rf, int pmax) throws ModelApplicationException {

//		boolean isAcid = false;
		if (null != rf.getDataInterpretation() && rf.getDataInterpretation().getAbsolutePath().toLowerCase().contains("acid")) {
			this.annotationBaseDir="/acogpr/mhh/manual_annotations/AcidAnnotations/";
			this.annotator="sm";
		}
		
		//Read RF
		if (!rf.initialized())
			rf.run();
		log.info("Predictions will be computed for Swallow " + rf.getSwallowId() + "  for Proband " + rf.getProband());

		
		// Preprocess the readFolder
		SwallowDS swallow = preprocessSwallow(rf, isSkipLeading(), isSkipBetween(), pmax);

		Matrix swallowData = swallow.data;

		for (int i = 0; i < getParameters().length ; i++) {

			if (getParameters()[i] == null) {
				try {
					parameters[i] = (Vectors.readDense(modelFiles[i]));
				} catch (IOException e) {
					log.error("Parameters can not be read from file: " + modelFiles[i] + " and are not set. ");
					e.printStackTrace();
				}
			}
		}


		if (columnSelector == null) {
			columnSelector = new IntRange("33,166");
		}

		// apply parameters from all modelFiles to data in rf

		Matrix[][] allSampleToLabels = new Matrix[getParameters().length][2];

		for (int i = 0; i < getParameters().length ; i++) {
			allSampleToLabels[i] = createSample2Labels(swallowData);
			predictLabels(swallow, getParameters()[i], allSampleToLabels[i][0]);
			AlgorithmController.computeSample2avgLabel(getWindowExtents()[i], allSampleToLabels[i][0], allSampleToLabels[i][1]);
		}

		Matrix avgLabels = allSampleToLabels[0][1];

		for (int i = 0; i < allSampleToLabels[0][0].getNumRows(); i++) {
			float rowSum = 0;
			float divisor = getParameters().length;
			for (int j = 0; j < getParameters().length ; j++) {
				if (allSampleToLabels[j][1].get(i, COL_LABEL_IN_SAMPLE2LABEL) == 0) {
					rowSum += allSampleToLabels[j][1].get(i, COL_LABEL_IN_SAMPLE2LABEL);
					divisor = divisor - 1;
				}
				else {
					rowSum += allSampleToLabels[j][1].get(i, COL_LABEL_IN_SAMPLE2LABEL);
				}

			}
			if (divisor == 0) {
				// Nothing happens because rowSum is divided by 1
			}
			else {
				rowSum = rowSum/( divisor);
			}
			
			avgLabels.set(i, COL_LABEL_IN_SAMPLE2LABEL, rowSum);
		}

		// That was all the ensemble nonsense!

		//Evaluate the certainty

		float modelCertainty=0;

		float countChanges = (float) countChanges(Vectors.col(avgLabels, COL_LABEL_IN_SAMPLE2LABEL));

		modelCertainty = 1/(countChanges-1);

		log.info("Everything successfull, will return Annotation and Predictions now...");

		int annotation = AlgorithmController.predictAnnotation(avgLabels, log);

		this.averagePredictions = new DefaultMatrix(new ColumnSubsetMatrixView(avgLabels,
				new int[] {COL_REL_SAMPLE_IDX, COL_LABEL_IN_SAMPLE2LABEL}));

		Vector predictions = Vectors.col(avgLabels, COL_LABEL_IN_SAMPLE2LABEL);

		int absoluteAnnotation = annotation + rf.getFirstSample();
		String absoluteAnnotationTime = Parser.sample2Time(absoluteAnnotation, 50);
		String relativeAnnotationTime = Parser.sample2Time(annotation, 50);

		int pmaxSample = (int) avgLabels.get(0, COL_PMAX_SAMPLE_IDX);

		int relativePmaxSample = pmaxSample - rf.getFirstSample();
		System.out.println("Pmax is at sample: " + relativePmaxSample);

//		int trueSample = getAnnotation(rf) - rf.getFirstSample();


		AnalysisResult ret = new AnalysisResult();
		ret.setSample2AvgLabels(averagePredictions);
		ret.setPmaxSample(relativePmaxSample);
		ret.setModelCertainty(modelCertainty);
		ret.setAbsoluteAnnotationTime(absoluteAnnotationTime);
		ret.setAnnotationTime(relativeAnnotationTime);
		ret.setAbsoluteEndSample(absoluteAnnotation);
		ret.setEndSample(annotation);
//		ret.setTrueSample(trueSample);
		ret.setPredictions(predictions);

		return ret;
	}




	public AnalysisResult predictWithParameters(DataInterpretation rf, Vector parameters) throws ModelApplicationException {

		rf.run();
		log.info("Predictions will be computed for Swallow " + rf.getSwallowId() + "  for Proband " + rf.getProband());

		//GET THE ANNOTATION!

		int restitutionsSample = getAnnotation(rf);
		int relativeRestitutionsSample = restitutionsSample - rf.getFirstSample(); 

		// Preprocess the readFolder
		SwallowDS swallow = preprocessTestSwallow(rf, restitutionsSample, isSkipLeading(), isSkipBetween());


		//GET DATA AND LABELS!!

		Matrix swallowData = swallow.data;
		Matrix swallowLabels = swallow.labels;


		// apply parameters from modelFile to data in rf

		Matrix[] sampleToLabels = createSample2Labels(swallowData);

		Matrix predictedLabels = sampleToLabels[0];
		Matrix avgLabels = sampleToLabels[1];

		// Predict the Labels!!

		predictLabels(swallow, parameters, predictedLabels);

		//Evaluate the Prediction!

		Accuracy acc = new Accuracy();

		float accuracy = acc.evaluate(Vectors.col(predictedLabels, COL_LABEL_IN_SAMPLE2LABEL), 
				Vectors.col(swallowLabels, COL_LABEL_IN_LABELS));




		// LABELS smoothen!!

		AlgorithmController.computeSample2avgLabel(getWindowExtent(), predictedLabels, avgLabels);

		int annotation = AlgorithmController.predictAnnotation(avgLabels, log);

		Vector predictions = Vectors.col(avgLabels, COL_LABEL_IN_SAMPLE2LABEL);

		//Evaluate the certainty

		float modelCertainty = 0;

		float countChanges = (float) countChanges(Vectors.col(avgLabels, COL_LABEL_IN_SAMPLE2LABEL));

		modelCertainty = 1/(countChanges+1);

		log.info("Everything successfull, will return Annotation and Predictions now...");

		// Berechne Sample RMSE!

		float rmse = (annotation-relativeRestitutionsSample)*(annotation-relativeRestitutionsSample);
		int absDifference = Math.abs(annotation-relativeRestitutionsSample);

		// Schreibe Output!

		AnalysisResult ret = new AnalysisResult();

		ret.setEndSample(annotation);
		ret.setPredictions(predictions);
		ret.setTrueSample(relativeRestitutionsSample);
		ret.setAccuracy(accuracy);
		ret.setSampleWiseRMSE(rmse);
		ret.setSampleDifference(absDifference);
		ret.setModelCertainty(modelCertainty);



		return ret;
	}



	public static int countChanges(Vector input) {
		int count = 0;

		int size = input.size();
		for (int i = 1; i < size ; i++) {
			if (Math.signum(  input.get(i-1)) != Math.signum(input.get(i))    ) {
				count++;
			}
		}

		return count;
	}

	/** 
	 * Predicts Labels of a given swallow as input data, uses learned model parameters,
	 *  writes the predicted labels into the sample2Labels Matrix
	 * @param swallow
	 * @param parameters
	 * @param predictedLabels
	 */
	public void predictLabels(SwallowDS swallow, Vector parameters, Matrix predictedLabels) {

		float[] predict = modelFunction.predictAsClassification(
				modelFunction.evaluate(
						Matrices.asArray( 
				new ColumnSubsetMatrixView(swallow.data, getColumnSelector().getUsedIndexes()) 
				)
				, Vectors.toFloatArray(parameters)));
		Vector predictVector = Vectors.floatArraytoVector(predict);

		for (Integer j : swallow.throwAway) {
			predictVector.set(j, 0);
		}

		Vectors.copy(predictVector, Matrices.col(predictedLabels,COL_LABEL_IN_SAMPLE2LABEL ));
	}

	


	public SwallowDS preprocessValidationSwallow(DataInterpretation folder, boolean skipLeading, boolean skipBetween,
			int pmaxSample) throws ModelApplicationException {

		int idxMaxSample2;

		// GET the absolute annotation! Absolute since it will be compared with the Abs Sample Indexes!

		int annotation = getAnnotation(folder);

		// Concatenate the data given in the ReadFolder to a SwallowDS

		SwallowDS ret = new SwallowDS();
		Matrix data = algcon.concatenate(log, folder, annotation, true, pmaxSample);

		System.out.println(folder.getDataInterpretation());

		int numRows = data.getNumRows();

		if (pmaxSample < 0) {
			idxMaxSample2 = (int) data.get(0, COL_PMAX_SAMPLE_IDX);
		}
		else {
			idxMaxSample2 = pmaxSample;
		}

		int rdEndSample = folder.getRdEndSample();
		int rdStartSample = folder.getRdStartSample();
		ArrayList<Integer> throwAway = new ArrayList<>();
		DefaultMatrix labels = new DefaultMatrix(data.getNumRows(), /*SampleId, label*/ 2);
		// default clazz is -9999 (indicator for missing)
		Vectors.set(Matrices.col(labels, 1), -9999);

		for (int j = 0; j < numRows; j++) {
			// copy over sample indizes to label matrix 
			labels.set(j, 0, (int) data.get(j, COL_ABS_SAMPLE_IDX)); 

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
		DefaultBitVector throwVector = new DefaultBitVector(numRows);
		Vectors.set(throwVector, true);
		for (Integer j : throwAway) {
			throwVector.set(j.intValue(), false);
		}
		RowSubsetMatrixView dataFiltered = new RowSubsetMatrixView(data, throwVector, true);
		RowSubsetMatrixView labelsFiltered = new RowSubsetMatrixView(labels, throwVector, true);

		ret.data=dataFiltered;
		ret.labels=labelsFiltered;
		return ret;
	}



	/**
	 * Reads a swallow; preprocessing the data, s.t.:
	 * 
	 * <ol>
	 * <li>Data is aggregated (e.g., concatenating FFT and Pressure samples, adding metadata, see {@link #concatenate(DataInterpretation)})
	 * <li>Data is narrowed (e.g., removing non-informative samples (between rdend and pmaxsample)
	 * </ol>
	 * @param colSelector 
	 * 
	 * 
	 * @return 
	 * @throws ModelApplicationException 
	 */
	public SwallowDS preprocessSwallow(DataInterpretation folder, boolean skipLeading, boolean skipBetween)
			throws ModelApplicationException {

		return preprocessSwallow(folder, skipLeading, skipBetween, -1);
	}


	public SwallowDS preprocessSwallow(DataInterpretation folder, boolean skipLeading, 
			boolean skipBetween,
			int pmaxSample) throws ModelApplicationException {

		int idxMaxSample2;

		SwallowDS ret = new SwallowDS();
		Matrix data = algcon.concatenate(log, folder, -1, true, pmaxSample);
		int numRows = data.getNumRows();


		if (pmaxSample < 1) {
			idxMaxSample2 = (int) data.get(0, COL_PMAX_SAMPLE_IDX);
		}
		else {
			idxMaxSample2 = pmaxSample;
		}

		int rdEndSample = folder.getRdEndSample();
		int rdStartSample = folder.getRdStartSample();
		ArrayList<Integer> throwAway = new ArrayList<>();
		DefaultMatrix labels = new DefaultMatrix(data.getNumRows(), /*SampleId, label*/ 2);
		// default clazz is -9999 (indicator for missing)
		Vectors.set(Matrices.col(labels, 1), -9999);

		for (int j = 0; j < numRows; j++) {
			// copy over sample indizes to label matrix 
			labels.set(j, 0, (int) data.get(j, COL_ABS_SAMPLE_IDX)); 

			float currentDataSampleId = data.get(j, COL_ABS_SAMPLE_IDX);

			if (currentDataSampleId <= rdStartSample) {
				if (skipLeading) {
					throwAway.add(Integer.valueOf(j));
				} 
				continue;
			}
			if (currentDataSampleId>=rdEndSample && currentDataSampleId <= idxMaxSample2) {
				if (skipBetween) {
					throwAway.add(Integer.valueOf(j));
				}
				continue;
			}
		}
		DefaultBitVector throwVector = new DefaultBitVector(numRows);
		Vectors.set(throwVector, true);
		for (Integer j : throwAway) {
			throwVector.set(j.intValue(), false);
		}

		// KEINE FILTERUNG!! im APPLY MODEL WEIL HIER NICHT MEHR GELERNT WIRD!!!

		//		RowSubsetMatrixView dataFiltered = new RowSubsetMatrixView(data, throwVector, true);
		//		RowSubsetMatrixView labelsFiltered = new RowSubsetMatrixView(labels, throwVector, true);	

		ret.throwAway = throwAway;
		ret.data=data;
		ret.labels=labels;
		return ret;
	}


	/**
	 * Creates a sample2Labels array; one sample2Labels for predicted labels,
	 * one for the averagedLabels, having the structure metadata | labels,
	 *  these can be used to compute avg Labels and to then deduce the annotation.
	 */
	private Matrix[] createSample2Labels(Matrix input) {
		return AlgorithmController.createSample2Labels(input);
	}



	public int getPmax(DataInterpretation folder) {
		int pmax = 0;

		Matrix annotations;

		int probandId = folder.getProband();
		int swallowId = folder.getSwallowId();

		String annotationPath = annotationBaseDir + probandId + "-" + annotator + ".tsv";

		// Parser modifizieren!

		try {
			annotations = Parser.readAnnotations(new File(annotationPath), folder.getSamplerateAsInt());
			pmax = (int) annotations.get(swallowId-1, Parser.ANNOTATION_COL_PMAX_SAMPLE);
			log.info("Pmax is given, will continue...");
		} catch (IOException e) {
			log.info("Pmax has not been provided for:  " + (null != folder.getDataInterpretation()? folder.getDataInterpretation().toString():" <a manual DataInterpretation Object>"));
			log.info("The annotation path is: " + annotationPath);
			pmax = -1;
		} catch (ArrayIndexOutOfBoundsException e) {
			log.info("ArrayIndexOutOfBoundsException :: " + folder.getDataInterpretation().toString());
			log.info("The annotation path is: " + annotationPath);
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

		Matrix annotations;

		int probandId = folder.getProband();
		int swallowId = folder.getSwallowId();

		String annotationPath = getAnnotationBaseDir() + probandId + "-" + getAnnotator() + ".tsv";

		try {
			annotations = Parser.readAnnotations(new File(annotationPath), folder.getSamplerateAsInt());
			restitutionszeitSample = (int) annotations.get(swallowId-1, Parser.ANNOTATION_COL_RESTITUTIONSZEIT_SAMPLE);
			log.info("Annotation is given, will continue...");
		} catch (IOException e) {
			log.info("Annotations have not been provided for:  " + folder.getDataInterpretation().toString() + " as the annotation Path is: " + annotationPath);
			restitutionszeitSample = (int) folder.getLastSample();
		}

		return restitutionszeitSample;

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
	public SwallowDS preprocessTestSwallow(DataInterpretation folder, int annotation, boolean skipLeading, boolean skipBetween) throws ModelApplicationException {
		SwallowDS ret = new SwallowDS();

		System.out.println(folder.getDataInterpretation());

		//		annotation = getAnnotation(folder);

		Matrix data = algcon.concatenate(log, folder, annotation, true);
		int numRows = data.getNumRows();


		int idxMaxSample2 = (int) data.get(0, COL_PMAX_SAMPLE_IDX);

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
//		ret.ruheDruck = ruheDruck;
//		ret.ruheDruckLabels = ruheDruckLabels2;

		return ret;
	}

	///// GETTERS AND SETTERS


	public boolean isSkipBetween() {
		return skipBetween;
	}

	public void setSkipBetween(boolean skipBetween) {
		this.skipBetween = skipBetween;
	}

	public boolean isSkipLeading() {
		return skipLeading;
	}

	public void setSkipLeading(boolean skipLeading) {
		this.skipLeading = skipLeading;
	}

	@Override
	public int getWindowExtent() {
		return windowExtent;
	}

	@Override
	public void setWindowExtent(int windowExtent) {
		this.windowExtent = windowExtent;
	}

	public ModelFunctions getModelFunction() {
		return modelFunction;
	}

	public void setModelFunction(ModelFunctions modelFunction) {
		this.modelFunction = modelFunction;
	}

	public IntRange getColumnSelector() {
		return columnSelector;
	}

	public void setColumnSelector(IntRange columnSelector) {
		this.columnSelector = columnSelector;
	}

	public String getAnnotationBaseDir() {
		return annotationBaseDir;
	}

	public void setAnnotationBaseDir(String annotationBaseDir) {
		this.annotationBaseDir = annotationBaseDir;
	}

	public String getAnnotator() {
		return annotator;
	}

	public void setAnnotator(String annotator) {
		this.annotator = annotator;
	}

	public void setAveragePredictions(Matrix averagePredictions) {
		this.averagePredictions = averagePredictions;
	}

	@Override
	public Matrix getAveragePredictions() {
		return averagePredictions;
	}

	/**
	 * @param directory
	 * @return a valid object instance
	 * @throws IOException if file reading fails
	 */
	public static ApplyMHHModelImpl fromDirectory(File directory) throws IOException {
		ApplyMHHModelImpl ret = new ApplyMHHModelImpl();

		// directory kann einmal das Verzeichnis sein, in dem Parameter und Window Extent liegen, Oder ein Verzeichnis von
		// Verzeichnissen in denen Parameter und Window Extent liegen = ENSEMBLE

		Vector[] parameterVectors;
		int[] windowExtents;

		File[] parameterFiles = directory.listFiles();

		if (parameterFiles[0].isDirectory() == false ) {
			// directory besitzt nur parameter und WindowExtent File
			// Standard Modell Anwendend
			Vector parametersV = Vectors.readDense(new File(directory, FILENAME_PARAMETERS));
			int windowExtent = ( (Integer) CommandLineParser.convert(Parser.readFileCompletely( 
					new File(directory, FILENAME_WINDOW_EXTENT)) , Integer.class ));
			parameterVectors = new Vector[] { parametersV };
			windowExtents = new int[] {windowExtent};
		}
		else {
			// directory beinhaltet mehrere directories mit parameter und window Extent File.
			// ENSEMBLING!
			parameterVectors = new Vector[parameterFiles.length];
			windowExtents = new int[parameterFiles.length];
			for (int i = 0; i < parameterVectors.length ; i++) {
				parameterVectors[i] = Vectors.readDense(new File(parameterFiles[i], FILENAME_PARAMETERS) );
				windowExtents[i] = ( (Integer) CommandLineParser.convert(Parser.readFileCompletely( 
						new File(parameterFiles[i], FILENAME_WINDOW_EXTENT)) , Integer.class ));

			}
		}

		ret.setParameters(parameterVectors);
		ret.setWindowExtents(windowExtents);

		return ret;
	}

	public File[] getModelFiles() {
		return modelFiles;
	}

	public void setModelFiles(File[] modelFiles) {
		this.modelFiles = modelFiles;
	}

	public Vector[] getParameters() {
		return parameters;
	}

	public void setParameters(Vector[] parameters) {
		this.parameters = parameters;
	}

	public int[] getWindowExtents() {
		return windowExtents;
	}

	public void setWindowExtents(int[] windowExtents) {
		this.windowExtents = windowExtents;
	}

	@Override
	public String getHumanName() {
		if (null!=modelFiles && modelFiles.length>0) {
			return "Ensemble " + modelFiles[0].getAbsolutePath();
		}
		return "unknown";
	}

}
