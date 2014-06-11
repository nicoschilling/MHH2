package de.ismll.mhh.db;

import de.ismll.database.dao.Column;
import de.ismll.database.dao.Datatypes;
import de.ismll.database.dao.Table;  



public class SwallowTable extends Table{
	
	public final Column probandId;
	public final Column swallowId;
	public final Column samplerate;
	public final Column firstSample;
	public final Column lastSample;
	public final Column misc1;
	public final Column misc2;
	public final Column date;
	public final Column firstPmax;
	public final Column multiswallow;
	

	public SwallowTable(String tableName) {
		super(tableName);
		
		probandId = addColumn("proband_id", Datatypes.Integer);
		swallowId = addColumn("swallow_id", Datatypes.Integer);
		samplerate = addColumn("samplerate", Datatypes.Integer);
		firstSample = addColumn("firstsample", Datatypes.Integer);
		lastSample = addColumn("lastsample", Datatypes.Integer);
		misc1 = addColumn("misc1", Datatypes.VString);
		misc2 = addColumn("misc2", Datatypes.VString);
		date = addColumn("date", Datatypes.VString);
		firstPmax = addColumn("pfirstmax", Datatypes.Integer);
		multiswallow = addColumn("multiswallow", Datatypes.Integer);
		
		
		
		
	}
	
	
	

}
