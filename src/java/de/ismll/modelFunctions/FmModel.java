package de.ismll.modelFunctions;

import java.util.Random;
import java.util.StringTokenizer;

import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntFloatHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import de.ismll.myfm.core.FmDataset;
import de.ismll.myfm.util.Printers;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.impl.DefaultVector;
import de.ismll.table.projections.RowUnionMatrixView;
import de.ismll.table.projections.VectorAsMatrixView;

public class FmModel extends ModelFunctions {

	public float bias;
	public Vector w;
	public Matrix v;

	public Matrix vAdd;
	public Vector wAdd;

	private int numAttributes;
	private int numFactor;

	private float reg0;
	private float regW;
	private float regV;

	private boolean useW0;
	private boolean useW;

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
	
	
	public void initialize(int nrAttributes, float stDev, int nrFactors, float reg0, float regV, float regW ) {
		
		// Initialize Parameters
		
		this.w = new DefaultVector(nrAttributes);
		this.v = new DefaultMatrix(nrAttributes, nrFactors);

		Random random = new Random();
		setBias((float) (random.nextGaussian()*stDev));
		
		this.w = new DefaultVector(getNumAttributes());
		for (int i = 0; i < w.size() ; i++) {
			this.w.set(i, (float) ( random.nextGaussian()*init_stdev) );
		}
		v = new DefaultMatrix(getNumAttributes(), getNumFactor());
		for (int i = 0; i < v.getNumRows() ; i++) {
			for (int j = 0; j < v.getNumColumns() ; j++) {
				this.v.set(i, j, (float) ( random.nextGaussian()*init_stdev));
			}
		}
		
		this.reg0 = reg0;
		this.regV = regV;
		this.regW = regW;
	}
	
	
	@Override
	public void initialize(float[] functionParameters) {
		if(functionParameters.length != 6) {
			log.fatal("Function Parameters are not set correctly for initialization!");
		}
		
		int nrAttributes = (int) functionParameters[0];
		float stDev = functionParameters[1];
		int nrFactors = (int) functionParameters[2];
		float reg0 = functionParameters[3];
		float regV = functionParameters[4];
		float regW = functionParameters[5];
		
		initialize(nrAttributes, stDev, nrFactors, reg0, regV, regW);
	}


	public void debug() {
		System.out.println("Number of Attributes: " + getNumAttributes());
		System.out.println("use W0: " + isUseW0());
		System.out.println("use W: " + isUseW());
		System.out.println("dim v: " + getNumFactor());
		System.out.println("Reg0: " + getReg0());
		System.out.println("RegW: " + getRegW());
		System.out.println("RegV: " + getRegV());
		System.out.println("Initialize V ~ N(" + getInit_mean() + "," + getInit_stdev() +")");
	}

	public void initializeModel(int nrAttributes) {
		
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


	public void initializeDimensions(String dimString) {
		StringTokenizer dimStringTok = new StringTokenizer(dimString, ",");
		int useW0 = Integer.parseInt(dimStringTok.nextToken());
		if (useW0 == 1) { this.setUseW0(true); }
		int useW = Integer.parseInt(dimStringTok.nextToken());
		if (useW == 1) {this.setUseW(true); }
		int numFactor = Integer.parseInt(dimStringTok.nextToken());
		this.setNumFactor(numFactor);
	}

	public void initializeRegularization(String regString) {
		StringTokenizer regStringTok = new StringTokenizer(regString , ",");
		float reg0 = Float.parseFloat(regStringTok.nextToken());
		this.setReg0(reg0);
		float regW = Float.parseFloat(regStringTok.nextToken());
		this.setRegW(regW);
		float regV = Float.parseFloat(regStringTok.nextToken());
		this.setRegV(regV);
	}



	public float convertPrediction(float in) {
		if (this.task == REGRESSION_TASK) {
			in = Math.min(in, this.maxTarget);
			in = Math.max(in, this.minTarget);
		}
		else if (this.task == CLASSIFICATION_TASK) {
			in = Math.min(in, 1);
			in = Math.max(in, -1);
		}
		//		if (this.task == RANKING_TASK) {
		//			in = Math.min(in, this.maxTarget);
		//			in = Math.max(in, this.minTarget);
		//		}
		return in;
	}


	public float computeError(float predictedValue, float trueValue) {
		float ret = 0;

		if (this.loss == LEAST_SQUARES_LOSS) {
			ret = (predictedValue - trueValue)*(predictedValue - trueValue);
			//			ret = Math.abs(predictedValue - trueValue);
		}
		return ret;
	}

	public float computeBPRLoss(float predictedProb) {
		float ret = 0;

		float sigmoid = computeSigmoid(predictedProb);
		ret = (float) Math.log(sigmoid);
		//		if(Float.isInfinite(ret)) {
		//			System.out.println("infinite");
		//		}

		return ret;
	}


	public float computeOverallError(float[] predictedValues, float[] trueValues) {
		if (predictedValues.length != trueValues.length) {
			System.err.println("Cannot compute Error as predicted Values and true Values do not have the same length");
			System.exit(1);
		}
		float averageError=0;

		if (this.loss == LEAST_SQUARES_LOSS) {
			for (int i = 0; i < predictedValues.length ; i++) {
				averageError += computeError(predictedValues[i], trueValues[i]);
			}
			averageError = (float) Math.sqrt(averageError/predictedValues.length);
			//			averageError = averageError/predictedValues.length;
		}

		return averageError;
	}


	public float computeOverallBPROpt(TIntObjectHashMap<int[]> orderedPairs, FmDataset data ) {
		float ret = 0;

		int[] keys = orderedPairs.keys();

		for (int i = 0; i < keys.length ; i++) {
			int ind = keys[i];
			float predPos = this.predict(data.getValues()[ind]);

			int[] lowerIndexes = orderedPairs.get(ind);

			for (int j = 0; j < lowerIndexes.length ; j++) {
				int lowerInd = lowerIndexes[j];
				float predNeg = this.predict(data.getValues()[lowerInd]);

				float predDiff = predPos - predNeg;
				ret += computeBPRLoss(predDiff);
			}

		}

		return ret;


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

	public float predict(TIntFloatHashMap tIntFloatHashMap) {
		float result = 0;
		int[] keys = tIntFloatHashMap.keys();
		if (useW0) {
			result += getBias();
		}
		if (useW) {
			for (int ind = 0; ind < keys.length ; ind++) {
				int key = keys[ind];
				result += this.w.get(key)*tIntFloatHashMap.get(key);
			}
		}
		for (int f = 0; f < getNumFactor() ; f++) {
			float sum = 0;
			float sum_sqr = 0;
			for (int ind = 0; ind < keys.length ; ind++) {
				int i = keys[ind];
				float d = (float) (this.v.get(i, f)*tIntFloatHashMap.get(i));
				sum += d;
				sum_sqr += d*d;
			}
			result += 0.5 * (sum*sum - sum_sqr);
		}	
		//		System.out.println("Result before converting is: " + result);
		result = convertPrediction(result);	
		if (Float.isNaN(result)) {
			System.out.println("Prediction is NaN..."); 
			System.exit(1);
		}
		//		System.out.println("Result is: " + result);
		//return result;
		return result;
	}


	public float[] predict(TIntFloatHashMap[] values) {	
		float[] ret = new float[values.length];
		for (int i = 0; i < values.length ; i++) {
			ret[i] = predict(values[i]);
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

		this.setNewAttributeIds(new TIntIntHashMap());

		for (int i = 0; i < newAttributes ; i++) {
			//			int ha = v.getNumRows() + i;
			getNewAttributeIds().put(i, v.getNumRows() + i);
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


	public float computeSigmoid(float x) {
		float exp = (float) Math.exp(-x);
		float ret = (float) (1/(1+exp));
		return ret;
	}


	// 10.06.2014 new functions... to ensure more generality! Hopefully!

	@Override
	public void SGD(TIntFloatHashMap x, float multiplier, float learnRate) {
		if (this.isUseW0()) {
			float updated = this.bias - learnRate *( multiplier + this.getReg0()*this.bias);
			if (Float.isNaN(updated) || Float.isInfinite(updated)) {
				System.out.println("Update of Bias is about to be NaN...");
				System.exit(1);
			}
			this.setBias(updated);
		}
		if (this.isUseW()) {	
			int[] keys = x.keys();	
			for (int ind = 0; ind < keys.length ; ind++) {
				int i = keys[ind];	
				float updated = (this.w.get(i) - learnRate*( multiplier*x.get(i) + this.getRegW()*this.w.get(i) ));
				if (Float.isNaN(updated) || Float.isInfinite(updated)) {
					System.out.println("Update of a Regression Term is about to be NaN...");
					System.exit(1);
				}
				this.w.set(i, updated );
			}
		}
		int[] keys = x.keys();
		for (int f = 0; f < this.getNumFactor() ; f++) {
			float sum = preComputeSum(x, f);
			for (int ind = 0; ind < keys.length ; ind++) {
				int i = keys[ind];
				float grad = x.get(i)*sum - this.v.get(i, f)*x.get(i)*x.get(i);
				float updated = (this.v.get(i, f) - learnRate*(multiplier*grad + this.getRegV()*this.v.get(i, f) ));
				if (Float.isNaN(updated) || Float.isInfinite(updated)) {
					System.out.println("Update of a latent Feature is about to be NaN...");
					System.exit(1);
				}
				this.v.set(i, f, updated);
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



}
