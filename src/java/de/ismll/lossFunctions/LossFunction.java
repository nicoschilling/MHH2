package de.ismll.lossFunctions;


import java.util.Random;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import gnu.trove.map.hash.TIntFloatHashMap;
import de.ismll.modelFunctions.ModelFunctions;
import de.ismll.myfm.core.FmModel;
import de.ismll.secondversion.AlgorithmController;
import de.ismll.secondversion.Calculations;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;


public abstract class LossFunction {
	
	protected Logger log = LogManager.getLogger(getClass());
	
	public static LossFunction convert(Object in) {
		String str;
		if(in instanceof String)
			str = (String) in;
		else {
			str = in.toString();}
		switch (str) {
		case "logistic": return new LogisticLoss();
		case "squared": return new SquaredLoss();
		case "lapLogistic": return new LapLogisticLoss();
		case "lapSquared": return new LapSquaredLoss();
		//TODO: think of a smarter string input that also specifies the direction type!!
		default:
			throw new RuntimeException("Loss specifier " + str + " unknown. Only values ... are allowed");
		}
	}

	
	
	//Aufgabe der LossFunctions:
	// COmpute a loss given a model and an instance or a dataset
	// Compute the multiplier to do updates on model functions
	// provide functionality do do exact these updates

	/** 
	 * Specifies the size of the step to be done in parameter space
	 */
	public float learnRate=0;

	

	/**
	 * Integer that specifies the direction that is computed, i.e. "0" stands for GD etc...
	 */
	public int directionType = 0;


//	
	
	
	
	public void iterate(ModelFunctions function, Vector instance, float label) {
		log.fatal("The given loss Function does not implement iterate!");
	}
	
	public void iterate(ModelFunctions function, Matrix data, float[] labels) {
		log.fatal("The given loss Function does not implement iterate!");
	}
	
	public void iterate(ModelFunctions function, TIntFloatHashMap instance, float label) {
		log.fatal("The given loss Function does not implement iterate!");
	}
	public void iterate(ModelFunctions function, TIntFloatHashMap[] data, float[] labels) {
		log.fatal("The given loss Function does not implement iterate!");
	}
	public void iterateLap(ModelFunctions function, Matrix data, int[] randomIndices, float[] labels, int smoothWindow) {
		log.fatal("The given loss function does not implement iterateLap");
	}
	
	
	public int[] computeRandomBatch(int nrInstances, int batchSize) {
		Random random = new Random();
//		random.setSeed(100);
		int[] ret = new int[batchSize];
		for (int i = 0; i < ret.length ; i++) {
			ret[i] = (int) (random.nextFloat()*nrInstances);
		}
		return ret;
	}
	

	
//	/** 
//	 * Function that updates the parameters (performs one update iteration), according to a given batch of instances
//	 * @param parameters
//	 * @param idxInstances
//	 */
//	public void iterate(float[] parameters, int[] idxInstances, float[][] data, float[] labels, ModelFunctions function) {
//
//		//TODO: Generalize according to direction Type!!!!
//
//		float[] gradient = new float[parameters.length];
//
//		gradient = computeGradient(idxInstances, parameters, data, labels, function);
//
//		// L2-Regularization
//
//		for (int i = 1; i < parameters.length; i++) {
//			gradient[i] += lambda * parameters[i];
//		}
//
//		// Parameter Updates
//
//		for (int i = 0; i < parameters.length; i++)
//		{
//			parameters[i] -= learnRate*gradient[i]; 
//		}
//
//	}

//	/** 
//	 * Function that updates the parameters (performs one update iteration), according to a given batch of instances and given
//	 * weights to these instances
//	 * @param parameters
//	 * @param idxInstances
//	 */
//	public void iterate(float[] parameters, int[] idxInstances, float[][] data, float[] labels,
//			float[] instanceWeights, ModelFunctions function) {
//
//		//TODO: Generalize according to direction Type!!!!
//
//		float[] gradient = new float[parameters.length];
//
//		gradient = computeGradient(idxInstances, parameters, data, labels, instanceWeights, function);
//
//		// L2-Regularization
//
//		for (int i = 1; i < parameters.length; i++) {
//			gradient[i] += lambda * parameters[i];
//		}
//
//		// Parameter Updates
//
//		for (int i = 0; i < parameters.length; i++)
//		{
//			parameters[i] -= learnRate*gradient[i]; 
//		}
//
//	}



	/** 
	 * Function that updates the parameters (performs one update iteration), according to a given batch of instances, and an additional Laplacian Reg.
	 * @param parameters
	 * @param idxInstances
	 */
//	public void iterateLaplacian(
//			float[] parameters, int[] idxInstances, 
//			float[][] data, float[] labels, 
//			ModelFunctions function, int smoothWindow,
//			float smoothReg) {
//
//		//TODO: Generalize according to direction Type!!!!
//
//
//		float[] gradient = new float[parameters.length];
//		float[] smoothGradient = new float[parameters.length];
//		float[] totalGradient = new float[parameters.length];
//
//		gradient = computeGradient(idxInstances, parameters, data, labels, function);
//		smoothGradient = computeLaplacianGradient(idxInstances, data, parameters, function, smoothWindow);
//
//		float factorGradient = 0;
//		factorGradient = (1 - smoothReg);
//		float factorLaplacian = 0;
//		factorLaplacian = smoothReg;
//
//		// TODO: Ist das korrekt????
//		for (int i = 0; i < parameters.length ; i++) {
//			totalGradient[i] = factorGradient*gradient[i] + factorLaplacian*smoothGradient[i];
//		}
//
//		//		// TODO: Ist das korrekt????
//		//				for (int i = 0; i < parameters.length ; i++) {
//		//					totalGradient[i] = gradient[i] + smoothReg*smoothGradient[i];
//		//				}
//
//		// L2-Regularization
//
//		for (int i = 1; i < parameters.length; i++) {
//			totalGradient[i] += lambda * parameters[i];
//		}
//
//		// Parameter Updates
//
//		for (int i = 0; i < parameters.length; i++)
//		{
//			parameters[i] -= learnRate*totalGradient[i]; 
//		}
//
//	}
//
//	
//
//	/**
//	 * Computes a Laplacian Gradient. So far for the difference in classification, as difference of a sigmoid of Theta^T * x_i...
//	 *  Needs to be generalized for other Laplacians
//	 * @param idxInstances
//	 * @param data
//	 * @param parameters
//	 * @param function
//	 * @param smoothWindow
//	 * @return
//	 */
//	public static float[] computeLaplacianGradient(final int[] idxInstances, final float[][] data, final float[] parameters, final ModelFunctions function, final int smoothWindow) {
//		float[] smoothGradient = new float[parameters.length];
//
//		for (int k = 0; k < smoothGradient.length ; k++) {
//			smoothGradient[k] = 0;
//		}
//
//		for (int i = 0; i < idxInstances.length ; i++) {
//
//			int index = idxInstances[i];
//
//			// COMPUTE THETA^T X_I
//			float value = function.evaluate(data[index], parameters);
//
//			// COMPUTE THE SIGMOIDS
//			double exp = Math.exp(-value);
//			double sigmoid = (1/(1 + exp));
//			double squaredSigmoid = (exp/((1 + exp)*(1 + exp)));
//
//			// GET GOIN!
//
//			int leftSide = index - smoothWindow;
//			int rightSide = index + smoothWindow;
//
//			float vectorProduct;
//
//			if (leftSide >= 0 && rightSide < data.length)  {   //DRIN
//				for (int sampleIdx = -smoothWindow ; sampleIdx <= smoothWindow ; sampleIdx++) {
//
//					int neighborInstance = index + sampleIdx;
//
//					vectorProduct = 0;
//					vectorProduct = function.evaluate(data[neighborInstance], parameters);
//
//					double expvectorProduct = Math.exp(-vectorProduct);
//					double neighborSigmoid = (1/(1 + expvectorProduct));
//					double neighborSigmoidSquared = (expvectorProduct/((1 + expvectorProduct)*(1 + expvectorProduct)));
//
//					//					for (int gradDim = 0; gradDim < smoothGradient.length ; gradDim++) {
//					for (int gradDim = smoothGradient.length-1; gradDim >= 0 ; gradDim--) {
//						double smoothGradientValue = 
//								(sigmoid - neighborSigmoid)*(data[index][gradDim] * squaredSigmoid - data[neighborInstance][gradDim]*neighborSigmoidSquared);
//						smoothGradient[gradDim] += smoothGradientValue;
//					}
//				}
//			}
//			else if ( leftSide < 0 && rightSide < data.length) {  //LINKS KURZ
//				int leftSide2 = smoothWindow + leftSide;
//				for (int sampleIdx = -leftSide2 ; sampleIdx <= smoothWindow ; sampleIdx++) {
//
//					int neighborInstance = index + sampleIdx;
//
//					vectorProduct = 0;
//					vectorProduct = function.evaluate(data[neighborInstance], parameters);
//
//					double expvectorProduct = Math.exp(-vectorProduct);
//					double neighborSigmoid = (1/(1 + expvectorProduct));
//					double neighborSigmoidSquared = (expvectorProduct/((1 + expvectorProduct)*(1 + expvectorProduct)));
//
//					//					for (int gradDim = 0; gradDim < smoothGradient.length ; gradDim++) {
//					for (int gradDim = smoothGradient.length-1; gradDim >= 0 ; gradDim--) {
//
//						double smoothGradientValue = 
//								(sigmoid - neighborSigmoid)*(data[index][gradDim] * squaredSigmoid - data[neighborInstance][gradDim]*neighborSigmoidSquared);
//						smoothGradient[gradDim] += smoothGradientValue;
//					}
//				}
//			}
//			else if (leftSide >= 0 && rightSide >= data.length) { // RECHTS KURZ
//				int rightSide2 = ((data.length -1) - index);
//				for (int sampleIdx = -smoothWindow ; sampleIdx <= rightSide2 ; sampleIdx++) {
//
//					int neighborInstance = index + sampleIdx;
//
//					vectorProduct = 0;
//					vectorProduct = function.evaluate(data[neighborInstance], parameters);
//
//					double expvectorProduct = Math.exp(-vectorProduct);
//					double neighborSigmoid = (1/(1 + expvectorProduct));
//					double neighborSigmoidSquared = (expvectorProduct/((1 + expvectorProduct)*(1 + expvectorProduct)));
//
//					//					for (int gradDim = 0; gradDim < smoothGradient.length ; gradDim++) {
//					for (int gradDim = smoothGradient.length-1; gradDim >= 0 ; gradDim--) {
//
//						double smoothGradientValue = 
//								(sigmoid - neighborSigmoid)*(data[index][gradDim] * squaredSigmoid - data[neighborInstance][gradDim]*neighborSigmoidSquared);
//						smoothGradient[gradDim] += smoothGradientValue;
//					}
//				}
//			}
//			else { System.out.println("Laplacian wrong... tooo high smoothWindow! "); System.exit(1); }
//		}
//		return smoothGradient;
//	}
//
//	
//
//	/** 
//	 * Computes the gradient of a given model function, according to a batch of Instances.
//	 * @param idxInstances
//	 * @param parameters
//	 * @param function
//	 * @return
//	 */
//	public float[] computeGradient(int[] idxInstances, float[] parameters, float[][] data, float[] labels, float[] instanceWeights,
//			ModelFunctions function) {
//		return null;
//	}


	





	public void setLearnRate(float in) {
		this.learnRate = in;
	}

	public int getDirectionType() {
		return directionType;
	}

	public void setDirectionType(int directionType) {
		this.directionType = directionType;
	}

	public float getLearnRate() {
		return learnRate;
	}

	



}
