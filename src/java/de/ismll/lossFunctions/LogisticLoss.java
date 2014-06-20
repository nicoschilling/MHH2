package de.ismll.lossFunctions;

import gnu.trove.map.hash.TIntFloatHashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.modelFunctions.ModelFunctions;
import de.ismll.secondversion.Calculations;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;


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
