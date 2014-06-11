package de.ismll.secondversion;

import de.ismll.exceptions.ModelApplicationException;
import de.ismll.mhh.io.DataInterpretation;
import de.ismll.table.Matrix;



public interface ApplyMHHModel {

	public AnalysisResult predict(DataInterpretation rf) throws ModelApplicationException;
	
	public void setWindowExtent(int windowExtent);
	
	public int getWindowExtent();
	
	public void setAveragePredictions(Matrix averagePredictions);
	
	public Matrix getAveragePredictions();

	AnalysisResult predict(DataInterpretation rf, int pmax)
			throws ModelApplicationException;
	
	public String getHumanName();
}
