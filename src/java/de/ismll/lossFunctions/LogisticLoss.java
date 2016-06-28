package de.ismll.lossFunctions;

import gnu.trove.map.hash.TIntFloatHashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.modelFunctions.ModelFunctions;
import de.ismll.secondversion.Calculations;
import de.ismll.table.IntVector;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultVector;
import de.ismll.table.projections.RowSubsetMatrixView;


public class LogisticLoss extends LossFunction{

	private Logger log = LogManager.getLogger(getClass());
	
	@Override
	public void iterate(ModelFunctions function, Vector instance, float label) {

		// Multiplier for logistic loss is -label*exp/(1+exp) wobei exp = Math.exp(-label*function.evaluate)
		float predicted = function.evaluate(instance);
		float exp = (float) Math.exp(-label*predicted);
		float multiplier = -label*exp/(1+exp);

		function.SGD(instance, multiplier, this.learnRate);

	}
	
	@Override
	public void iterate(ModelFunctions function, Matrix data, float[] labels) {
		
		float[] multipliers = new float[labels.length];
		
		float[] predicted = function.evaluate(data);
		
		for (int i = 0; i < labels.length ; i++) {
			float exp = (float) Math.exp(-labels[i]*predicted[i]);
			multipliers[i] = -labels[i]*exp/(1+exp);
		}
		
		function.GD(data, multipliers, this.learnRate);
		
		
	}
	
	
	
	public void iterateLap(ModelFunctions function, Matrix data, int[] randomIndices, float[] labels, int smoothWindow) {
		
		float[] multipliersFit = new float[labels.length];
		
		float[] predicted = new float[labels.length];
		
		for (int i = 0; i < labels.length ; i++) {
			predicted[i] = function.evaluate(Vectors.row(data, randomIndices[i]));
		}
		
		for (int i = 0; i < labels.length ; i++) {
			float exp = (float) Math.exp(-labels[i]*predicted[i]);
			multipliersFit[i] = -labels[i]*exp/(1+exp);
		}
		
		
		
		
		float[][] surroundingMultipliersSmooth = new float[randomIndices.length][smoothWindow*2];
		float[][] sigmoidDifferences = new float[randomIndices.length][smoothWindow*2];
		
		float[] instanceMultipliersSmooth = new float[randomIndices.length];
		
		for (int instance = 0; instance < randomIndices.length ; instance++) {
			int randomInstance = randomIndices[instance];
			
			if( randomInstance < smoothWindow || randomInstance >= (data.getNumRows() - smoothWindow)) {
//				log.info("laplacian cant be computed!");
				break;
			}
			
			// get the surrounding part of instance from the data
			int[] pointers = new int[2*smoothWindow];
			for (int i = 0; i <pointers.length; i++) {
				if (i < smoothWindow) {
					pointers[i] = randomInstance-smoothWindow+i;
				}
				else {
					pointers[i] = randomInstance-smoothWindow+i+1;
				}
				
			}
			
			float[] predictSurroundings = new float[pointers.length];
			
			for (int surroundingInstance = 0; surroundingInstance < pointers.length ; surroundingInstance++) {
				predictSurroundings[surroundingInstance] = function.evaluate(Vectors.row(data, pointers[surroundingInstance] ));
			}
			
			float predictInstance = function.evaluate(Vectors.row(data, randomInstance));
			
			
			float[] sigmoidSurroundings = function.computeSigmoids(predictSurroundings);
			
			float sigmoidInstance = (float)function.computeSigmoid(predictInstance);
			
			for (int i = 0; i < sigmoidDifferences[0].length ; i++) {
				sigmoidDifferences[instance][i] = sigmoidSurroundings[i] - sigmoidInstance;
			}
			
			for (int i = 0; i < sigmoidSurroundings.length ; i++) {
				float exp = (float) Math.exp(-predictSurroundings[i]);
				float denominator = (1+exp)*(1+exp);
				surroundingMultipliersSmooth[instance][i] = exp/denominator;	
			}
			
			
			float exp = (float) Math.exp(-predictInstance);
			float denominator = (1+exp)*(1+exp);
			
			instanceMultipliersSmooth[instance] = exp/denominator;
		}
		
		function.LAPGD(data, multipliersFit, surroundingMultipliersSmooth, this.learnRate, randomIndices,
				instanceMultipliersSmooth, sigmoidDifferences);
		
		
	}
	
	

	@Override
	public void iterate(ModelFunctions function, TIntFloatHashMap instance, float label) {

		// Multiplier for logistic loss is -label*exp/(1+exp) wobei exp = Math.exp(-label*function.evaluate)
		
		float exp = (float) Math.exp(-label*function.evaluate(instance));
		float multiplier = -label*exp/(1+exp);

		function.SGD(instance, multiplier, this.learnRate);

	}
	
	@Override
	public void iterate(ModelFunctions function, TIntFloatHashMap[] data, float[] labels) {
		
		for (int row = 0; row < data.length ; row++) {
			float label = labels[row];
			TIntFloatHashMap instance = data[row];
			
			iterate(function, instance, label);
		}
		
	}






}
