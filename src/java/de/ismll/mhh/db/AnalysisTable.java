package de.ismll.mhh.db;

import de.ismll.database.dao.Column;
import de.ismll.database.dao.Datatypes;
import de.ismll.database.dao.Table;

public class AnalysisTable extends Table{
	
	public final Column predictions;
	public final Column restitutionsSample;
	public final Column startSearch;
	public final Column averageWindow;

	public AnalysisTable(String tableName) {
		super(tableName);
		
		predictions = addColumn("predictions", Datatypes.VString);
		restitutionsSample = addColumn("restitutions_sample", Datatypes.Integer);
		startSearch	= addColumn("start_search", Datatypes.Integer);
		averageWindow = addColumn("average_window", Datatypes.Integer);
		
	}

}
