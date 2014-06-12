package de.ismll.secondversion;

import de.ismll.table.Matrix;
import de.ismll.table.Vector;

public class MhhRawData {
	
	public MhhRawData() {
		
	}
	
	public MhhRawData(int testSwallows, int validationSwallows, int trainSwallows) {
		
		
		trainData = new Matrix[trainSwallows];
		trainDataLabels = new Matrix[trainSwallows];
		trainRuhedruck = new Matrix[trainSwallows+testSwallows];
		trainRuhedruckLabels = new Matrix[trainSwallows+testSwallows];
		instanceWeights = new Matrix[trainSwallows];
		trainDataAnnotations = new int[trainSwallows];
		
		validationData = new Matrix[validationSwallows];
		validationDataLabels = new Matrix[validationSwallows];
		validationDataAnnotations = new int[validationSwallows];
		
		testData = new Matrix[testSwallows];
		testDataLabels = new Matrix[testSwallows];
		testRuhedruck = new Matrix[testSwallows];
		testRuhedruckLabels = new Matrix[testSwallows];
		testDataAnnotations = new int[testSwallows];
		
	}
	
	Matrix[] instanceWeights;
	Matrix[] trainData;
	Matrix[] trainDataLabels;
	Matrix[] trainRuhedruck; 
	Matrix[] trainRuhedruckLabels;
	int[] trainDataAnnotations;
	
	Matrix[] validationData;
	Matrix[] validationDataLabels;
	int[] validationDataAnnotations;
	
	Matrix[] testData;
	Matrix[] testDataLabels;
	Matrix[] testRuhedruck;
	Matrix[] testRuhedruckLabels;
	int[] testDataAnnotations;
	
	
	
	
	public Matrix[] getTrainData() {
		return trainData;
	}
	public void setTrainData(Matrix[] trainData) {
		this.trainData = trainData;
	}
	public Matrix[] getTrainDataLabels() {
		return trainDataLabels;
	}
	public void setTrainDataLabels(Matrix[] trainDataLabels) {
		this.trainDataLabels = trainDataLabels;
	}
	public Matrix[] getValidationData() {
		return validationData;
	}
	public void setValidationData(Matrix[] validationData) {
		this.validationData = validationData;
	}
	public Matrix[] getValidationDataLabels() {
		return validationDataLabels;
	}
	public void setValidationDataLabels(Matrix[] validationDataLabels) {
		this.validationDataLabels = validationDataLabels;
	}
	public Matrix[] getTestData() {
		return testData;
	}
	public void setTestData(Matrix[] testData) {
		this.testData = testData;
	}
	public Matrix[] getTestDataLabels() {
		return testDataLabels;
	}
	public void setTestDataLabels(Matrix[] testDataLabels) {
		this.testDataLabels = testDataLabels;
	}

	public int[] getTrainDataAnnotations() {
		return trainDataAnnotations;
	}

	public void setTrainDataAnnotations(int[] trainAnnotations) {
		this.trainDataAnnotations = trainAnnotations;
	}

	public int[] getValidationDataAnnotations() {
		return validationDataAnnotations;
	}

	public void setValidationDataAnnotations(int[] validationDataAnnotations) {
		this.validationDataAnnotations = validationDataAnnotations;
	}

	public int[] getTestDataAnnotations() {
		return testDataAnnotations;
	}

	public void setTestDataAnnotations(int[] testDataAnnotations) {
		this.testDataAnnotations = testDataAnnotations;
	}

}
