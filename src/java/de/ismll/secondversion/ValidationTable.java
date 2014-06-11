package de.ismll.secondversion;

import de.ismll.database.dao.Column;
import de.ismll.database.dao.Datatypes;
import de.ismll.database.dao.Table;

public class ValidationTable extends Table{
	
	public final Column experimentName;
	public final Column proband;
	public final Column split;
	public final Column windowExtent;
	public final Column validationAccuracy;
	public final Column validationSampleDifference;
	public final Column testAccuracy;
	public final Column testSampleDifference;
	public final Column predictedAnnotation;
	public final Column trueAnnotation;
	
	public ValidationTable(String tablename) {
		super(tablename);
		
		experimentName = addColumn("experiment_name", Datatypes.VString);
		proband = addColumn("proband", Datatypes.Integer);
		split = addColumn("split", Datatypes.Integer);
		windowExtent = addColumn("window_extent", Datatypes.Integer);
		validationAccuracy = addColumn("validation_accuracy", Datatypes.Double);
		validationSampleDifference = addColumn("validation_sample_diff", Datatypes.Double);
		testAccuracy = addColumn("test_accuracy", Datatypes.Double);
		testSampleDifference = addColumn("test_sample_diff", Datatypes.Double);
		predictedAnnotation = addColumn("predicted_annotation", Datatypes.Integer);
		trueAnnotation = addColumn("true_annotation", Datatypes.Integer);
	}

}
