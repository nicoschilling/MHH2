package de.ismll.secondversion;

import java.io.File;
import java.io.IOException;


import de.ismll.bootstrap.CommandLineParser;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Matrices.FileType;

/**
 * Simple dataset, read from a directory, requiring the presence of the mentioned FILENAME_s therein.
 * 
 * 
 * @author Andre Busche
 *
 */
public class MhhDataset {
	public static final String FILENAME_VALIDATION_LABEL = "validation.label.bin";
	public  static final String FILENAME_VALIDATION_DATA = "validation.data.bin";
	public  static final String FILENAME_TRAIN_LABEL = "train.label.bin";
	public  static final String FILENAME_TRAIN_DATA = "train.data.bin";
	public  static final String FILENAME_TEST_LABEL = "test.label.bin";
	public  static final String FILENAME_TEST_DATA = "test.data.bin";
	/**
	 * Matrix mit TrainingsDaten
	 */
	public Matrix trainData;
	
	public Matrix instanceWeights;
	
	/**
	 * Matrix mit TrainDaten plus RuheDruck von Test
	 */
	public Matrix ruheDruckTrainData;
	
	/**
	 * Matrix mit TestDaten
	 */
	public Matrix testData;
	
	/**
	 * Matrix mit ValidationDaten
	 */
	public Matrix validationData;
	
	/**
	 * Matrix mit TrainingsDaten Labels
	 */
	public Matrix trainDataLabels;
	
//	/**
//	 * Matrix mit TrainDaten plus RuheDruck von Test
//	 */
//	public Matrix ruheDruckTrainDataLabels;
	
	/**
	 * Matrix mit TestDaten Labels
	 */
	public Matrix testDataLabels;
	
	/**
	 * Matrix mit ValidationDaten Labels
	 */
	public Matrix validationDataLabels;

	public MhhDataset() {
	}

	public void writeTo(File serializeData) throws IOException {
		Matrices.writeBinary(testData, new File(serializeData, FILENAME_TEST_DATA));
		Matrices.writeBinary(testDataLabels, new File(serializeData, FILENAME_TEST_LABEL));
		Matrices.writeBinary(trainData, new File(serializeData, FILENAME_TRAIN_DATA));
		Matrices.writeBinary(trainDataLabels, new File(serializeData, FILENAME_TRAIN_LABEL));
		Matrices.writeBinary(validationData, new File(serializeData, FILENAME_VALIDATION_DATA));
		Matrices.writeBinary(validationDataLabels, new File(serializeData, FILENAME_VALIDATION_LABEL));
	}
	
	public static MhhDataset convert(Object in) {
		if (in instanceof File)
			return read((File)in);
		String source;
		if (in instanceof String) {
			source = (String) in;
		}
		else {
			source = in.toString();					
		}
		return read((File)CommandLineParser.convert(source, File.class));
	}

	public static MhhDataset read(File data) {
		MhhDataset ret = new MhhDataset();
		
		try {
			ret.testData = Matrices.read(new File(data, FILENAME_TEST_DATA), FileType.Binary, -1);
			ret.testDataLabels = Matrices.read(new File(data, FILENAME_TEST_LABEL), FileType.Binary,  -1);
			ret.trainData = Matrices.read(new File(data, FILENAME_TRAIN_DATA), FileType.Binary,  -1);
			ret.trainDataLabels = Matrices.read(new File(data, FILENAME_TRAIN_LABEL), FileType.Binary,  -1);
			ret.validationData = Matrices.read(new File(data, FILENAME_VALIDATION_DATA), FileType.Binary,  -1);
			ret.validationDataLabels = Matrices.read(new File(data, FILENAME_VALIDATION_LABEL), FileType.Binary,  -1);
		} catch (IOException e) {
			e.printStackTrace();
			
		}
		
		return ret;
	}

	public Matrix getTrainData() {
		return trainData;
	}

	public void setTrainData(Matrix trainData) {
		this.trainData = trainData;
	}

	public Matrix getTestData() {
		return testData;
	}

	public void setTestData(Matrix testData) {
		this.testData = testData;
	}

	public Matrix getValidationData() {
		return validationData;
	}

	public void setValidationData(Matrix validationData) {
		this.validationData = validationData;
	}

	public Matrix getTrainDataLabels() {
		return trainDataLabels;
	}

	public void setTrainDataLabels(Matrix trainDataLabels) {
		this.trainDataLabels = trainDataLabels;
	}

	public Matrix getTestDataLabels() {
		return testDataLabels;
	}

	public void setTestDataLabels(Matrix testDataLabels) {
		this.testDataLabels = testDataLabels;
	}

	public Matrix getValidationDataLabels() {
		return validationDataLabels;
	}

	public void setValidationDataLabels(Matrix validationDataLabels) {
		this.validationDataLabels = validationDataLabels;
	}

	public Matrix getRuheDruckTrainData() {
		return ruheDruckTrainData;
	}

	public void setRuheDruckTrainData(Matrix ruheDruckTrainData) {
		this.ruheDruckTrainData = ruheDruckTrainData;
	}

	public Matrix getInstanceWeights() {
		return instanceWeights;
	}

	public void setInstanceWeights(Matrix instanceWeights) {
		this.instanceWeights = instanceWeights;
	}
}