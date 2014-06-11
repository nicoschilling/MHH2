package de.ismll.mhh.featureExtractors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.table.Matrix;
import de.ismll.table.Vector;

public class LowerMiddleExtractor implements SphincterFeatureExtractor{
	
	protected Logger log = LogManager.getLogger(getClass());

	@Override
	public Vector extractFeatures(Matrix pressureData, int lowerSensor,
			int upperSensor) {
		
		Vector extractedFeatures;
		
		// Berechne Anzahl der Sensoren
		int length = (upperSensor - lowerSensor) + 1; 
		
		int middle;
		
		
		//Berechne erste Hälfte
		if (length % 4 == 0) { middle = lowerSensor + (int) Math.ceil(length * 0.5);}
		else {middle = (lowerSensor - 1) + (int) Math.ceil(length * 0.5);}
		
		
		int intervalLength = middle - lowerSensor + 1;
		
		int[] usedIndexes = new int[intervalLength];
				
		String usedIndexesString = ""; 
		
		for (int i = 0; i < intervalLength ; i++) {
			usedIndexes[i] =  lowerSensor + i;
			usedIndexesString = usedIndexesString + "" + usedIndexes[i] + " ";
		}
		
		log.info("Will use sensors " + usedIndexesString + " for lower-middle feature extraction.");
		
		// Columns holen
		MatrixOperations mo = new MatrixOperations();
		
		extractedFeatures =  mo.averageOverColumns(pressureData, usedIndexes);
		
		return extractedFeatures;
	}

}
