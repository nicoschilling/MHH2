package de.ismll.mhh.featureExtractors;

import de.ismll.secondversion.*;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.projections.ColumnSubsetMatrixView;

public class TimeFeatureExtractor {
	

	public Matrix extractFeatures(Matrix data, int order) {
		
		// order = 1 -> gehe 1 nach links und rechts, order 2, dann gehe 2 nach links und rechts, usw...
		
		int nrCols = data.getNumColumns();
		int nrRows = data.getNumRows();
		
		Matrix ret = new DefaultMatrix(nrRows, nrCols);
		
		for (int feature = 0; feature < nrCols ; feature++) {
			for (int instance = 0; instance < nrRows ; instance++) {
				float value = 0;
				if ( (instance-order+1) <= 0 || (instance+order-1) >= (nrRows-1) ) {
					value = data.get(instance, feature);
				}
				else {
					if (order == 1) {
						value = ( data.get(instance+1, feature) - data.get(instance-1, feature) )/2;
					}
					else if (order == 2)  {
						value = ( 1/12*data.get(instance-2, feature) -2/3*data.get(instance-1, feature) + 
								2/3*data.get(instance+1, feature) + 1/12*data.get(instance+2, feature));
					}
					else if (order == 3) {
						value = ( -1/60*data.get(instance-3, feature) + 3/20*data.get(instance-2, feature) +  -3/4*data.get(instance-1, feature) 
								+ 3/4*data.get(instance+1, feature)
								- 3/20*data.get(instance+2, feature) + 1/60*data.get(instance+3, feature));
					}
					else if (order == 4) {
						value = (1/280*data.get(instance-4, feature) -4/105*data.get(instance-3, feature)
								+ 1/5*data.get(instance-2, feature) +  -4/5*data.get(instance-1, feature) 
								+ 4/5*data.get(instance+1, feature)
								- 1/5*data.get(instance+2, feature) + 4/105*data.get(instance+3, feature) - 1/280*data.get(instance+4, feature));
					}
					
				}
				ret.set(instance, feature, value);
			}
		}
		
		return ret;
	}

	

}
