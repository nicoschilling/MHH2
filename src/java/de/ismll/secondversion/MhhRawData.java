package de.ismll.secondversion;

import de.ismll.table.Matrix;
import de.ismll.table.Vector;

public class MhhRawData {
	
	public MhhRawData() {
		
	}
	
	public MhhRawData(int testSwallows, int validationSwallows, int trainSwallows) {
		
		
		trainData = new Matrix[trainSwallows];
		trainDataLabels = new Matrix[trainSwallows];
//		trainRuhedruck = new Matrix[trainSwallows+testSwallows];
//		trainRuhedruckLabels = new Matrix[trainSwallows+testSwallows];
		instanceWeights = new Matrix[trainSwallows];
		trainDataRelativeAnnotations = new int[trainSwallows];
		trainDataAbsoluteAnnotations = new int[trainSwallows];
		
		validationData = new Matrix[validationSwallows];
		validationDataLabels = new Matrix[validationSwallows];
		validationDataRelativeAnnotations = new int[trainSwallows];
		validationDataAbsoluteAnnotations = new int[trainSwallows];

		testData = new Matrix[testSwallows];
		testDataLabels = new Matrix[testSwallows];
//		testRuhedruck = new Matrix[testSwallows];
//		testRuhedruckLabels = new Matrix[testSwallows];
		testDataRelativeAnnotations = new int[testSwallows];
		testDataAbsoluteAnnotations = new int[testSwallows];

	}
	
	Matrix[] instanceWeights;
	Matrix[] trainData;
	Matrix[] trainDataLabels;
//	Matrix[] trainRuhedruck; 
//	Matrix[] trainRuhedruckLabels;
	@Deprecated
	int[] trainDataRelativeAnnotations;
	@Deprecated
	int[] trainDataAbsoluteAnnotations;
	
	Matrix[] validationData;
	Matrix[] validationDataLabels;
	@Deprecated
	int[] validationDataRelativeAnnotations;
	@Deprecated
	int[] validationDataAbsoluteAnnotations;
	
	Matrix[] testData;
	Matrix[] testDataLabels;
//	Matrix[] testRuhedruck;
//	Matrix[] testRuhedruckLabels;
	@Deprecated
	int[] testDataRelativeAnnotations;
	@Deprecated
	int[] testDataAbsoluteAnnotations;
	
	
	
	
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

	public int[] getTrainDataRelativeAnnotations() {
		return trainDataRelativeAnnotations;
	}

	public void setTrainDataRelativeAnnotations(int[] trainAnnotations) {
		this.trainDataRelativeAnnotations = trainAnnotations;
	}

	public int[] getValidationDataRelativeAnnotations() {
		return validationDataRelativeAnnotations;
	}

	public void setValidationDataRelativeAnnotations(int[] validationDataAnnotations) {
		this.validationDataRelativeAnnotations = validationDataAnnotations;
	}

	public int[] getTestDataRelativeAnnotations() {
		return testDataRelativeAnnotations;
	}

	public void setTestDataRelativeAnnotations(int[] testDataAnnotations) {
		this.testDataRelativeAnnotations = testDataAnnotations;
	}

}
