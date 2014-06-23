package de.ismll.mhh.featureExtractors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.table.Matrix;
import de.ismll.table.Vector;

public class MiddleExtractor implements FeatureExtractor{
	
	protected Logger log = LogManager.getLogger(getClass());

	@Override
	public Vector extractFeatures(Matrix pressureData, int lowerSensor,
			int upperSensor) {
		
		Vector extractedFeatures;
		
		int length = (upperSensor - lowerSensor) + 1;
		
		int firstThird;
		int secondThird;
		
		if( length % 3 == 0) {	firstThird = (int) Math.ceil(length * 0.333) + 1;}
		else { firstThird = (int) Math.ceil(length * 0.333); }
			
	
		secondThird = (int) Math.ceil(length * 0.666);
		
		int intervalLength = secondThird - firstThird + 1;
		
		
		int[] usedIndexes = new int[intervalLength];
				
		String usedIndexesString = ""; 
		
		for (int i = 0; i < intervalLength ; i++) {
			usedIndexes[i] = (lowerSensor - 1) + firstThird + i;
			usedIndexesString = usedIndexesString + "" + usedIndexes[i] + " ";
		}
		
		log.info("Will use sensors " + usedIndexesString + " for middle feature extraction.");
		
		
		MatrixOperations mo = new MatrixOperations();
		
		extractedFeatures =  mo.averageOverColumns(pressureData, usedIndexes);
		
		return extractedFeatures;
	}

}
