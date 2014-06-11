package de.ismll.mhh.featureExtractors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.table.Matrix;
import de.ismll.table.Vector;

public class UpperExtractor implements SphincterFeatureExtractor{

	protected Logger log = LogManager.getLogger(getClass());
	
	@Override
	public Vector extractFeatures(Matrix pressureData, int lowerSensor,
			int upperSensor) {
		
		Vector extractedFeatures;
		
		// Berechne Anzahl der Sensoren
		int length = (upperSensor - lowerSensor) + 1; 
		
		int secondThird;
		
		//Berechne zweites Drittel und interVallänge des oberen Drittels
		if (length % 3 == 0) { secondThird = lowerSensor + (int) Math.ceil(length * 0.666);}
		else {secondThird = lowerSensor + (int) Math.ceil(length * 0.666) -1 ;}
		
		int intervalLength = upperSensor - secondThird + 1;
		
//		System.out.println("Second Third is: " + secondThird);
//		System.out.println("Interval Length: " + intervalLength);
		
		int[] usedIndexes = new int[intervalLength];
				
		String usedIndexesString = ""; 
		
		for (int i = 0; i < intervalLength ; i++) {
			usedIndexes[i] =  secondThird + i;
			usedIndexesString = usedIndexesString + "" + usedIndexes[i] + " ";
		}
		
		log.info("Will use sensors " + usedIndexesString + " for upper feature extraction.");
		
		// Columns holen
		MatrixOperations mo = new MatrixOperations();
		
		extractedFeatures =  mo.averageOverColumns(pressureData, usedIndexes);
		
		return extractedFeatures;
		
	}

}
