package de.ismll.mhh.featureExtractors;

import de.ismll.secondversion.*;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.projections.ColumnSubsetMatrixView;

public class TimeFeatureExtractor {

	public Matrix extractFeatures(Matrix data) {
		
		int nrCols = data.getNumColumns();
		int nrRows = data.getNumRows();
		
		Matrix ret = new DefaultMatrix(nrRows, nrCols);
		
		for (int feature = 0; feature < nrCols ; feature++) {
			for (int instance = 0; instance < nrRows ; instance++) {
				float value = 0;
				if (instance == 0 || instance == (nrRows-1) ) {
					value = data.get(instance, feature);
				}
				else {
					value = ( data.get(instance+1, feature) - data.get(instance-1, feature) )/2;
				}
				ret.set(instance, feature, value);
			}
		}
		
		return ret;
	}

}
