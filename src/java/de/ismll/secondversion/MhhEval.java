package de.ismll.secondversion;

import de.ismll.table.Matrix;

public class MhhEval {
	
	public Matrix[] predictedTestLabels;
	public Matrix[] avgTestLabels;
	
	public Matrix[] predictedValidationLabels;
	public Matrix[] avgValidationLabels;
	
	public Matrix[] predictedTrainLabels;
	public Matrix[] avgTrainLabels;
	
	public int[] testAnnotations;
	public int[] trainAnnotations;
	public int[] validationAnnotations;
	
	
	
	public MhhEval(int testSwallows, int validationSwallows, int trainSwallows) {
		this.predictedTestLabels = new Matrix[testSwallows];
		this.avgTestLabels = new Matrix[testSwallows];
		
		this.predictedValidationLabels = new Matrix[validationSwallows];
		this.avgValidationLabels = new Matrix[validationSwallows];
		
		this.predictedTrainLabels = new Matrix[trainSwallows];
		this.avgTrainLabels = new Matrix[trainSwallows];
		
		this.testAnnotations = new int[testSwallows];
		this.trainAnnotations = new int[trainSwallows];
		this.validationAnnotations = new int[validationSwallows];
	}
	
	public MhhEval() {
		
	}
	
	
	
	public Matrix[] getPredictedTestLabels() {
		return predictedTestLabels;
	}
	public void setPredictedTestLabels(Matrix[] predictedTestLabels) {
		this.predictedTestLabels = predictedTestLabels;
	}
	public Matrix[] getAvgTestLabels() {
		return avgTestLabels;
	}
	public void setAvgTestLabels(Matrix[] avgTestLabels) {
		this.avgTestLabels = avgTestLabels;
	}
	public Matrix[] getPredictedValidationLabels() {
		return predictedValidationLabels;
	}
	public void setPredictedValidationLabels(Matrix[] predictedValidationLabels) {
		this.predictedValidationLabels = predictedValidationLabels;
	}
	public Matrix[] getAvgValidationLabels() {
		return avgValidationLabels;
	}
	public void setAvgValidationLabels(Matrix[] avgValidationLabels) {
		this.avgValidationLabels = avgValidationLabels;
	}
	public Matrix[] getPredictedTrainLabels() {
		return predictedTrainLabels;
	}
	public void setPredictedTrainLabels(Matrix[] predictedTrainLabels) {
		this.predictedTrainLabels = predictedTrainLabels;
	}
	public Matrix[] getAvgTrainLabels() {
		return avgTrainLabels;
	}
	public void setAvgTrainLabels(Matrix[] avgTrainLabels) {
		this.avgTrainLabels = avgTrainLabels;
	}
}
