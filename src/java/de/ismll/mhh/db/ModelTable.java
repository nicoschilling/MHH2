package de.ismll.mhh.db;

import de.ismll.database.dao.Column;
import de.ismll.database.dao.Datatypes;
import de.ismll.database.dao.Table;

public class ModelTable extends Table{
	
	public final Column description;
	public final Column name;
	public final Column parameters;
	public final Column windowExtent;
	
	public ModelTable(String tableName) {
		super(tableName);
		
		description = addColumn("description", Datatypes.VString);
		name = addColumn("name", Datatypes.VString);
		parameters = addColumn("parameters", Datatypes.VString);
		windowExtent = addColumn("window_extent", Datatypes.Integer);
		
		
	}
	


}
