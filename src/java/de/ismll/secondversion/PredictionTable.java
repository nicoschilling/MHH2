package de.ismll.secondversion;


import de.ismll.database.dao.Column;
import de.ismll.database.dao.Datatypes;
import de.ismll.database.dao.Table;

public class PredictionTable extends Table{

	
	
	  public final Column proband;
	  public final Column swallow;
	  public final Column pmax;
	  public final Column pmaxTime;
	  public final Column annotation;
	  public final Column predictedAnnotation;
	  public final Column sampleDifference;
	  public final Column annotationTime;
	  public final Column predictedAnnotationTime;
	  public final Column timeDifference;
	  public final Column restitutionTimeInSamples;
	  public final Column restitutionTime;
	  public final Column predictedRestitutionTimeInSamples;
	  public final Column predictedRestitutionTime;
	  
	  
	  public PredictionTable(String tablename) {
		  super(tablename);
		  
		  proband = addColumn("proband", Datatypes.Integer);
		  swallow = addColumn("swallow", Datatypes.Integer);
		  pmax = addColumn("pmax", Datatypes.Integer);
		  pmaxTime = addColumn("pmax_time", Datatypes.VString);
		  annotation = addColumn("annotation", Datatypes.Integer);
		  predictedAnnotation = addColumn("predicted_annotation", Datatypes.Integer);
		  sampleDifference = addColumn("sample_difference", Datatypes.Integer);
		  annotationTime = addColumn("annotation_time", Datatypes.VString);
		  predictedAnnotationTime = addColumn("predicted_annotation_time", Datatypes.VString);
		  timeDifference = addColumn("time_difference", Datatypes.VString);
		  restitutionTimeInSamples = addColumn("restitution_time_samples", Datatypes.Integer);
		  restitutionTime = addColumn("restitution_time", Datatypes.VString);
		  predictedRestitutionTimeInSamples = addColumn("predicted_restitution_time_samples", Datatypes.Integer);
		  predictedRestitutionTime = addColumn("predicted_restitution_time", Datatypes.VString);
		  
	  }
	
	
	
	
	
}
