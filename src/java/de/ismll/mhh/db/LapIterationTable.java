package de.ismll.mhh.db;

import de.ismll.database.dao.Column;
import de.ismll.database.dao.Datatypes;
import de.ismll.database.dao.Table;

public class LapIterationTable extends Table {

	public final Column iterationNumber;
	public final Column accuracy;
	public final Column sampleDifference;
	public final Column overshootPercentage;
	public final Column modelParameters;
	public final Column splitNumber;
	public final Column probandNumber;
	public final Column fkRunId;

	public LapIterationTable(String tablename) {
		super(tablename);
		
		iterationNumber = addColumn("iteration_nr", Datatypes.Integer);
		accuracy = addColumn("accuracy", Datatypes.Double);
		sampleDifference = addColumn("sample_difference", Datatypes.Double);
		overshootPercentage = addColumn("overshoot", Datatypes.Double);
		modelParameters = addColumn("model_parameters", Datatypes.VString); // -> Save Parameter Vector here!
		splitNumber = addColumn("split", Datatypes.Integer);
		probandNumber = addColumn("proband", Datatypes.Integer);
		fkRunId = addColumn("run_id", Datatypes.Integer);  // Ist Id dann ein String?
		
		
	}

}
