package de.ismll.mhh.db;

import de.ismll.database.dao.Column;
import de.ismll.database.dao.Datatypes;
import de.ismll.database.dao.Table;

public class DataInterpretationTable extends Table{
	
	public final Column rdStart;
	public final Column rdEnd;
	public final Column channelStart;
	public final Column channelEnd;
	public final Column date;
	public final Column misc1;	
	

	public DataInterpretationTable(String tableName) {
		super(tableName);
		
		rdStart = addColumn("rd_start", Datatypes.Integer);
		rdEnd = addColumn("rd_end", Datatypes.Integer);
		channelStart = addColumn("channel_start", Datatypes.Integer);
		channelEnd = addColumn("channel_end", Datatypes.Integer);
		date = addColumn("date", Datatypes.VString);
		misc1 = addColumn("misc1", Datatypes.VString);
		
		
	}

}
