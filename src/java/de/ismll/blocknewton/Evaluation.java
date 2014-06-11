package de.ismll.blocknewton;

public class Evaluation {


	int[] predictedLabels;
	float[] sigmoidLabels;

	public double computeAccuracy(Data data) {

		double accuracy;
		int correctClass = 0;

		for (int instance = 0; instance < data.labels.length ; instance++ )
		{
			if(data.labels[instance] == predictedLabels[instance])
			{
				correctClass++;
			}


		}

		accuracy = (double) (correctClass)/( (double) data.labels.length);

		return accuracy;
	}

	public int countPositive() {
		int count = 0;
		for (int instance = 0; instance < predictedLabels.length ; instance++)
		{
			if(predictedLabels[instance] == 1)
			{
				count++;
			}

		}
		return count;
	}

	public void predictLabels(Data data, Parameters params) {
		
		predictedLabels = new int[data.labels.length];
		

		switch (Hyperparameters.dataType)
		{

		case "sparse":

			for (int instance = 0; instance < data.labels.length ; instance++)
			{
				double vecProd = 0;

				int[] keys = data.values[instance].keys();

				for (int dimension = 0; dimension < keys.length ; dimension ++)
				{
					int instNonzero = keys[dimension];

					vecProd += params.parameters[instNonzero]*data.values[instance].get(instNonzero);
				}

				if(vecProd >= 0)
				{
					predictedLabels[instance] = 1;
				}
				else
				{
					predictedLabels[instance] = -1;
				}

			}

			break;

		case "dense":
			
			for (int instance = 0; instance < data.labels.length ; instance++)
			{
				double vecProd = 0;
				
				for (int dimension = 0; dimension < params.parameters.length; dimension++)
				{
					vecProd += params.parameters[dimension]*data.array[instance][dimension];
				}
				if(vecProd >= 0)
				{
					predictedLabels[instance] = 1;
				}
				else
				{
					predictedLabels[instance] = -1;
				}
			}

			break;
		}
	}
	
	public void predictSigmoidLabels(Data data, Parameters params) {
		sigmoidLabels = new float[data.labels.length];
		

		for (int instance = 0; instance < data.labels.length ; instance++)
		{
			double vecProd = 0;
			
			for (int dimension = 0; dimension < params.parameters.length; dimension++)
			{
				vecProd += params.parameters[dimension]*data.array[instance][dimension];
			}
			
			float sigmoidLabel = (float) (1/(1+Math.exp(-vecProd)));
			
			sigmoidLabels[instance] = sigmoidLabel;
			
		}
		
		
	}

	public double computeLoss(Data data, Parameters params) {

		double loss = 0;

		switch (Hyperparameters.lossType)
		{
		case "logisticLoss":

			for (int instance = 0; instance < data.labels.length ; instance++)
			{
				double vecProd = 0;

				int[] keys = data.values[instance].keys();

				for (int dimension = 0; dimension < keys.length ; dimension++)
				{
					int instNonzero = keys[dimension];
					vecProd += params.parameters[instNonzero]*data.values[instance].get(instNonzero);
				}

				loss += Math.log(1 + Math.exp(-data.labels[instance]*vecProd));
			}

			loss = loss/data.labels.length;

			break;

		case "squaredLoss":

			for (int instance = 0; instance < data.labels.length ; instance++)
			{
				double vecProd = 0;

				int[] keys = data.values[instance].keys();

				for (int dimension = 0; dimension < keys.length ; dimension++)
				{
					int instNonzero = keys[dimension];
					vecProd += params.parameters[instNonzero]*data.values[instance].get(instNonzero);
				}

				loss += (vecProd - data.labels[instance])*(vecProd - data.labels[instance]);

			}

			loss = Math.sqrt(loss/data.labels.length);

			break;

		default:
			System.out.println("1. squaredLoss  2. logisticLoss");
			break;
		}

		return loss;

	}

}
