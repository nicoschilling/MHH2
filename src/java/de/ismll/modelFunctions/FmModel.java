package de.ismll.modelFunctions;

import java.util.Random;
import java.util.StringTokenizer;

import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntFloatHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import de.ismll.myfm.core.FmDataset;
//import de.ismll.myfm.core.FmDataset;
//import de.ismll.myfm.util.Printers;
import de.ismll.secondversion.AlgorithmController;
import de.ismll.secondversion.IntRange;
import de.ismll.secondversion.MhhRawData;
import de.ismll.secondversion.Quality;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.impl.DefaultVector;
import de.ismll.table.projections.RowUnionMatrixView;
import de.ismll.table.projections.VectorAsMatrixView;

public class FmModel extends ModelFunctions {

	public float bias;
	private float bias_copy;

	public Vector w;
	private Vector w_copy;

	public Matrix v;
	private Matrix v_copy;

	public Matrix vAdd;
	public Vector wAdd;

	private int numAttributes;
	private int numFactor;

	private float reg0;
	private float regW;
	private float regV;

	private float smoothReg;

	private boolean useW0;
	private boolean useW;
	private boolean useV;

	private float init_stdev;
	private float init_mean;

	private int task;
	private int loss;

	public static final int REGRESSION_TASK = 1;
	public static final int CLASSIFICATION_TASK = 2;
	public static final int RANKING_TASK = 3;
	// usw...

	public static final int LEAST_SQUARES_LOSS = 1;

	public static final int BPR_LOSS = 2;



	private float maxTarget;
	private float minTarget;

	private TIntIntHashMap newAttributeIds;



	@Override
	public float evaluate(Vector instance) {
		float ret = 0;
		if (this.useW0) {
			ret = this.bias;
		}
		if (this.useW) {
			for (int ind = 0; ind < instance.size() ; ind++) {
				ret += this.w.get(ind)*instance.get(ind);
			}
		}
		if (this.useV) {
			for (int f = 0; f < this.getNumFactor() ; f++) {
				float sum = 0;
				float sum_sqr = 0;
				for (int ind = 0; ind < instance.size() ; ind++) {
					float d = this.v.get(ind, f)*instance.get(ind);
					sum += d;
					sum_sqr += d*d;
				}
				ret += 0.5 * (sum*sum - sum_sqr);
			}
		}
		return ret;
	}




	public void initialize(int nrAttributes, float stDev, int nrFactors, float reg0, float regV, float regW, float smoothReg ) {

		// Initialize Parameters

		this.numAttributes=nrAttributes;
		this.numFactor=nrFactors;

		this.w = new DefaultVector(nrAttributes);
		this.v = new DefaultMatrix(nrAttributes, nrFactors);

		Random random = new Random();
		setBias((float) (random.nextGaussian()*stDev));

		this.w = new DefaultVector(getNumAttributes());
		for (int i = 0; i < w.size() ; i++) {
			this.w.set(i, (float) ( random.nextGaussian()*stDev) );
		}
		v = new DefaultMatrix(getNumAttributes(), getNumFactor());
		for (int i = 0; i < v.getNumRows() ; i++) {
			for (int j = 0; j < v.getNumColumns() ; j++) {
				this.v.set(i, j, (float) ( random.nextGaussian()*stDev));
			}
		}


		this.reg0 = reg0;
		this.regV = regV;
		this.regW = regW;

		this.smoothReg = smoothReg;

		this.useV=true;
		this.useW=true;
		this.useW0=true;
	}


	@Override
	public void initialize(AlgorithmController algcon) {
		
		System.out.println("initializing from algorithm controller...");

		int nrAttributes = algcon.getNrAttributes();

		float stDev = algcon.getStDev();
		int nrFactors = algcon.getFm_numFactors();
		float reg0 = algcon.getReg0();
		float regV = algcon.getFm_regV();
		float regW = algcon.getFm_regW();
		float smoothReg = algcon.getSmoothReg();

		initialize(nrAttributes, stDev, nrFactors, reg0, regV, regW, smoothReg);
	}

	public void printLatentFeatures() {
		System.out.println("There are in total " + this.getNumFactor() + " latent features.");
		for (int attribute = 0; attribute < this.getNumAttributes() ; attribute++) {
			System.out.println("Attribute: " + attribute);
			for(int feature= 0; feature < this.getNumFactor() ; feature++) {
				System.out.println("Feature: " + feature);
				System.out.println(this.v.get(attribute,feature));
			}
		}
	}

	public static float computeScalarProduct(Vector x, Vector y) {
		float ret = 0;
		if (x.size() != y.size()) { 
			System.out.println("Vectors have different size, scalar product cannot be computed!");
			throw new RuntimeException();
		}
		for (int i = 0; i < x.size() ; i++) {
			ret += x.get(i)*y.get(i);
		}
		return ret;
	}



	public float evaluate(TIntFloatHashMap instance) {
		float result = 0;
		int[] keys = instance.keys();
		if (this.useW0) {
			result += getBias();
		}
		if (this.useW) {
			for (int ind = 0; ind < keys.length ; ind++) {
				int key = keys[ind];
				result += this.w.get(key)*instance.get(key);
			}
		}
		if (this.useV) {
			for (int f = 0; f < getNumFactor() ; f++) {
				float sum = 0;
				float sum_sqr = 0;
				for (int ind = 0; ind < keys.length ; ind++) {
					int i = keys[ind];
					float d = this.v.get(i, f)*instance.get(i);
					sum += d;
					sum_sqr += d*d;
				}
				result += 0.5 * (sum*sum - sum_sqr);
			}	
		}
		if (Float.isNaN(result)) {
			System.out.println("Prediction is NaN..."); 
			System.exit(1);
		}
		return result;
	}


	public float[] predict(TIntFloatHashMap[] values) {	
		float[] ret = new float[values.length];
		for (int i = 0; i < values.length ; i++) {
			ret[i] = evaluate(values[i]);
		}	
		return ret;
	}






	public void initFoldIn(int newAttributes) {
		Random random = new Random();
		this.vAdd = new DefaultMatrix(newAttributes, this.numFactor);
		this.wAdd = new DefaultVector(newAttributes);
		for (int i = 0; i < vAdd.getNumRows() ; i++) {
			for (int j = 0; j < vAdd.getNumColumns() ; j++) {
				this.vAdd.set(i, j, (float) (init_mean + random.nextGaussian()*init_stdev));
			}
		}
		for (int i = 0; i < wAdd.size() ; i++) {
			wAdd.set(i, (float) (init_mean + random.nextGaussian()*init_stdev) );
		}
	}



	public void performFoldIn(FmDataset foldData, FmDataset trainData) {
		// Compare fold in with trainData

		System.out.println("Current model has: " + v.getNumRows() + " attributes.");

		System.out.println("Performing a fold in on additional " + foldData.getNumRows() + " instances.");
		System.out.println("Fold In Data has " + foldData.getNumColumns() +  " attributes");

		int newAttributes = foldData.getNumColumns() - trainData.getNumColumns();

		newAttributeIds=new TIntIntHashMap();

		for (int i = 0; i < newAttributes ; i++) {
			//			int ha = v.getNumRows() + i;
			newAttributeIds.put(i, v.getNumRows() + i);
			//			System.out.println("Will put new attribute: " + ha );
		}

		initFoldIn(newAttributes);

		Matrix vComb = new RowUnionMatrixView(new Matrix[] {this.v , this.vAdd});
		Matrix wComb = new RowUnionMatrixView(new Matrix[] {new VectorAsMatrixView(this.w) , new VectorAsMatrixView(this.wAdd)});

		Matrix vNew = new DefaultMatrix(vComb);
		Vector wNew = new DefaultVector(wComb.getNumRows());

		for (int i = 0 ; i < wComb.getNumRows() ; i++) {
			wNew.set(i, wComb.get(i, 0));
		}


		setW(wNew);
		setV(vNew);

		System.out.println("New Model has now: " + v.getNumRows() + " attributes.");
		//		System.out.println("Will learn model parameters for fold-in attributes now.");

	}





	// 10.06.2014 new functions... to ensure more generality! Hopefully!

	@Override
	public void SGD(TIntFloatHashMap x, float multiplier, float learnRate) {
		float lr_times_multi = learnRate *multiplier;
		int[] keys = x.keys();	

		if (this.isUseW0()) {
			float updated = this.bias - lr_times_multi;
			if (Float.isNaN(updated) || Float.isInfinite(updated)) {
				System.out.println("Update of Bias is about to be NaN...");
				System.exit(1);
			}
			this.setBias(updated);
		}
		if (this.useW) {	
			for (int ind = 0; ind < keys.length ; ind++) {
				int i = keys[ind];	
				float w_i = w.get(i);
				float updated = (w_i - lr_times_multi * x.get(i) + regW*w_i );
				if (Float.isNaN(updated) || Float.isInfinite(updated)) {
					System.out.println("Update of a Regression Term is about to be NaN...");
					System.exit(1);
				}
				this.w.set(i, updated );
			}
		}
		if (this.useV) {
			for (int f = 0; f < this.getNumFactor() ; f++) {
				float sum = preComputeSum(x, f);
				for (int ind = 0; ind < keys.length ; ind++) {
					int i = keys[ind];
					float grad = x.get(i)*sum - this.v.get(i, f)*x.get(i)*x.get(i);
					float updated = (this.v.get(i, f) - lr_times_multi * grad + regV*this.v.get(i, f) );
					if (Float.isNaN(updated) || Float.isInfinite(updated)) {
						System.out.println("Update of a latent Feature is about to be NaN...");
						System.exit(1);
					}
					this.v.set(i, f, updated);
				}
			}
		}	
	}

	@Override
	public void GD(Matrix data, float[] multipliers, float learnRate) {
		float multiplierSum=0;
		for (int i = 0; i < multipliers.length ; i++) {
			multiplierSum += multipliers[i];
		}
		// Update of Bias...
		if (this.useW0) {
			float updatedBias = this.bias - learnRate*multiplierSum;
			this.bias = updatedBias;
		}
		if (this.isUseW()) {
			float[] grad = new float[this.w.size()];
			for (int dim = 0; dim < grad.length ; dim++) {
				for (int instance = 0; instance < multipliers.length ; instance++) {
					grad[dim] += multipliers[instance]*data.get(instance, dim);
				}
				float updated = (this.w.get(dim) - learnRate*grad[dim] - regW*this.w.get(dim));
				this.w.set(dim, updated);
			}
		}
		if (this.useV) {
			for (int f = 0; f < numFactor ; f++) {
				float[] sums = preComputeSums(data, f);
				for (int dim=0; dim < numAttributes ; dim++) {
					float gradient = 0;
					for (int instance = 0; instance < multipliers.length ; instance++) {
						float data_i_d = data.get(instance, dim);
						gradient += multipliers[instance]*( data_i_d*sums[instance] 
								- this.v.get(dim, f)*data_i_d*data_i_d );
					}
					float updated = this.v.get(dim, f) - learnRate*gradient - regV*this.v.get(dim, f);
					this.v.set(dim, f, updated);
				}
			}
		}
		
//		System.out.println("maximum of V:" + Matrices.max(this.v));
//		System.out.println("minimum of V:" + Matrices.min(this.v));
	}

	@Override
	public void LAPGD(Matrix data, float[] multipliersFit, float[][] multipliersSmooth, float learnRate,
			int[] randomIndices, float[] instanceMultipliersSmooth, float[][] sigmoidDifferences) {

		if (this.useW0) {
			float multiplierFitSum=0;
			for (int i = 0; i < multipliersFit.length ; i++) {
				multiplierFitSum += multipliersFit[i];
			}
			// Update of Bias...
			float updatedBias = this.bias - learnRate*multiplierFitSum;
			this.bias = updatedBias;
		}

		if (useW) {
			int w_size = this.w.size();
			float[] gradFitW = new float[w_size];
			for (int dim = 0; dim < w_size ; dim++) {
				for (int instance = 0; instance < multipliersFit.length ; instance++) {
					int index = randomIndices[instance];
					gradFitW[dim] += multipliersFit[instance]*data.get(index,dim);
				}
			}

			float[] gradSmoothW;
			gradSmoothW = new float[w_size];

			for (int dim = 0; dim < w_size ; dim++) {
				for (int instance = 0; instance < multipliersFit.length ; instance++) {

					int instanceIdx = randomIndices[instance];
					int smoothWindow = multipliersSmooth[0].length/2;

					if( instanceIdx < smoothWindow || instanceIdx >= (data.getNumRows() - smoothWindow)) {
						//						log.info("laplacian cant be computed!");
						break;
					}

					float data_instanceIdx_dim = data.get(instanceIdx, dim);

					for (int surroundInstance = 0; surroundInstance < multipliersSmooth[0].length ; surroundInstance++) {
						int surroundIndex;
						if (surroundInstance < smoothWindow) {
							surroundIndex = instanceIdx-smoothWindow+surroundInstance;
						}
						else {
							surroundIndex = instanceIdx-smoothWindow+surroundInstance+1;
						}
						gradSmoothW[dim] += ( instanceMultipliersSmooth[instance]*sigmoidDifferences[instance][surroundInstance]
								*data_instanceIdx_dim - multipliersSmooth[instance][surroundInstance]*sigmoidDifferences[instance][surroundInstance]
										*data.get(surroundIndex, dim) );

					}
				}
			}
			for (int dim = 0; dim < w_size ; dim++) {
				float w_dim = this.w.get(dim);
				float updated = w_dim - learnRate*( (1-this.smoothReg)*gradFitW[dim] + this.smoothReg*gradSmoothW[dim]) -
						regW*w_dim;
				this.w.set(dim, updated);
			}


		}

		if (useV) {
			for (int f = 0; f < numFactor ; f++) {
				// get some kind of fit gradient and some kind of smooth gradient

				float[] sums = new float[randomIndices.length];

				for (int i = 0; i < sums.length; i++) {
					int instance = randomIndices[i];
					Vector currentInstance = Vectors.row(data, instance);
					sums[i] = preComputeSum(currentInstance, f);
				}

				float[] gradientFitV;
				float[] gradientSmoothV;

				gradientFitV = new float[numAttributes];
				gradientSmoothV = new float[numAttributes];


				for (int dim=0; dim < numAttributes ; dim++) {
					float v_dim_f = this.v.get(dim, f);

					for (int instance = 0; instance < sums.length ; instance++) {
						int randomInstance = randomIndices[instance];
						float data_rndInstance_dim = data.get(randomInstance, dim);
						gradientFitV[dim] += multipliersFit[instance]*( data_rndInstance_dim*sums[instance] 
								- v_dim_f*data_rndInstance_dim*data_rndInstance_dim );
					}
				}

				for (int instance = 0; instance < multipliersFit.length ; instance++) {

					for (int dim = 0; dim < this.getNumAttributes(); dim++) {
						int instanceIdx = randomIndices[instance];
						int smoothWindow = multipliersSmooth[0].length/2;
						float v_dim_f = this.v.get(dim, f);

						if( instanceIdx < smoothWindow || instanceIdx >= (data.getNumRows() - smoothWindow)) {
							//							log.info("laplacian cant be computed!");
							break;
						}

						Vector middleInstance = Vectors.row(data, instanceIdx);
						float middleInstanceSum = preComputeSum(middleInstance, f);

						float middleInstance_dim = middleInstance.get(dim);
						float middleInstanceDerivative = middleInstance_dim*middleInstanceSum -
								v_dim_f*middleInstance_dim*middleInstance_dim;

						for (int surroundInstance = 0; surroundInstance < multipliersSmooth[0].length ; surroundInstance++) {
							int surroundIndex;
							if (surroundInstance < smoothWindow) {
								surroundIndex = instanceIdx-smoothWindow+surroundInstance;
							}
							else {
								surroundIndex = instanceIdx-smoothWindow+surroundInstance+1;
							}

							Vector surroundInstanceVector = Vectors.row(data, surroundIndex);
							float surroundInstanceSum = preComputeSum(surroundInstanceVector, f);
							float surroundInstanceDerivative = surroundInstanceVector.get(dim)*surroundInstanceSum
									- v_dim_f*middleInstance_dim*middleInstance_dim;


							gradientSmoothV[dim] += ( instanceMultipliersSmooth[instance]*sigmoidDifferences[instance][surroundInstance]
									*middleInstanceDerivative - multipliersSmooth[instance][surroundInstance]*sigmoidDifferences[instance][surroundInstance]
											*surroundInstanceDerivative );

						}

					}

				}

				for (int dim = 0; dim < numAttributes ; dim++) {
					float updated = this.v.get(dim, f) - learnRate*( (1-smoothReg)*gradientFitV[dim] + smoothReg*gradientSmoothV[dim]) -
							this.getRegV()*this.v.get(dim, f);
					this.v.set(dim, f, updated);
				}


			}
		}

	}


	@Override
	public void SGD(Vector x, float multiplier, float learnRate) {
		if (useW0) {
			float updated = this.bias - learnRate * multiplier;
			if (Float.isNaN(updated) || Float.isInfinite(updated)) {
				System.out.println("Update of Bias is about to be NaN...");
				System.exit(1);
			}
			this.setBias(updated);
		}
		int x_size = x.size();
		if (useW) {	
			for (int ind = 0; ind < x_size; ind++) {	
				float updated = (this.w.get(ind) - learnRate*multiplier*x.get(ind) + this.getRegW()*this.w.get(ind) );
				if (Float.isNaN(updated) || Float.isInfinite(updated)) {
					System.out.println("Update of a Regression Term is about to be NaN...");
					System.exit(1);
				}
				this.w.set(ind, updated );
			}
		}

		for (int f = 0; f < this.getNumFactor() ; f++) {
			float sum = preComputeSum(x, f);
			for (int ind = 0; ind < x_size ; ind++) {
				float x_ind = x.get(ind);
				float v_ind_f = this.v.get(ind, f);
				float grad = x_ind*sum - v_ind_f*x_ind*x_ind;
				float updated = (v_ind_f - learnRate*multiplier*grad + regV*v_ind_f );
				if (Float.isNaN(updated) || Float.isInfinite(updated)) {
					System.out.println("Update of a latent Feature is about to be NaN...");
					System.exit(1);
				}
				this.v.set(ind, f, updated);
			}
		}	
	}

	public float preComputeSum(TIntFloatHashMap x, int f) {
		float sum = 0;
		int[] keys = x.keys();
		for (int ind = 0; ind < keys.length ; ind++) {
			int j = keys[ind];
			sum += this.v.get(j, f)*x.get(j);		
		}
		return sum;
	}

	public float preComputeSum(Vector x, int f) {
		float sum = 0;
		int x_size = x.size();
		for (int dim = 0; dim < x_size ; dim++) {
			sum += this.v.get(dim, f)*x.get(dim);		
		}
		return sum;
	}

	public float[] preComputeSums(Matrix data, int f) {
		float[] sums = new float[data.getNumRows()];

		for (int i = 0; i < sums.length	; i++) {
			sums[i] = preComputeSum(Vectors.row(data, i), f);
		}
		return sums;
	}



	@Override
	public void saveBestParameters(Quality quality, String forWhat) {

		if(forWhat=="accuracy") {
			if (quality.getAccuracy() > this.getBestAccuracy()) {
				this.setBestAccuracy(quality.getAccuracy());
				this.setV_copy(this.getV());;
				this.setW_copy(this.getW());
				this.setBias_copy(bias);
			}
		}
		else if (forWhat == "sampleDifference") {
			if (quality.getSampleDifference() > this.getBestSampleDiff()) {
				this.setBestSampleDiff(quality.getSampleDifference());;
				this.setV_copy(this.getV());;
				this.setW_copy(this.getW());
				this.setBias_copy(bias);
			}
		}
	}

	public float getBias() {
		return bias;
	}
	public void setBias(float w0) {
		this.bias = w0;
	}
	public Vector getW() {
		return w;
	}
	public void setW(Vector w) {
		this.w = w;
	}
	public Matrix getV() {
		return v;
	}
	public void setV(Matrix v) {
		this.v = v;
	}
	public int getNumAttributes() {
		return numAttributes;
	}
	public void setNumAttributes(int numAttributes) {
		this.numAttributes = numAttributes;
	}
	public int getNumFactor() {
		return numFactor;
	}
	public void setNumFactor(int numFactor) {
		this.numFactor = numFactor;
	}
	public float getReg0() {
		return reg0;
	}
	public void setReg0(float reg0) {
		this.reg0 = reg0;
	}
	public float getRegW() {
		return regW;
	}
	public void setRegW(float regW) {
		this.regW = regW;
	}
	public float getRegV() {
		return regV;
	}
	public void setRegV(float regV) {
		this.regV = regV;
	}
	public float getInit_stdev() {
		return init_stdev;
	}
	public void setInit_stdev(float init_stdev) {
		this.init_stdev = init_stdev;
	}
	public float getInit_mean() {
		return init_mean;
	}
	public void setInit_mean(float init_mean) {
		this.init_mean = init_mean;
	}


	public boolean isUseW0() {
		return useW0;
	}

	public void setUseW0(boolean useReg0) {
		this.useW0 = useReg0;
	}


	public boolean isUseW() {
		return useW;
	}


	public void setUseW(boolean useRegW) {
		this.useW = useRegW;
	}

	public int getTask() {
		return task;
	}
	public void setTask(int task) {
		this.task = task;
	}
	public int getLoss() {
		return loss;
	}
	public void setLoss(int loss) {
		this.loss = loss;
	}

	public float getMaxTarget() {
		return maxTarget;
	}

	public void setMaxTarget(float maxTarget) {
		this.maxTarget = maxTarget;
	}

	public float getMinTarget() {
		return minTarget;
	}

	public void setMinTarget(float minTarget) {
		this.minTarget = minTarget;
	}

	public Vector getwAdd() {
		return wAdd;
	}

	public void setwAdd(Vector wAdd) {
		this.wAdd = wAdd;
	}

	public Matrix getvAdd() {
		return vAdd;
	}

	public void setvAdd(Matrix vAdd) {
		this.vAdd = vAdd;
	}

	public TIntIntHashMap getNewAttributeIds() {
		return newAttributeIds;
	}

	public void setNewAttributeIds(TIntIntHashMap newAttributeIds) {
		this.newAttributeIds = newAttributeIds;
	}


	public Vector getW_copy() {
		return w_copy;
	}


	public void setW_copy(Vector w_copy) {
		this.w_copy = w_copy;
	}


	public Matrix getV_copy() {
		return v_copy;
	}


	public void setV_copy(Matrix v_copy) {
		this.v_copy = v_copy;
	}


	public float getBias_copy() {
		return bias_copy;
	}


	public void setBias_copy(float bias_copy) {
		this.bias_copy = bias_copy;
	}




	public boolean isUseV() {
		return useV;
	}




	public void setUseV(boolean useV) {
		this.useV = useV;
	}




	public float getSmoothReg() {
		return smoothReg;
	}




	public void setSmoothReg(float smoothReg) {
		this.smoothReg = smoothReg;
	}



}
