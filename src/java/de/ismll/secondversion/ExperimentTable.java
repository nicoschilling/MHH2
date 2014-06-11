package de.ismll.secondversion;

import de.ismll.database.dao.Column;
import de.ismll.database.dao.Datatypes;
import de.ismll.database.dao.Table;

public class ExperimentTable extends Table{
	
	public final Column experimentName;
	public final Column proband;
	public final Column split;
	public final Column modelParameters;
	public final Column accuracy;
	public final Column sampleDifference;
	public final Column stepSize;
	public final Column lambda;
	public final Column smoothReg;
	public final Column smoothWindow;
	public final Column windowExtent;
	public final Column iterationNr;
	
	public ExperimentTable(String tablename) {
		super(tablename);
		
		experimentName = addColumn("experiment_name", Datatypes.VString);
		proband = addColumn("proband", Datatypes.Integer);
		split = addColumn("split", Datatypes.Integer);
		modelParameters = addColumn("model_parameters", Datatypes.VString);
		accuracy = addColumn("accuracy" , Datatypes.Double);
		sampleDifference = addColumn("sample_difference", Datatypes.Double);
		stepSize = addColumn("step_size", Datatypes.Double);
		lambda = addColumn("lambda", Datatypes.Double);
		smoothReg = addColumn("smooth_reg", Datatypes.Double);
		smoothWindow = addColumn("smooth_window", Datatypes.Integer);
		
		windowExtent = addColumn("window_extent", Datatypes.Integer);
		iterationNr = addColumn("iteration_nr", Datatypes.Integer);
		
		// TODO Auto-generated constructor stub
	}

	

}
