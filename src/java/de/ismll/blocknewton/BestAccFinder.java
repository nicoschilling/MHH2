package de.ismll.blocknewton;

import java.io.File;
import java.io.IOException;

import de.ismll.bootstrap.CommandLineParser;
import de.ismll.table.Matrix;
import de.ismll.table.io.CSV;

public class BestAccFinder {


	public static void main(String[] args) throws IOException {
		
		Hyperparameters hyper = new Hyperparameters();
		CommandLineParser.parseCommandLine(args, hyper);
		
		System.out.println(Hyperparameters.fileDir);
		
		File[] files = new File(Hyperparameters.fileDir).listFiles();
		
		for (int i = 0; i < files.length ; i++)
		{
			System.out.println(files[i].getAbsolutePath());
		}
		
		float accuracy = 0;
		float bestAccuracy = 0;
		@SuppressWarnings("unused")
		String filename = null;
		
		for (File file : files)
		{
			Matrix accuracyMatrix = CSV.read(file);
			accuracy = accuracyMatrix.get(0, 0);
			
			if (accuracy > bestAccuracy)
			{
				bestAccuracy = accuracy;
				filename = file.getAbsolutePath();
			}
			
		}
		
		System.out.println(filename);


	}

}
