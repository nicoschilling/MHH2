package de.ismll.blocknewton;

import java.util.Random;



public class Parameters {

	double[] parametersBestSoFar;

	double[] parameters;

	double[] parametersOld;

	double variance;

	double expectationValue;

	int[] batch;
	
	int[] smoothBatch;
	
	public void computeSmoothBatch()
	{
		smoothBatch = new int[Hyperparameters.smoothSize];
		for (int dimension = 0; dimension < smoothBatch.length ; dimension++)
		{
			smoothBatch[dimension] = dimension;
//			smoothBatch[dimension] = (int) (Math.random()*Hyperparameters.trainInstances);
		}
	}

	public void initializeParametersBestSoFar() {
		parametersBestSoFar = new double[parameters.length];
		for (int i = 0; i < parameters.length ; i++)
		{
			this.parametersBestSoFar[i] = 0;
		}
	}
	
	public void setParametersBestSoFar(double[] parameters) {
		for (int i = 0; i < parameters.length ; i++)
		{
			this.parametersBestSoFar[i] = parameters[i];
		}
	}

	public void initializeParametersOld() {
		parametersOld = new double[parameters.length];
		for (int i = 0; i < parameters.length ; i++)
		{
			this.parametersOld[i] = 0;
		}
	}

	public void setParameters(double[] parameters) {
		for (int i = 0; i < parameters.length ; i++)
		{
			this.parameters[i] = parameters[i];
		}
	}

	public void setParametersOld(double[] parameters) {
		for (int i = 0; i < parameters.length ; i++)
		{
			this.parametersOld[i] = parameters[i];
		}
	}


	public double getVariance() {
		return variance;
	}

	public void setVariance(double variance) {
		this.variance = variance;
	}

	public double getExpectationValue() {
		return expectationValue;
	}

	public void setExpectationValue(double expectationValue) {
		this.expectationValue = expectationValue;
	}

	public void computeRandomBatch() {
		batch = new int[Hyperparameters.batchSize];

		for (int dimension = 0; dimension < batch.length ; dimension++)
		{
//			batch[dimension] = (int) (Math.random()*Hyperparameters.trainInstances);
			batch[dimension] = dimension;
		}
	}

	public double[] getParameters() {
		return parameters;
	}

	public void initializeParameters() {
		parameters = new double[Hyperparameters.variables];

		Random random = new Random();
		random.setSeed(100);

		for (int dimension = 0; dimension < parameters.length ; dimension++)
		{
			parameters[dimension] = random.nextGaussian()*variance + expectationValue;
		}
	}

	public void updateParameters(Data data, Gradient grad, Newton newton, int update) {

		switch (Hyperparameters.algorithmType)
		{

		case "GradientDescent":

			for (int paramDimension = 0; paramDimension < parameters.length; paramDimension++)
			{
				parameters[paramDimension] -= Hyperparameters.getStepSize(update)*grad.gradient[paramDimension]; 
			}

			break;

		case "BlockNewtonDescent":

			switch(Hyperparameters.newtonType)
			{

			case "diagonal":

				for (int paramDimension = 0; paramDimension < parameters.length ; paramDimension++)
				{
					parameters[paramDimension] -= Hyperparameters.getStepSize(update)*newton.invDiagHessian[paramDimension]*grad.gradient[paramDimension];
				}

				break;

			case "blockDiagonal":

				int[] keys = newton.invHessian.keys();

				for (int keyIdx = 0; keyIdx < keys.length ; keyIdx++)
				{	
					int key = keys[keyIdx];

					if (newton.pairs.get(key).length == 2)
					{

						int coord1 = newton.pairs.get(key)[0];
						int coord2 = newton.pairs.get(key)[1];

						parameters[coord1] -= Hyperparameters.getStepSize(update)*(newton.invHessian.get(key)[0][0]*grad.gradient[coord1] +
								newton.invHessian.get(key)[0][1]*grad.gradient[coord2] );
						parameters[coord2] -= Hyperparameters.getStepSize(update)*(newton.invHessian.get(key)[1][0]*grad.gradient[coord1] +
								newton.invHessian.get(key)[1][1]*grad.gradient[coord2] ); 

					}
					else if (newton.pairs.get(key).length == 1)
					{
						int coord1 = newton.pairs.get(key)[0];

						parameters[coord1] -= Hyperparameters.getStepSize(update)*(newton.invHessian.get(key)[0][0]*grad.gradient[coord1]);
					}

				}



			}


			break;


		default:



			break;





		}

	}

	public void updateSGDQDNParameters(Data data, Parameters params, Gradient grad, Newton newton, int update) {

		// Erster Case normaler SGDQN!!!




	}

}
