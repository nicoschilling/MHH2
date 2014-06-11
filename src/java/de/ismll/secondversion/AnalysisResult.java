package de.ismll.secondversion;

import java.util.HashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.table.Matrix;
import de.ismll.table.Vector;



public class AnalysisResult extends HashMap<String,Object> {

	protected Logger log = LogManager.getLogger(getClass());

	public static final String KEY_PMAX_SAMPLE = "pmax_sample";
	public static final String KEY_END_SAMPLE = "end_sample";
	public static final String KEY_ABSOLUTE_END_SAMPLE = "absolute_end_sample";
	public static final String KEY_PREDICTIONS = "predictions";
	public static final String KEY_SAMPLE2AVGLABELS = "sample2avgLabels";
	public static final String KEY_ACCURACY = "accuracy";
	public static final String KEY_TRUE_SAMPLE = "true_sample";
	public static final String KEY_SAMPLE_DIFFERENCE = "sample_difference";
	public static final String KEY_SAMPLE_DIFFERENCE_RMSE = "sample_difference_rmse";
	public static final String KEY_ANNOTATION_TIME = "annotation_time";
	public static final String KEY_ABSOLUTE_ANNOTATION_TIME = "absolute_annotation_time";
	public static final String KEY_MODEL_CERTAINTY = "model_certainty";
	
	public void setModelCertainty(float in) {
		super.put(KEY_MODEL_CERTAINTY, in);
	}
	
	public float getModelCertainty() {
		return (float) super.get(KEY_MODEL_CERTAINTY);
	}

	
	public String getAnnotationTime() {
		if (super.get(KEY_ANNOTATION_TIME) == null) 
		{ log.warn("Annotation Time is not initialized, will return 0"); return null;}
		else if (super.get(KEY_ANNOTATION_TIME) instanceof String) 
		{ return (String) super.get(KEY_ANNOTATION_TIME);}
		else {log.warn("Annotation Time is not of the type String"); return null; }
	}
	
	public void setAnnotationTime(String str) {
		super.put(KEY_ANNOTATION_TIME, str);
	}

	public String getAbsoluteAnnotationTime() {
		if (super.get(KEY_ABSOLUTE_ANNOTATION_TIME) == null) 
		{ log.warn("Absolute Annotation Time is not initialized, will return 0"); return null;}
		else if (super.get(KEY_ABSOLUTE_ANNOTATION_TIME) instanceof String) 
		{ return (String) super.get(KEY_ABSOLUTE_ANNOTATION_TIME);}
		else {log.warn("Absolute Annotation Time is not of the type String"); return null; }
	}
	
	public void setAbsoluteAnnotationTime(String str) {
		super.put(KEY_ABSOLUTE_ANNOTATION_TIME, str);
	}

	public int getEndSample() {
		if(super.get(KEY_END_SAMPLE) == null)
		{ log.warn("End Sample is not initialized, will return 0"); return 0;}

		else if(super.get(KEY_END_SAMPLE) instanceof Integer)
		{ return (int) super.get(KEY_END_SAMPLE);}

		else{ log.warn("End Sample is not of the type Integer"); return 0; }
	}

	public void setEndSample(int endSample) {
		super.put(KEY_END_SAMPLE, endSample);
	}
	
	public int getAbsoluteEndSample() {
		if(super.get(KEY_ABSOLUTE_END_SAMPLE) == null)
		{ log.warn("Absolute End Sample is not initialized, will return 0"); return 0;}

		else if(super.get(KEY_ABSOLUTE_END_SAMPLE) instanceof Integer)
		{ return (int) super.get(KEY_ABSOLUTE_END_SAMPLE);}

		else{ log.warn("AbsoluteEnd Sample is not of the type Integer"); return 0; }
	}

	public void setAbsoluteEndSample(int endSample) {
		super.put(KEY_ABSOLUTE_END_SAMPLE, endSample);
	}
	
	public Matrix getSample2AvgLabels() {
		if (super.get(KEY_SAMPLE2AVGLABELS) ==  null) 
			{ log.warn("Sample2AvgLabels are not initialized. Will return"); return null ; }
		else if (super.get(KEY_SAMPLE2AVGLABELS) instanceof Matrix) 
		{ return (Matrix) super.get(KEY_SAMPLE2AVGLABELS); }
		
		else { log.warn("Sample2AvgLabels are not of the type Matrix"); return null; }
	}
	
	public void setSample2AvgLabels(Matrix in) {
		super.put(KEY_SAMPLE2AVGLABELS, in);
	}

	public Vector getPredictions() {
		if(super.get(KEY_PREDICTIONS) == null)
		{ log.warn("Predictions are not initialized, will return"); return null; }

		else if (super.get(KEY_PREDICTIONS) instanceof Vector)
		{return (Vector) super.get(KEY_PREDICTIONS);}
		
		else { log.warn("Predictions are not of the type Vector"); return null; }

	}

	public void setPredictions(Vector predictions) {
		super.put(KEY_PREDICTIONS, predictions);
	}

	public float getAccuracy() {
		if(super.get(KEY_ACCURACY) == null)
		{ log.warn("Accuracy is not initialized, will return 0"); return 0; }

		else if (super.get(KEY_ACCURACY) instanceof Float)
		{return (Float) super.get(KEY_ACCURACY);}
		
		else { log.warn("Accuracy is not of the type Float"); return 0; }
	}

	public void setAccuracy(float accuracy) {
		super.put(KEY_ACCURACY, accuracy);
	}

	public int getTrueSample() {
		if(super.get(KEY_TRUE_SAMPLE) == null)
		{ log.warn("True Sample is not initialized, will return 0"); return 0; }

		else if (super.get(KEY_TRUE_SAMPLE) instanceof Integer)
		{return (int) super.get(KEY_TRUE_SAMPLE);}
		
		else { log.warn("True Sample is not of the type Integer"); return 0; }
	}

	public void setTrueSample(int trueSample) {
		super.put(KEY_TRUE_SAMPLE, trueSample);
	}

	public float getSampleWiseRMSE() {
		if(super.get(KEY_SAMPLE_DIFFERENCE_RMSE) == null)
		{ log.warn("RMSE Sample Difference is not initialized, will return 0"); return 0; }

		else if (super.get(KEY_SAMPLE_DIFFERENCE_RMSE) instanceof Float)
		{return (Float) super.get(KEY_SAMPLE_DIFFERENCE_RMSE);}
		
		else { log.warn("RMSE Sample Difference is not of the type Float"); return 0; }
	}

	public void setSampleWiseRMSE(float sampleWiseRMSE) {
		super.put(KEY_SAMPLE_DIFFERENCE_RMSE, sampleWiseRMSE);
	}

	public int getSampleDifference() {
		if(super.get(KEY_SAMPLE_DIFFERENCE) == null)
		{ log.warn("Sample Difference is not initialized, will return 0"); return 0; }

		else if (super.get(KEY_SAMPLE_DIFFERENCE) instanceof Integer)
		{return (int) super.get(KEY_SAMPLE_DIFFERENCE);}
		
		else { log.warn(" Sample Difference is not of the type Integer"); return 0; }
	}

	public void setSampleDifference(int sampleDifference) {
		super.put(KEY_SAMPLE_DIFFERENCE, sampleDifference);
	}

	public int getPmaxSample() {
		if(super.get(KEY_PMAX_SAMPLE) == null)
		{ log.warn("Pmax Sample is not initialized, will return 0"); return 0; }

		else if (super.get(KEY_PMAX_SAMPLE) instanceof Integer)
		{return (int) super.get(KEY_PMAX_SAMPLE);}
		
		else { log.warn("Pmax Sample is not of the type Integer"); return 0; }
	}

	public void setPmaxSample(int pmaxSample) {
		super.put(KEY_PMAX_SAMPLE, pmaxSample);
	}

	
}
