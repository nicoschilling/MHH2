package de.ismll.mhh.featureExtractors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.projections.ColumnSubsetMatrixView;



public class LowerExtractor implements SphincterFeatureExtractor{
	
	protected Logger log = LogManager.getLogger(getClass());

	@Override
	public Vector extractFeatures(Matrix pressureData, int lowerSensor,
			int upperSensor) {
		
		Vector extractedFeatures;
		
		int length = (upperSensor - lowerSensor) + 1; 
		
		int intervalLength = (int) Math.ceil(length * 0.333);
		
		int[] usedIndexes = new int[intervalLength];
				
		String usedIndexesString = ""; 
		
		for (int i = 0; i < intervalLength ; i++) {
			usedIndexes[i] = lowerSensor + i;
			usedIndexesString = usedIndexesString + "" + usedIndexes[i] + " ";
		}
		
		log.info("Will use sensors " + usedIndexesString + " for lower feature extraction.");
		
		// Columns holen
		MatrixOperations mo = new MatrixOperations();
		
		extractedFeatures =  mo.averageOverColumns(pressureData, usedIndexes);
		
		return extractedFeatures;
		
	}

}
