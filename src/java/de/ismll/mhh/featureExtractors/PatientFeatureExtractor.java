package de.ismll.mhh.featureExtractors;

import de.ismll.mhh.io.DataInterpretation;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.impl.DefaultVector;

public class PatientFeatureExtractor {

	public Matrix extractFeatures(DataInterpretation folder) {

		Matrix ret = null;

		int proband = folder.getProband();

		Vector categoricalPatientFeatures = new DefaultVector(10);

		categoricalPatientFeatures.set(proband-1, 1);

		ret = new DefaultMatrix(folder.getDruck().getNumRows(),10);

		for (int row = 0; row < ret.getNumRows(); row++) {
			Vectors.set(Matrices.row(ret,row),categoricalPatientFeatures);
		}

		return ret;
	}

}
