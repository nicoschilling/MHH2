package de.ismll.secondversion;

import de.ismll.database.dao.Column;
import de.ismll.database.dao.Datatypes;
import de.ismll.database.dao.Table;

public class RunTable extends Table{

	
	public final Column stepSize;
	public final Column reg0;
	public final Column regW;
	public final Column regV;
	public final Column nrLatentFeatures;
	public final Column windowExtent;
	public final Column batchSize;
	public final Column splitPath;
	public final Column smoothReg;
	public final Column smoothWindow;
	
	public RunTable(String tablename) {
		super(tablename);
		
		stepSize = addColumn("step_size", Datatypes.Double);
		reg0 = addColumn("reg0", Datatypes.Double);
		regW = addColumn("regW", Datatypes.Double);
		regV = addColumn("regV", Datatypes.Double);
		nrLatentFeatures = addColumn("nrLatentFeatures", Datatypes.Integer);
		windowExtent = addColumn("window_extent", Datatypes.Integer);
		batchSize = addColumn("batch_size", Datatypes.Integer);
		splitPath = addColumn("split", Datatypes.VString);
		smoothReg = addColumn("smoothReg", Datatypes.Double);
		smoothWindow = addColumn("smoothWindow", Datatypes.Integer);
	}

}
