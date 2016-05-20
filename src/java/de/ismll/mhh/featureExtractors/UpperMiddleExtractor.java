package de.ismll.mhh.featureExtractors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.table.Matrix;
import de.ismll.table.Vector;

public class UpperMiddleExtractor implements FeatureExtractor {
	
	protected Logger log = LogManager.getLogger(getClass());

	@Override
	public Vector extractFeatures(Matrix pressureData, int lowerSensor,
			int upperSensor) {
		
		
		Vector extractedFeatures;
		
		// Berechne Anzahl der Sensoren
		int length = (upperSensor - lowerSensor) + 1; 
		
		int middle;
		
		
		//Berechne zweite Haelfte
		if (length % 4 == 0) { middle = lowerSensor + (int) Math.floor(length * 0.5) - 2;}
		else {middle = (lowerSensor - 1) + (int) Math.floor(length * 0.5);}
		
		
		int intervalLength = upperSensor - middle ;
		
		int[] usedIndexes = new int[intervalLength];
				
		String usedIndexesString = ""; 
		
		for (int i = 0; i < intervalLength ; i++) {
			usedIndexes[i] =  middle + 1 + i;
			usedIndexesString = usedIndexesString + "" + usedIndexes[i] + " ";
		}
		
		log.info("Will use sensors " + usedIndexesString + " for upper-middle feature extraction.");
		
		// Columns holen
		MatrixOperations mo = new MatrixOperations();
		
		extractedFeatures =  mo.averageOverColumns(pressureData, usedIndexes);
		
		return extractedFeatures;
	}

}
