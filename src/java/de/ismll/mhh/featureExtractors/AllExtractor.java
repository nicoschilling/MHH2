package de.ismll.mhh.featureExtractors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.table.Matrix;
import de.ismll.table.Vector;

public class AllExtractor implements SphincterFeatureExtractor{
	
	protected Logger log = LogManager.getLogger(getClass());

	@Override
	public Vector extractFeatures(Matrix pressureData, int lowerSensor,
			int upperSensor) {
		
		Vector extractedFeatures;
		
		int length = (upperSensor - lowerSensor) + 1;
		
		int[] usedIndexes = new int[length];
		
		String usedIndexesString = ""; 
		
		for (int i = 0; i < length ; i++) {
			usedIndexes[i] = lowerSensor + i;
			usedIndexesString = usedIndexesString + "" + usedIndexes[i] + " ";
		}
		
		log.info("Will use sensors " + usedIndexesString + " for extraction over all features.");
		
		// Columns holen
		MatrixOperations mo = new MatrixOperations();
		
		extractedFeatures =  mo.averageOverColumns(pressureData, usedIndexes);
		
		return extractedFeatures;
		
		
	}

}
