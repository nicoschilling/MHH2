package de.ismll.modelFunctions;

import static de.ismll.secondversion.DatasetFormat.COL_LABEL_IN_LABELS;
import static de.ismll.secondversion.DatasetFormat.COL_LABEL_IN_SAMPLE2LABEL;
import gnu.trove.map.hash.TIntFloatHashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.evaluation.Accuracy;
import de.ismll.secondversion.Algorithm;
import de.ismll.secondversion.AlgorithmController;
import de.ismll.secondversion.Calculations;
import de.ismll.secondversion.IntRange;
import de.ismll.secondversion.Quality;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.impl.DefaultVector;
import de.ismll.table.projections.ColumnSubsetMatrixView;

public abstract class ModelFunctions {
	
	private float bestAccuracy;
	private float bestSampleDiff;

	public abstract void initialize(AlgorithmController algcon);

	public void saveBestParameters(Quality quality,  String forWhat) {
		log.fatal("The given model function cannot save the best parameters found so far!");
		}
	
	public static float computeMajorityClassAccuracy(Vector labels) {
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
	
		
	public Quality evaluateModel(final Matrix[] applyData, final Matrix[] trueSamplewiseLabels, final int[] timebasedAnnotation
			,final int windowExtent, final IntRange columnSelector) {
		
		final Quality ret = new Quality();
		
		final double[] accuracies = new double[applyData.length];
		final double[] sampleDifferences = new double[applyData.length];
		final double[] overshootPercentages = new double[applyData.length]; 

		for (int data = 0; data < applyData.length ; data++) {

			final Matrix[] sampleToLabels = AlgorithmController.createSample2Labels(applyData[data]);

			final Matrix predictedLabels = sampleToLabels[0];
			final Matrix avgLabels = new DefaultMatrix( sampleToLabels[1]);
			
			final Matrix preprocess = new ColumnSubsetMatrixView(applyData[data], columnSelector.getUsedIndexes());			
			
			final float[] predicted = this.evaluate(preprocess);
			
			// convert to classification i.e. -1 and 1
			
			final float[] predictedClassification = this.predictAsClassification(predicted);
		
			// make a vector

			final Vector _predictedSamplewiseLabels = DefaultVector.wrap(predictedClassification);

			// copy it to the sample2labels object
			Vectors.copy(_predictedSamplewiseLabels, Matrices.col(predictedLabels,COL_LABEL_IN_SAMPLE2LABEL ));

			AlgorithmController.computeSample2avgLabel( windowExtent, predictedLabels, avgLabels);

			final int predictedIdxAnnotation = AlgorithmController.predictAnnotation(avgLabels, log);

			final int trueTimebasedIdxAnnotation = timebasedAnnotation[data];
			
//			System.out.println("predicted annotation: " + predictedAnnotation);
//			System.out.println("true annotation: " + trueAnnotation);
			
			final double currentSampleIdxDiff = Math.abs(predictedIdxAnnotation-trueTimebasedIdxAnnotation);

			final Accuracy accuracy = new Accuracy();
			
			final Vector _trueSamplewiseLabels = Matrices.col(trueSamplewiseLabels[data] , COL_LABEL_IN_LABELS);
			
			final double currentAcc = accuracy.evaluate(_trueSamplewiseLabels, _predictedSamplewiseLabels);

//			double currentAcc = accuracy.evaluate(new DefaultVector(Matrices.col(rawData.getValidationData()[val], COL_LABEL_IN_LABELS)),
//					new DefaultVector(Matrices.col(predictedLabels, COL_LABEL_IN_SAMPLE2LABEL)));

			

			final double overshootPercentage = 0;

			accuracies[data] = currentAcc;
			sampleDifferences[data] = currentSampleIdxDiff;
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

		float avgAccuracy = (float) (accuracySum/(float)accuracies.length);
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
	
	public double computeSigmoid(float x) {
		return Calculations.computeSigmoid(x);
//		float exp = (float) Math.exp(-x);
//		float ret = 1/(1+exp);
//		return ret;
	}
	
	
	public float[] computeSigmoids(float[] x) {
		float[] ret = new float[x.length];
		for (int i = 0; i < ret.length ; i++) {
			ret[i] = (float)computeSigmoid(x[i]);
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
