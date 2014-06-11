package de.ismll.modelFunctions;

import gnu.trove.map.hash.TIntFloatHashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.table.Matrix;
import de.ismll.table.Vector;

public abstract class ModelFunctions {
	
	public void initialize(float[] functionParameters) {
		log.fatal("The given model function cannot initialize itself!");
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
		log.fatal("The chosen model function does not implement predictAsClassification()");
		return null;
	}
	
	
	/**
	 * Returns the prediction (target value) of a given instance according to the learned parameters
	 * 
	 * @param instance The instance where target value should be evaluated
	 * @return the target value
	 */
	public float evaluate(float[] instance, float[] parameters) {
		log.fatal("The chosen model function does not implement evaluate()");
		return 0;
	}
	
	

	public float[] evaluate(Matrix data, Vector parameters) {
		// TODO Auto-generated method stub
		return null;
	}

}
