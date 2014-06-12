package de.ismll.modelFunctions;

import static de.ismll.secondversion.DatasetFormat.COL_LABEL_IN_LABELS;
import static de.ismll.secondversion.DatasetFormat.COL_LABEL_IN_SAMPLE2LABEL;

import java.util.Random;
import java.util.StringTokenizer;

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


/**
 * This is a class resembling the function theta^T times x_i
 * @author nico
 *
 */
public class LinearRegressionPrediction extends ModelFunctions {

	private float[] parameters;
	private float[] parameters_copy;

	private float reg0;

	private float bias;
	private float bias_copy;
	
	
	@Override
	public Quality saveBestParameters(MhhRawData rawData, int windowExtent, String forWhat, IntRange columnSelector) {
		
		// execute predicton Validation
		Quality quality = this.evaluateModelFunctionOnValidation(rawData, windowExtent,columnSelector);
				
		// check if better and save
		
		if(forWhat=="accuracy") {
			if (quality.getAccuracy() > this.getBestAccuracy()) {
				this.setBestAccuracy(quality.getAccuracy());
				this.setParameters_copy(parameters);
				this.setBias_copy(bias);
			}
		}
		else if (forWhat == "sampleDifference") {
			if (quality.getSampleDifference() > this.getBestSampleDiff()) {
				this.setBestSampleDiff(quality.getSampleDifference());;
				this.setParameters_copy(parameters);
				this.setBias_copy(bias);
			}
		}
		
		return quality;
	}


	public void initialize(int nrAttributes, float stDev, float reg0) {
		Random random = new Random();
		bias = (float) (random.nextGaussian()*stDev);
		this.parameters = new float[nrAttributes];
		for (int i = 0; i < parameters.length ; i++) {
			parameters[i] = (float) (random.nextGaussian()*stDev);
		}

		this.reg0 = reg0;
	}

	@Override
	public void initialize(AlgorithmController algcon) {

		int nrAttributes = algcon.getNrAttributes();
		float stDev = algcon.getStDev();
		float reg0 = algcon.getReg0();

		initialize(nrAttributes, stDev, reg0);
	}



	private Logger logger = LogManager.getLogger(getClass());

	@Override
	public float evaluate(Vector instance) {

		if (instance.size() != this.parameters.length) {
			logger.error("Parameters and Instance Vector do not have the same length! Length of instance vector = " + instance.size()
					+ " and length of parameters = " + this.parameters.length);
		}
		float ret = this.getBias();
		for (int i = 0 ; i < instance.size() ; i++) {
			ret += parameters[i]*instance.get(i);
		}
		return ret;
	}

	@Override
	public float[] evaluate(Matrix data) {
		float[] ret = new float[data.getNumRows()];

		for (int i = 0; i < ret.length ; i++) {
			ret[i] = evaluate(Matrices.row(data, i));
		}
		return ret;
	}

	@Override
	public void SGD(Vector instance, float multiplier , float learnRate) {

		// Update of Bias...
		float updatedBias = this.bias - learnRate*multiplier;
		this.bias = updatedBias;


		// Update of Parameters

		for (int i = 0; i < instance.size() ; i++)  {
			float update = this.parameters[i] - learnRate*multiplier*instance.get(i) - this.reg0*this.parameters[i];
			this.parameters[i] = update;
		}
	}


	@Override
	public float[] predictAsClassification(float[] predict) {
		float[] ret = new float[predict.length];
		for (int i = 0; i < predict.length ; i++) {
			if (predict[i] >= 0) { ret[i] = 1; }
			else { ret[i] = -1; }
		}
		return ret;
	}


	public float[] evaluate(Matrix data, Vector parameters) {
		int numRows = data.getNumRows();
		float[] ret = new float[numRows];
		int numColumns = data.getNumColumns();
		for (int i = 0; i < numRows ; i++) {
			float ret2 = 0;
			for (int i2 = 0 ; i2 < numColumns ; i2++) {
				ret2 += parameters.get(i2)*data.get(i,i2);
			}
			ret[i] = ret2;
		}
		return ret;
	}

	public float[] getParameters() {
		return parameters;
	}

	public void setParameters(float[] parameters) {
		this.parameters = parameters;
	}

	public float getReg0() {
		return reg0;
	}

	public void setReg0(float reg0) {
		this.reg0 = reg0;
	}



	public float getBias() {
		return bias;
	}

	public void setBias(float bias) {
		this.bias = bias;
	}


	public float[] getParameters_copy() {
		return parameters_copy;
	}


	public void setParameters_copy(float[] parameters_copy) {
		this.parameters_copy = parameters_copy;
	}


	public float getBias_copy() {
		return bias_copy;
	}


	public void setBias_copy(float bias_copy) {
		this.bias_copy = bias_copy;
	}



}
