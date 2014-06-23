package de.ismll.mhh.featureExtractors;

import de.ismll.table.Matrix;
import de.ismll.table.Vector;

public interface FeatureExtractor {
	
	public Vector extractFeatures(Matrix pressureData, int lowerSensor, int upperSensor);

}
