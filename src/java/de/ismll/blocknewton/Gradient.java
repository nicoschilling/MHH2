package de.ismll.blocknewton;


public class Gradient {

	double[] gradient;

	double[] smoothGradient;

	double[] gradient_old;
	
	double[] lossGradient;
	
	public void initializeLossGradient() {
		lossGradient = new double[Hyperparameters.variables];
	}


	public void initializeSmoothGradient() {
		smoothGradient = new double[Hyperparameters.variables];
	}

	public void initializeGradient() {
		gradient = new double[Hyperparameters.variables];
	}

	public double[] getGradient() {
		return gradient;
	}

	public double computeGradientNorm() {
		double norm = 0;
		for (int dimension = 0; dimension < gradient.length ; dimension++)
		{
			norm += gradient[dimension]*gradient[dimension];
		}
		return norm;
	}

	public static double computeSigmoid(double value) {
		double result;
		result = (1/(1 + Math.exp(-value)));
		return result;
	}

	public static double computeSquaredSigmoid(double value) {
		double result;
		result = (Math.exp(-value))/((1 + Math.exp(-value))*(1 + Math.exp(-value)));
		return result;
	}

	public void computeLaplacianGradient(Data data, Parameters params, int windowSize) {

		initializeGradient();
		initializeSmoothGradient();
		initializeLossGradient();

		switch (Hyperparameters.lossType)
		{

		case "logisticLoss":

			for (int instanceIdx = 0 ; instanceIdx < params.batch.length ; instanceIdx++)
			{
				int instance = params.batch[instanceIdx];

				double vecProd = 0;

				for (int dimension = 0; dimension < params.parameters.length ; dimension++)
				{
					vecProd += params.parameters[dimension]*data.array[instance][dimension];
				}
				for (int dimension = 0; dimension < params.parameters.length ; dimension++)
				{
					lossGradient[dimension] += (-data.labels[instance]*data.array[instance][dimension]/
							(1+Math.exp(data.labels[instance]*vecProd)));

				}


			}


			for (int instanceIdx = 0 ; instanceIdx < params.smoothBatch.length ; instanceIdx++)
			{
				int instance = params.smoothBatch[instanceIdx];

				double vecProd = 0;

				for (int dimension = 0; dimension < params.parameters.length ; dimension++)
				{
					vecProd += params.parameters[dimension]*data.array[instance][dimension];
				}

				double instSigmoid = computeSigmoid(vecProd);
				double instSigmoidSquared = computeSquaredSigmoid(vecProd);

				int leftSide = instance - windowSize;
				int rightSide = instance + windowSize;

				double scalarProduct;


				if (leftSide >= 0 && rightSide < Hyperparameters.trainInstances)  // ALLES OKAY!!
				{

					for ( int sampleIdx = -windowSize; sampleIdx <= windowSize ; sampleIdx++)
					{
						scalarProduct = 0;
						
						int neighbourInstance = instance + sampleIdx;
						
						for (int dimension = 0; dimension < params.parameters.length; dimension++)
						{
							scalarProduct += params.parameters[dimension]*data.array[neighbourInstance][dimension];
						}
						
						double neighbourSigmoid = computeSigmoid(scalarProduct);
						double neighbourSigmoidSquared = computeSquaredSigmoid(scalarProduct);
						
						
						for (int gradDim = 0; gradDim < params.parameters.length ; gradDim++)
						{
							
							double innerGradientValue = (instSigmoid - neighbourSigmoid)*(data.array[instance][gradDim]
									*instSigmoidSquared - data.array[neighbourInstance][gradDim]*neighbourSigmoidSquared);

							smoothGradient[gradDim] += innerGradientValue;	


						}

					}

				}
				else if (leftSide < 0 && rightSide < Hyperparameters.trainInstances) // LINKS ZU KURZ!!
				{
					int leftSide2 = windowSize + leftSide;
					for ( int sampleIdx = -leftSide2; sampleIdx <= windowSize ; sampleIdx++)
					{
						scalarProduct = 0;
						
						int neighbourInstance = instance + sampleIdx;
						
						for (int dimension = 0; dimension < params.parameters.length; dimension++)
						{
							scalarProduct += params.parameters[dimension]*data.array[neighbourInstance][dimension];
						}
						
						double neighbourSigmoid = computeSigmoid(scalarProduct);
						double neighbourSigmoidSquared = computeSquaredSigmoid(scalarProduct);
						
						
						for (int gradDim = 0; gradDim < params.parameters.length ; gradDim++)
						{
							
							double innerGradientValue = (instSigmoid - neighbourSigmoid)*(-data.array[instance][gradDim]*instSigmoidSquared + data.array[neighbourInstance][gradDim]*neighbourSigmoidSquared);

							smoothGradient[gradDim] += innerGradientValue;	


						}

					}
				}
				else if (leftSide >= 0 &&  rightSide >= Hyperparameters.trainInstances) //  RECHTS ZU KURZ!!
				{
					int lastIndex = Hyperparameters.trainInstances - 1;
					int rightSide2 = (lastIndex - instance);
					for ( int sampleIdx = -windowSize; sampleIdx <= rightSide2 ; sampleIdx++)
					{
						scalarProduct = 0;
						
						int neighbourInstance = instance + sampleIdx;
						
						for (int dimension = 0; dimension < params.parameters.length; dimension++)
						{
							scalarProduct += params.parameters[dimension]*data.array[neighbourInstance][dimension];
						}
						
						double neighbourSigmoid = computeSigmoid(scalarProduct);
						double neighbourSigmoidSquared = computeSquaredSigmoid(scalarProduct);
						
						
						for (int gradDim = 0; gradDim < params.parameters.length ; gradDim++)
						{
							
							double innerGradientValue = (instSigmoid - neighbourSigmoid)*(-data.array[instance][gradDim]*instSigmoidSquared + data.array[neighbourInstance][gradDim]*neighbourSigmoidSquared);

							smoothGradient[gradDim] += innerGradientValue;	


						}

					}
				}
				else
				{
					System.out.println("Window Size exceeds Data Size!");
				}


			}

			break;

		default:

			System.out.println("No other cases yet!!");
			break;

		}
		
		for (int i = 0; i < params.parameters.length ; i++)
		{
			gradient[i] = lossGradient[i] + Hyperparameters.smoothReg*smoothGradient[i];
		}

		// Regularization!!! dimension starts at 1 because no bias regularization!!!

		for (int dimension = 1 ; dimension < gradient.length ; dimension++)
		{
			gradient[dimension] += Hyperparameters.lambda*params.parameters[dimension];
		}

	}

	public void computeGradient(Data data, Parameters params) {

		initializeGradient();

		switch (Hyperparameters.dataType)
		{

		case "sparse":



			switch (Hyperparameters.lossType)
			{

			case "logisticLoss":

				for (int instanceIdx = 0 ; instanceIdx < params.batch.length ; instanceIdx++) 
				{
					int instance = params.batch[instanceIdx];
					double vecProd = 0;
					int[] keys = data.values[instance].keys();

					for (int dimension = 0; dimension < keys.length ; dimension++ ) 
					{
						int instNonzero = keys[dimension];
						vecProd += params.parameters[instNonzero]*data.values[instance].get(instNonzero);
					}

					for (int dimension = 0; dimension < keys.length; dimension++)
					{
						int instNonzero = keys[dimension];
						gradient[instNonzero] += (-data.labels[instance]*data.values[instance].get(instNonzero)/(1+Math.exp(data.labels[instance]*vecProd)));
					} 
				}
				break;

			case "squaredLoss":

				for (int instanceIdx = 0 ; instanceIdx < params.batch.length ; instanceIdx++) 
				{
					int instance = params.batch[instanceIdx];
					double vecProd = 0;
					int[] keys = data.values[instance].keys();

					for (int dimension = 0; dimension < keys.length ; dimension++ ) 
					{
						int instNonzero = keys[dimension];
						vecProd += params.parameters[instNonzero]*data.values[instance].get(instNonzero);
					}

					for (int dimension = 0; dimension < keys.length; dimension++)
					{
						int instNonzero = keys[dimension];
						gradient[instNonzero] += (vecProd - data.labels[instance])*(vecProd - data.labels[instance]);
					} 
				}
				break;

			case "hingeLoss":

				for (int instanceIdx = 0 ; instanceIdx < params.batch.length ; instanceIdx++) 
				{
					int instance = params.batch[instanceIdx];

					double vecProd = 0;
					int[] keys = data.values[instance].keys();

					for (int dimension = 0; dimension < keys.length ; dimension++ ) 
					{
						int instNonzero = keys[dimension];
						vecProd += params.parameters[instNonzero]*data.values[instance].get(instNonzero);
					}

					if (data.labels[instance] == 1)
					{
						if ( vecProd >= 1)
						{
							// Nothing happens because the gradient is zero in every dimension!
						}
						else
						{
							for (int dimension = 0; dimension < keys.length; dimension++)
							{
								gradient[dimension] -= params.parameters[dimension];
							}
						}
					}
					else if (data.labels[instance] == -1)
					{
						if (vecProd <= -1)
						{
							// Nothing happens because the gradient is zero in every dimension!
						}
						else
						{
							for (int dimension = 0; dimension < keys.length; dimension++)
							{
								gradient[dimension] += params.parameters[dimension];
							}
						}
					}
					else
					{
						System.out.println("Labels should be 1 or -1!!!");
					}

				}
				break;


			default:
				System.out.println("1. squaredLoss  2. logisticLoss 3. hingeLoss");
				break;



			}

			break;


		case "dense":

			switch (Hyperparameters.lossType)
			{

			case "logisticLoss":

				for (int instanceIdx = 0 ; instanceIdx < params.batch.length ; instanceIdx++)
				{
					int instance = params.batch[instanceIdx];

					double vecProd = 0;

					for (int dimension = 0; dimension < params.parameters.length ; dimension++)
					{
						vecProd += params.parameters[dimension]*data.array[instance][dimension];
					}
					for (int dimension = 0; dimension < params.parameters.length ; dimension++)
					{
						gradient[dimension] += (-data.labels[instance]*data.array[instance][dimension]/(1+Math.exp(data.labels[instance]*vecProd)));

					}


				}



				break;


			case "squaredLoss":


				break;


			case "hingeLoss":



				break;



			}


			break;

		}




		// Regularization!!! dimension starts at 1 because no bias regularization!!!

		for (int dimension = 1 ; dimension < gradient.length ; dimension++)
		{
			gradient[dimension] += Hyperparameters.lambda*params.parameters[dimension];
		}




	}


}
