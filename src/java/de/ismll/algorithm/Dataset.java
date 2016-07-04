package de.ismll.algorithm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Dataset {

	// TODO: Only have dense encoding of data... how about sparse with HashMaps?

	public Dataset(String path) {
		dataPath = path;
	}
	public Dataset() { }
		

	private Logger logger = LogManager.getLogger(getClass());

	String dataPath;

	float[][] trainDataArray;
	float[] trainLabels;

	float[][] validationDataArray;
	float[] validationLabels;

	float[][] testDataArray;
	float[] testLabels;

	int nrVariables; // THINK OF BIAS!!

	int trainInstances;
	int validationInstances;
	int testInstances;

	public enum Filetype { LibSVM, Dense }

	/**
	 * Returns the number of instances and variables of the Dataset specified by the file given
	 */
	public int[] readDimensions(File file, Filetype filetype, String delimiter) {
		switch(filetype) {
		case LibSVM:
			return readDimensionsLibSVM(file, delimiter);
		case Dense:
			return readDimensionsDense(file, delimiter);
		default:
			logger.warn("Filetype is not specified, Cannot continue reading Dimensions."); 
			return null;
		}


	}
	@SuppressWarnings("unused")
	public int[] readDimensionsLibSVM(File file, String delimiter) {
		int nrVariables = 0;
		int row = 0;
		try {
			Scanner input = new Scanner(file);
			while(input.hasNextLine()) {
				StringTokenizer line = new StringTokenizer(input.nextLine(), delimiter);
				int column = 0;
				while(line.hasMoreTokens()) {
					if(column == 0) {
						String dummie = line.nextToken();
						column++;
					}
					else {
						StringTokenizer pair = new StringTokenizer(line.nextToken(), ":");
						int variable = Integer.parseInt(pair.nextToken());
						String dummie2 = pair.nextToken();
						if (variable > nrVariables) {
							nrVariables = variable;
						}
						column++;
					}
				}
				row++;
			}
			input.close();
		}
		catch(FileNotFoundException e)
		{
			logger.warn("File: " + file.getAbsolutePath() + " not found at reading Dimensions! Will not be able to read Data");
		}	
		int[] ret = new int[2];
		ret[0] = nrVariables;
		ret[1] = row;
		return ret;
	}
	@SuppressWarnings("unused")
	public int[] readDimensionsDense(File file, String delimiter) {
		int nrVariables = 0;
		int row = 0;
		try {
			Scanner input = new Scanner(file);
			StringTokenizer line = new StringTokenizer(input.nextLine(), delimiter);
			int column = 0;
			while(line.hasMoreTokens()) {
				if(column == 0) {
					String dummie = line.nextToken();
					column++;
				}
				else {
					String dummie = line.nextToken();
					nrVariables++;
					column++;
				}
			}
			row++;
			while(input.hasNextLine()) {
				String line2 = input.nextLine();
				row++;
			}
			input.close();
		}
		catch(FileNotFoundException e)
		{
			logger.warn("File: " + file.getAbsolutePath() + " not found at reading Dimensions! Will not be able to read Data");
		}	
		int[] ret = new int[2];
		ret[0] = nrVariables;
		ret[1] = row;
		return ret;
	}
	/**
	 * Reads the whole Data and builds a float-array out of it!
	 * @param file File that contains the Data.
	 */
	public float[][] readData(File file, Filetype filetype, String delimiter)
	{
		switch (filetype) {
		case LibSVM:
			return readDataLibSVM(file, delimiter);
		case Dense:
			return readDataDense(file, delimiter);
		default:
			logger.warn("Filetype is not specified, Cannot continue reading Data."); 
			return null;
		}
	}
	@SuppressWarnings("unused")
	public float[][] readDataDense(File file, String delimiter) {
		int[] dimensions = readDimensionsLibSVM(file, delimiter);
		float[][] data = new float[dimensions[1]][dimensions[0]+1]; //+1 since bias term
		int row = 0;
		try
		{
			Scanner input = new Scanner(file);
			while(input.hasNextLine())
			{
				StringTokenizer line = new StringTokenizer(input.nextLine(), delimiter);
				int column = 0;
				while(line.hasMoreTokens()) {
					if(column == 0) {
						String dummie = line.nextToken();
						data[row][column] = 1; //BiasTerm!!
						column++;
					}
					else {
						data[row][column] = Float.parseFloat(line.nextToken());
						column++;
					}
				}
				row++;
			}
			input.close();
		}
		catch(FileNotFoundException e)
		{
			System.out.println("File not found :-(");
		}
		return data;

	}
	@SuppressWarnings("unused")
	public float[][] readDataLibSVM(File file, String delimiter) {
		int[] dimensions = readDimensionsLibSVM(file, delimiter);
		float[][] data = new float[dimensions[1]][dimensions[0]+1]; //+1 since bias term
		int row = 0;
		try
		{
			Scanner input = new Scanner(file);
			while(input.hasNextLine())
			{
				StringTokenizer line = new StringTokenizer(input.nextLine(), delimiter);
				int column = 0;
				while(line.hasMoreTokens()) {
					if(column == 0) {
						String dummie = line.nextToken();
						data[row][column] = 1; //BiasTerm!!
						column++;
					}
					else {
						StringTokenizer pair = new StringTokenizer(line.nextToken(), ":");
						int spalte = Integer.parseInt(pair.nextToken());
						float value = Float.parseFloat(pair.nextToken()); 
						data[row][spalte] =  value;
						column++;
					}
				}
				row++;
			}
			input.close();
		}
		catch(FileNotFoundException e)
		{
			System.out.println("File not found :-(");
		}
		return data;
	}

	public float[] readLabels(File file, String delimiter) {
		int[] dimensions = readDimensionsLibSVM(file, delimiter);
		float[] labels = new float[dimensions[1]];
		int row = 0;
		try
		{
			Scanner input = new Scanner(file);
			while(input.hasNextLine()) {
				StringTokenizer line = new StringTokenizer(input.nextLine(), delimiter);
				labels[row] = Float.parseFloat(line.nextToken());
				row++;
			}
			input.close();
		}
		catch(FileNotFoundException e)
		{
			System.out.println("File not found :-(");
		}
		return labels;
	}
}
