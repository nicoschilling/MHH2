package de.ismll.mhh.featureExtractors;

import de.ismll.mhh.io.DataInterpretation;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.impl.DefaultVector;

public class AcidFeatureExtractor {
	
	public Matrix extractFeatures(DataInterpretation folder) {
		Matrix ret = null;
		
		String acid_level = folder.getAcid_level();
		
		Vector categoricalAcidFeatures = new DefaultVector(4);
		
		if (acid_level.equals("7")) {
			categoricalAcidFeatures.set(0, 1);
			System.out.println("This is a normal swallow");
		}
		else if(acid_level.equals("5")) {
			categoricalAcidFeatures.set(1, 1);
			System.out.println("This is a little acidic swallow");
		}
		else if(acid_level.equals("3")) {
			categoricalAcidFeatures.set(2, 1);
			System.out.println("This is a more acidic swallow");
		}
		else if(acid_level.equals("1.8")) {
			categoricalAcidFeatures.set(3, 1);
			System.out.println("This is a really acidic swallow");
		}
		else {
			System.out.println("Acid Features are not of a known type and could not be parsed!");
			System.out.println("Returning only zeros as categorical acid features...");
		}
		
		ret = new DefaultMatrix(folder.getDruck().getNumRows(),4);
		
		for (int row = 0; row < ret.getNumRows(); row++) {
			Vectors.set(Matrices.row(ret,row),categoricalAcidFeatures);
		}
		
	
		return ret;
	}

}
