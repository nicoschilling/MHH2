package de.ismll.modelFunctions;

import static de.ismll.secondversion.DatasetFormat.COL_LABEL_IN_LABELS;
import static de.ismll.secondversion.DatasetFormat.COL_LABEL_IN_SAMPLE2LABEL;
import gnu.trove.map.hash.TIntFloatHashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.evaluation.Accuracy;
import de.ismll.secondversion.AlgorithmController;
import de.ismll.secondversion.IntRange;
import de.ismll.secondversion.MhhDataset;
import de.ismll.secondversion.MhhRawData;
import de.ismll.secondversion.Quality;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultVector;

public abstract class ModelFunctions {
	
	private float bestAccuracy;
	private float bestSampleDiff;

	public void initialize(AlgorithmController algcon) {
		log.fatal("The given model function cannot initialize itself!");
	}

	public Quality saveBestParameters(MhhRawData rawData, int windowExtent, String forWhat, IntRange columnSelector) {
		log.fatal("The given model function cannot save the best parameters found so far!");
		return null;
	}
	
		
	public Quality evaluateModelFunctionOnValidation(MhhRawData rawData, int windowExtent, IntRange columnSelector) {
		
		Quality ret = new Quality();
		
		double[] accuracies = new double[rawData.getValidationData().length];
		double[] sampleDifferences = new double[rawData.getValidationData().length];
		double[] overshootPercentages = new double[rawData.getValidationData().length]; 
		
//		IntRange columnSelector = new IntRange("33,166");

		for (int val = 0; val < rawData.getValidationData().length ; val++) {


			Matrix[] sampleToLabels = AlgorithmController.createSample2Labels(rawData.getValidationData()[val]);

			Matrix predictedLabels = sampleToLabels[0];
			Matrix avgLabels = sampleToLabels[1];
			
			float[] predictedVal = this.evaluate(rawData.getValidationData()[val]);
			
			// convert to classification i.e. -1 and 1
			
			float[] predictedValClassification = this.predictAsClassification(predictedVal);
		
			// make a vector

			Vector predictVector = Vectors.floatArraytoVector(predictedValClassification);

			// copy it to the sample2labels object

			Vectors.copy(predictVector, Matrices.col(predictedLabels,COL_LABEL_IN_SAMPLE2LABEL ));

			AlgorithmController.computeSample2avgLabel( windowExtent, predictedLabels, avgLabels);

			int predictedAnnotation = AlgorithmController.predictAnnotation(avgLabels, log);

			Accuracy accuracy = new Accuracy();

			double currentAcc = accuracy.evaluate(new DefaultVector(Matrices.col(rawData.getValidationData()[val], COL_LABEL_IN_LABELS)),
					new DefaultVector(Matrices.col(predictedLabels, COL_LABEL_IN_SAMPLE2LABEL)));

			double currentSampleDiff = Math.abs(predictedAnnotation-rawData.getValidationDataAnnotations()[val]);

			double overshootPercentage = 0;

			accuracies[val] = currentAcc;
			sampleDifferences[val] = currentSampleDiff;
			overshootPercentages[val] = overshootPercentage;
		}
		
		float[] allRSS = new float[rawData.getTrainData().length];
		
		for (int train = 0; train < rawData.getTrainData().length ; train++) {
			float[] predictedTrain = this.evaluate(rawData.getTrainData()[train]);
			
			float rss=0;
			
			for (int instance = 0; instance < predictedTrain.length ; instance++) {
				float error	= predictedTrain[instance] - rawData.getTrainDataLabels()[train].get(instance, COL_LABEL_IN_LABELS);
				rss += error*error;
			}
			
			allRSS[train] = rss;
		}
		
		float allRSSSum = 0;
		
		for (int i = 0; i < allRSS.length ; i++) {
			allRSSSum += allRSS[i];
		}
		allRSSSum = allRSSSum/allRSS.length;

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
		ret.setRss(allRSSSum);


		return ret;
	}




	// every model function has its own form of parameters that are learned... they will be stored in the function class 

	// every model function should implement some SGD method on data given a multiplier that will be computed depending on the loss

	// what is an instance to a model function? usually a float[] array but can also be a sparse vector 

	protected Logger log = LogManager.getLogger(getClass());




	//IF methods (except convert() ) are NOT overwritten by sub classes, they make no sense, give a fatal log entry and should return null!

	public static ModelFunctions convert(Object in) {
		String str;
		str = (String) in;

		switch(str) {
		case "linearModel":
			return new LinearRegressionPrediction();

		case "factorizationMachine":
			return new FmModel();

			//TODO: Add and implement more different Models!!

		default:
			System.out.println("FATAL: Cannot convert to a known Model Function!");
			return null;
		}
	}

	public float evaluate(Vector instance) {
		log.fatal("The chosen model function does not implement evaluate()");
		return 0;
	}

	public float evaluate(TIntFloatHashMap instance) {
		log.fatal("The chosen model function does not implement evaluate()");
		return 0;
	}

	public float[] evaluate(Matrix data) {
		log.fatal("The chosen model function does not implement evaluate()");
		return null;
	}

	public float[] evaluate(TIntFloatHashMap[] data) {
		log.fatal("The chosen model function does not implement evaluate()");
		return null;
	}

	public void SGD(TIntFloatHashMap x, float multiplier , float learnRate) {
		log.fatal("The chosen model function does not implement SGD()");
	}

	public void SGD(Vector instance, float multiplier , float learnRate) {
		log.fatal("The chosen model function does not implement SGD()");
	}




	/**
	 * Returns an array of predicted values given a dataset and parameters.
	 * @param data
	 * @param parameters
	 * @return
	 */
	public float[] evaluate(float[][] data, float[] parameters) {
		log.fatal("The chosen model function does not implement evaluate()");
		return null;
	}


	/**
	 * Morphs the output of evaluate into categorical variables, i.e. for binary classificiation doing logistic regression
	 * @param predict
	 * @return
	 */
	public float[] predictAsClassification(float[] predict) {
		float[] ret = new float[predict.length];
		for (int i = 0; i < ret.length ; i++) {
			if (predict[i] > 0) {
				ret[i] = 1;
			}
			else {
				ret[i] = -1;
			}
		}
		return ret;
	}

	public float getBestAccuracy() {
		return bestAccuracy;
	}

	public void setBestAccuracy(float bestAccuracy) {
		this.bestAccuracy = bestAccuracy;
	}

	public float getBestSampleDiff() {
		return bestSampleDiff;
	}

	public void setBestSampleDiff(float bestSampleDiff) {
		this.bestSampleDiff = bestSampleDiff;
	}









}
