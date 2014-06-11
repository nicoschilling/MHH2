package de.ismll.blocknewton;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.StringTokenizer;

import de.ismll.blocknewton.Data;

public class IO {

	int[][] dataSizes;



	public void readSample2Data(String filename, Data data) {	
		//Count the lines;
		int rowNr = 0;

		try
		{
			Scanner input = new Scanner(new File(filename));
			while(input.hasNextLine())
			{
				StringTokenizer line = new StringTokenizer(input.nextLine(), ",");
				@SuppressWarnings("unused")
				int column = 0;
				while(line.hasMoreTokens())
				{
					@SuppressWarnings("unused")
					double dummie = Float.parseFloat(line.nextToken());
					column++;
				}

				rowNr++;

			}

			input.close();
		}
		catch(FileNotFoundException e)
		{
			System.out.println("File not found @ reading sample2Labels");
		}

		data.initializeSample2Label(rowNr);

		int row = 0;
		try
		{
			Scanner input = new Scanner(new File(filename));

			while(input.hasNextLine())
			{
				StringTokenizer line = new StringTokenizer(input.nextLine(), ",");

				int column = 0;

				while(line.hasMoreTokens())
				{
					data.sample2label[row][column] = Float.parseFloat(line.nextToken());
					column++;
				}

				row++;

			}

			input.close();

		}
		catch(FileNotFoundException e)
		{
			System.out.println("File not found @ reading sample2Labels");
		}

	}

	public void readSparseData(String filename, Data data)
	{
		data.initializeHashMap(Hyperparameters.trainInstances);
		data.initializeLabels(Hyperparameters.trainInstances);

		try
		{
			Scanner input = new Scanner(new File(filename));

			int row = 0;

			while(input.hasNextLine())
			{
				StringTokenizer line = new StringTokenizer(input.nextLine());

				int column = 0;

				while(line.hasMoreTokens())
				{
					if(column == 0)
					{
						data.labels[row] = Double.parseDouble(line.nextToken());
					}
					else
					{
						StringTokenizer pair = new StringTokenizer(line.nextToken(), ":");  
						int spalte = Integer.parseInt(pair.nextToken());
						double value = Double.parseDouble(pair.nextToken());
						data.values[row].put(spalte,value);

					}

					column++;

				}
				data.values[row].put(0, 1);    // For Bias Parameter
				row++;

			}

			input.close();
		}
		catch(FileNotFoundException e)
		{
			System.err.println("The File does not exist :-(");
		}
	}


	public void readData(String filename, Data data)
	{




		data.initializeArray(Hyperparameters.trainInstances,Hyperparameters.variables);


		int row = 0;


		try
		{
			Scanner input = new Scanner(new File(filename));

			while(input.hasNextLine())
			{
				StringTokenizer line = new StringTokenizer(input.nextLine());

				int column = 0;

				while(line.hasMoreTokens())
				{
					if(column == 0)
					{
						data.labels[row] = (Integer.parseInt(line.nextToken()));
						column++;
					}
					else
					{
						data.array[row][column-1] =  (Double.parseDouble(line.nextToken())); 
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



	}

	public void readDataSizes(String filename) {

		dataSizes = new int[12][3]; // 10 folds.. 3 variables   11 folds 3 variables proband2

		int row = 0;

		try
		{
			Scanner input = new Scanner(new File(filename));

			while(input.hasNextLine())
			{
				StringTokenizer line = new StringTokenizer(input.nextLine());

				int column = 0;

				while(line.hasMoreTokens())
				{
					dataSizes[row][column] =  (Integer.parseInt(line.nextToken())); 
					column++;
				}
				row++;
			}

			input.close();
		}
		catch(FileNotFoundException e)
		{
			System.out.println("File not found :-( @ reading DataSizeFile");
		}
	}

	public void readSamples(String filename, Data data, String type) {

		switch(type)
		{

		case "train":
			data.initializeSamples(Hyperparameters.trainInstances);

			break;


		case "test":
			data.initializeSamples(Hyperparameters.testInstances);

			break;

		default:
			System.out.println("1. test   2. train");
			break;

		}

		int row = 0;

		try
		{
			Scanner input = new Scanner(new File(filename));

			while(input.hasNextLine())
			{
				StringTokenizer line = new StringTokenizer(input.nextLine(), ",");

				int column = 0;

				while(line.hasMoreTokens())
				{
					if (column == 0)   
					{
						column++;
						data.samples[row] = (int) (Double.parseDouble(line.nextToken()));
					}
					else
					{
						column++;	
						@SuppressWarnings("unused")
						double dummie = (Double.parseDouble(line.nextToken()));
					}
				}

				row++;

			}

			input.close();

		}
		catch(FileNotFoundException e)
		{
			System.out.println("File not found @ reading Samples");
		}



	}

	public void readDenseMHHData(String filename, Data data, String type) {

		switch (type)
		{
		case "train":
			data.initializeArray(Hyperparameters.trainInstances,Hyperparameters.variables);
			break;

		case "test":
			data.initializeArray(Hyperparameters.testInstances,Hyperparameters.variables);
			break;
		}

		int row = 0;


		try
		{
			Scanner input = new Scanner(new File(filename));

			while(input.hasNextLine())
			{
				StringTokenizer line = new StringTokenizer(input.nextLine(), ",");

				int column = 1;

				while(line.hasMoreTokens())
				{
					if (column <= Hyperparameters.variables)
					{
						if (column == 1)   // DIE erste zeile weg, weil nur Sample Index!!!
						{
							column++;
							@SuppressWarnings("unused")
							double dummie = (Double.parseDouble(line.nextToken()));
						}
						else
						{

							data.array[row][column-1] =  (Double.parseDouble(line.nextToken())); 
							column++;

						}
					}
					else
					{
						break;
					}
					
				}

				row++;

			}

			input.close();

			switch (type)
			{

			case "train":

				for (int row2 = 0; row2 < Hyperparameters.trainInstances; row2++)
				{
					data.array[row2][0] = 1;
				}

				break;

			case "test":

				for (int row2 = 0; row2 < Hyperparameters.testInstances; row2++)
				{
					data.array[row2][0] = 1;
				}

				break;

			}


		}
		catch(FileNotFoundException e)
		{
			System.out.println("File not found @ reading DenseData");
		}
	}

	public void readLabels(String filename, Data data, String type) {

		switch (type)
		{
		case "train":
			data.initializeLabels(Hyperparameters.trainInstances);
			break;
		case "test":
			data.initializeLabels(Hyperparameters.testInstances);
			break;
		}

	
		int row = 0;
		try
		{
			Scanner input = new Scanner(new File(filename));
			while(input.hasNextLine())
			{
				StringTokenizer line = new StringTokenizer(input.nextLine());
				while(line.hasMoreTokens())
				{
					data.labels[row] = Double.parseDouble(line.nextToken());
					row++;
				}
			}
			input.close();
		}
		catch(FileNotFoundException e)
		{
			System.out.println("File not found @ reading labels");
		}
	}

}
