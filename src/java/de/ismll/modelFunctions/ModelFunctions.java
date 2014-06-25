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
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.impl.DefaultVector;

public abstract class ModelFunctions {
	
	private float bestAccuracy;
	private float bestSampleDiff;

	public void initialize(AlgorithmController algcon) {
		log.fatal("The given model function cannot initialize itself!");
	}

	public void saveBestParameters(Quality quality,  String forWhat) {
		log.fatal("The given model function cannot save the best parameters found so far!");
		}
	
	public float computeMajorityClassAccuracy(Vector labels) {
		int countPositive = 0;
		int countNegative = 0;
		for (int i = 0; i < labels.size() ; i++) {
			if (labels.get(i) > 0) {
				countPositive++;
			}
			else {
				countNegative++;
			}
		}
		float majority1 = (float) countNegative/ ( (float) labels.size() );
		float majority2 = (float) countPositive/ ( (float) labels.size() );
		float ret = 0;
		if (majority1 > majority2) {
			ret = majority1;
		}
		else {
			ret = majority2;
		}
		return ret;
	}
	
		
	public Quality evaluateModel(Matrix[] applyData, Matrix[] applyLabels, int[] applyAnnotations
			, int windowExtent, IntRange columnSelector) {
		
		Quality ret = new Quality();
		
		
		double[] accuracies = new double[applyData.length];
		double[] sampleDifferences = new double[applyData.length];
		double[] overshootPercentages = new double[applyData.length]; 

		for (int data = 0; data < applyData.length ; data++) {

			Matrix[] sampleToLabels = AlgorithmController.createSample2Labels(applyData[data]);

			Matrix predictedLabels = sampleToLabels[0];
			Matrix avgLabels = sampleToLabels[1];
			
			Matrix apply2Data = new DefaultMatrix(AlgorithmController.preprocess(applyData[data] , columnSelector));
			
			float[] predicted = this.evaluate(apply2Data);
			
			// convert to classification i.e. -1 and 1
			
			float[] predictedClassification = this.predictAsClassification(predicted);
		
			// make a vector

			Vector predictVector = Vectors.floatArraytoVector(predictedClassification);

			// copy it to the sample2labels object

			Vectors.copy(predictVector, Matrices.col(predictedLabels,COL_LABEL_IN_SAMPLE2LABEL ));

			AlgorithmController.computeSample2avgLabel( windowExtent, predictedLabels, avgLabels);

			int predictedAnnotation = AlgorithmController.predictAnnotation(avgLabels, log);

			int trueAnnotation = applyAnnotations[data];
			
//			System.out.println("predicted annotation: " + predictedAnnotation);
//			System.out.println("true annotation: " + trueAnnotation);
			
			double currentSampleDiff = Math.abs(predictedAnnotation-trueAnnotation);

			Accuracy accuracy = new Accuracy();
			
			Vector trueLabels = Matrices.col(applyLabels[data] , COL_LABEL_IN_LABELS);
			
			double currentAcc = accuracy.evaluate(trueLabels, predictVector);

//			double currentAcc = accuracy.evaluate(new DefaultVector(Matrices.col(rawData.getValidationData()[val], COL_LABEL_IN_LABELS)),
//					new DefaultVector(Matrices.col(predictedLabels, COL_LABEL_IN_SAMPLE2LABEL)));

			

			double overshootPercentage = 0;

			accuracies[data] = currentAcc;
			sampleDifferences[data] = currentSampleDiff;
			overshootPercentages[data] = overshootPercentage;
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




	// every model function has its own form of parameters that are learned... they will be stored in the function class 

	// every model function should implement some SGD method on data given a multiplier that will be computed depending on the loss

	// what is an instance to a model function? usually a float[] array but can also be a sparse vector 

	protected Logger log = LogManager.getLogger(getClass());




	//IF methods (except convert() ) are NOT overwritten by sub classes, they make no sense, give a fatal log entry and should return null!

	public static ModelFunctions convert(Object in) {
		String str;
		str = (String) in;

		switch(str) {
		case "lm":
			return new LinearRegressionPrediction();

		case "fm":
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
		float[] ret = new float[data.getNumRows()];

		for (int i = 0; i < ret.length ; i++) {
			ret[i] = evaluate(Matrices.row(data, i));
		}
		return ret;
	}

	public float[] evaluate(TIntFloatHashMap[] data) {
		float[] ret = new float[data.length];
		
		for (int i = 0; i < ret.length ; i++) {
			ret[i] = this.evaluate(data[i]);
		}
		return ret;
	}

	public void SGD(TIntFloatHashMap x, float multiplier , float learnRate) {
		log.fatal("The chosen model function does not implement SGD()");
	}

	public void SGD(Vector instance, float multiplier , float learnRate) {
		log.fatal("The chosen model function does not implement SGD()");
	}
	
	public void GD(Matrix data, float[] multipliers, float learnRate) {
		log.fatal("The chosen model function does not implement GD()");
	}
	
	
	public void LAPGD(Matrix data, float[] multipliersFit, float[][] multipliersSmooth,
			float learnRate, int[] randomIndices, float[] instanceMultipliersSmooth, float[][] sigmoidDifferences) {
		log.fatal("The chosen model does not implement LAPGD() ");
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
	
	public float computeSigmoid(float x) {
		float exp = (float) Math.exp(-x);
		float ret = 1/(1+exp);
		return ret;
	}
	
	
	public float[] computeSigmoids(float[] x) {
		float[] ret = new float[x.length];
		for (int i = 0; i < ret.length ; i++) {
			ret[i] = computeSigmoid(x[i]);
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
