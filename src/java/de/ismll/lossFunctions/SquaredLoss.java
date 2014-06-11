package de.ismll.lossFunctions;

import gnu.trove.map.hash.TIntFloatHashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.modelFunctions.ModelFunctions;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;

public class SquaredLoss extends LossFunction{

	private Logger logger = LogManager.getLogger(getClass());

	@Override
	public void iterate(ModelFunctions function, Vector instance, float label) {

		// Multiplier for squared loss is 2times the error:

		float predicted = function.evaluate(instance);

		float multiplier = 2*(predicted - label); // predicted - label und nicht umgekehrt da sonst die ableitung der model function noch ein minus zeichen braucht...

		function.SGD(instance, multiplier, this.learnRate);

	}
	
	@Override
	public void iterate(ModelFunctions function, Matrix data, float[] labels) {

		for (int row = 0; row < data.getNumRows(); row++) {
			Vector instance = Matrices.row(data, row);
			float label = labels[row];
			
			iterate(function, instance, label);
		}

	}

	@Override
	public void iterate(ModelFunctions function, TIntFloatHashMap instance, float label) {

		// Multiplier for squared loss is 2times the error:

		float predicted = function.evaluate(instance);

		float multiplier = 2*(predicted - label); // predicted - label und nicht umgekehrt da sonst die ableitung der model function noch ein minus zeichen braucht...

		function.SGD(instance, multiplier, this.learnRate);

	}
	
	@Override
	public void iterate(ModelFunctions function, TIntFloatHashMap[] data, float[] labels) {

		for (int row = 0; row < data.length; row++) {
			TIntFloatHashMap instance = data[row];
			float label = labels[row];
			
			iterate(function, instance, label);
		}

	}




}
