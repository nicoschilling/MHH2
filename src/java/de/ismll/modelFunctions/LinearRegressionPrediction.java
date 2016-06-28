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
	private float smoothReg;

	private float bias;
	private float bias_copy;
	
	
	@Override
	public void saveBestParameters(Quality quality, String forWhat) {
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
	}


	public void initialize(int nrAttributes, float stDev, float reg0, float smoothReg) {
		Random random = new Random();
//		random.setSeed(100);
		bias = (float) (random.nextGaussian()*stDev);
		this.parameters = new float[nrAttributes];
		for (int i = 0; i < parameters.length ; i++) {
			parameters[i] = (float) (random.nextGaussian()*stDev);
		}

		this.reg0 = reg0;
		this.smoothReg = smoothReg;
	}

	@Override
	public void initialize(AlgorithmController algcon) {

		int nrAttributes = algcon.getNrAttributes();
		float stDev = algcon.getStDev();
		float reg0 = algcon.getReg0();
		float smoothReg = algcon.getSmoothReg();

		initialize(nrAttributes, stDev, reg0, smoothReg);
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
			ret += this.parameters[i]*instance.get(i);
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
	public void GD(Matrix data, float[] multipliers, float learnRate) {
		float multiplierSum=0;
		for (int i = 0; i < multipliers.length ; i++) {
			multiplierSum += multipliers[i];
		}
		// Update of Bias...
		float updatedBias = this.bias - learnRate*multiplierSum;
		this.bias = updatedBias;
		
		float[] grad;
		grad = new float[this.parameters.length];
		for (int dim = 0; dim < parameters.length ; dim++) {
			for (int instance = 0; instance < multipliers.length ; instance++) {
				grad[dim] += multipliers[instance]*data.get(instance,dim);
			}
		}
		
		for (int dim = 0; dim < grad.length ; dim++) {
			float updated = this.parameters[dim] - learnRate*grad[dim] - this.reg0*this.parameters[dim];	
			this.parameters[dim] = updated;
		}
		
	}
	
	@Override
	public void LAPGD(Matrix data, float[] multipliersFit, float[][] multipliersSmooth, float learnRate,
			int[] randomIndices, float[] instanceMultipliersSmooth, float[][] sigmoidDifferences) {
		
		float multiplierFitSum=0;
		for (int i = 0; i < multipliersFit.length ; i++) {
			multiplierFitSum += multipliersFit[i];
		}
		// Update of Bias...
		float updatedBias = this.bias - learnRate*multiplierFitSum;
		this.bias = updatedBias;
		
		float[] gradFit;
		gradFit = new float[this.parameters.length];
		for (int dim = 0; dim < parameters.length ; dim++) {
			for (int instance = 0; instance < multipliersFit.length ; instance++) {
				int index = randomIndices[instance];
				gradFit[dim] += multipliersFit[instance]*data.get(index,dim);
			}
		}
		
		
		float[] gradSmooth;
		gradSmooth = new float[this.parameters.length];
		
		for (int dim = 0; dim < parameters.length ; dim++) {
			for (int instance = 0; instance < multipliersFit.length ; instance++) {
						
				int instanceIdx = randomIndices[instance];
				int smoothWindow = multipliersSmooth[0].length/2;
				
				if( instanceIdx < smoothWindow || instanceIdx >= (data.getNumRows() - smoothWindow)) {
//					log.info("laplacian cant be computed!");
					break;
				}
				
				for (int surroundInstance = 0; surroundInstance < multipliersSmooth[0].length ; surroundInstance++) {
					int surroundIndex;
					if (surroundInstance < smoothWindow) {
						surroundIndex = instanceIdx-smoothWindow+surroundInstance;
					}
					else {
						surroundIndex = instanceIdx-smoothWindow+surroundInstance+1;
					}
					gradSmooth[dim] += ( instanceMultipliersSmooth[instance]*sigmoidDifferences[instance][surroundInstance]
							*data.get(instanceIdx, dim) - multipliersSmooth[instance][surroundInstance]*sigmoidDifferences[instance][surroundInstance]
									*data.get(surroundIndex, dim) );
								
				}
			}
		}
		
		
		
		for (int dim = 0; dim < parameters.length ; dim++) {
			float updated = parameters[dim] - learnRate*( (1-this.smoothReg)*gradFit[dim] + this.smoothReg*gradSmooth[dim] ) 
					- this.reg0*this.parameters[dim];
			if (Float.isNaN(updated)) {
//				System.out.println("update is NAN!!!!");
			}
			this.parameters[dim] = updated;
		}
		
//		this.GD(data, multipliersFit, learnRate);

		
		
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


	public float getSmoothReg() {
		return smoothReg;
	}


	public void setSmoothReg(float smoothReg) {
		this.smoothReg = smoothReg;
	}



}
