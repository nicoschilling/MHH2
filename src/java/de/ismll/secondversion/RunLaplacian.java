package de.ismll.secondversion;

import de.ismll.database.dao.Column;
import de.ismll.database.dao.Datatypes;
import de.ismll.database.dao.Table;

public class RunLaplacian extends Table{
	
	
	public final Column stepSize;
	public final Column lambda;
	public final Column windowExtent;
	public final Column batchSize;
	public final Column splitPath;
	public final Column smoothReg;
	public final Column smoothWindow;
	
	public RunLaplacian(String tablename) {
		super(tablename);
		
		stepSize = addColumn("step_size", Datatypes.Double);
		lambda = addColumn("lambda", Datatypes.Double);
		windowExtent = addColumn("window_extent", Datatypes.Integer);
		batchSize = addColumn("batch_size", Datatypes.Integer);
		splitPath = addColumn("split", Datatypes.VString);
		smoothReg = addColumn("smooth_reg", Datatypes.Double);
		smoothWindow = addColumn("smooth_window", Datatypes.Integer);
	} 

}



