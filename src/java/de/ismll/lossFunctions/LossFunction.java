package de.ismll.lossFunctions;

import java.util.Random;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import gnu.trove.map.hash.TIntFloatHashMap;
import de.ismll.modelFunctions.ModelFunctions;
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
