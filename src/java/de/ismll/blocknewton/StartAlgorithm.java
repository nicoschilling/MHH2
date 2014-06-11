package de.ismll.blocknewton;

import de.ismll.bootstrap.CommandLineParser;

public class StartAlgorithm {

	public static void main(String[] args) {


		Hyperparameters hyper = new Hyperparameters();
		CommandLineParser.parseCommandLine(args, hyper);
		
		IO input = new IO();
		
		Data data = new Data();
		
		Parameters params = new Parameters();
		
		Gradient grad = new Gradient();
		
		Newton newton = new Newton();
		
		Evaluation eval = new Evaluation();
		
		input.readSparseData(Hyperparameters.trainfile, data);
		
		params.setExpectationValue(0);
		params.setVariance(1);
		
		params.initializeParameters();
		
		for (int update=0; update < Hyperparameters.maxIterations ; update++)
		{
			params.computeRandomBatch();
			grad.computeGradient(data, params);
			
			if (Hyperparameters.newtonType == "diagonal")
			{
				
			}
			else if(Hyperparameters.newtonType == "blockDiagonal")
			{
				
			}
			
			params.updateParameters(data, grad, newton, update);
			eval.predictLabels(data, params);
			System.out.println("Accuracy at Iteration "+update+": " + eval.computeAccuracy(data));
			
//			for (int k = 0; k < params.parameters.length ; k++)
//			{
//				System.out.println(grad.gradient[k]);
//			}
			
			
		}
		
		





	}

}
