package de.ismll.mhh.featureExtractors;

import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.impl.DefaultVector;
import de.ismll.table.projections.ColumnSubsetMatrixView;

public class MatrixOperations {

	public Vector averageOverColumns(Matrix in, int[] usedIndexes) {
		
		int numRows = in.getNumRows();
		
		Matrix chosenColumns = new DefaultMatrix(new ColumnSubsetMatrixView(in, usedIndexes));
		
		Vector ret = new DefaultVector(numRows);
		
		float sum = 0;
		
		for (int row = 0; row < numRows ; row++) {
			sum = 0;
			for (int col = 0; col < chosenColumns.getNumColumns() ; col++ ) {
				sum += chosenColumns.get(row, col);
			}
			ret.set(row, sum/chosenColumns.getNumColumns());
		}
		
		return ret;
		
		
	}
	
}
