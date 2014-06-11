package de.ismll.blocknewton;

import gnu.trove.map.hash.TIntObjectHashMap;

public class Newton {

	double[] diagHessian;
	double[] invDiagHessian;

	double[] gradientDifference;

	double[] parametersDifference;

	double rValue;

	double eigenvalue;

	TIntObjectHashMap<double[][]> invHessian;

	TIntObjectHashMap<double[][]> hessian;

	TIntObjectHashMap<int[]> pairs;

	int[] shuffle;

	public void initializeShuffle() {
		shuffle = new int[Hyperparameters.variables];
	}

	public void setShuffle() {
		for (int shuffleIdx = 0; shuffleIdx < shuffle.length - 1 ; shuffleIdx++)
		{
			shuffle[shuffleIdx] = shuffleIdx + 1;
		}

		shuffle[shuffle.length] = 0;
	}





	public void initializeDiagHessian() {
		diagHessian = new double[Hyperparameters.variables];       
	}


	public void initializeInvDiagHessian() {
		invDiagHessian = new double[Hyperparameters.variables];
	}

	public void setInvDiagHessian(double eigenvalue) {
		for( int dimension = 0; dimension < invDiagHessian.length ; dimension++)
		{
			invDiagHessian[dimension] = 1/eigenvalue;
		}
	}

	public void initializeInvHessian() {
		invHessian = new TIntObjectHashMap<double[][]>();
	}

	public void initializeHessian() {
		hessian = new TIntObjectHashMap<double[][]>();
	}

	public TIntObjectHashMap<int[]> getPairs(int[] shuffle)
	{
		pairs = new TIntObjectHashMap<int[]>(); 

		if(shuffle.length%2 == 0)
		{
			for (int i = 0; i < shuffle.length/2 ; i++)
			{
				int j = 2*i;
				int[] pair = new int[2];
				pair[0] = shuffle[j];
				pair[1] = shuffle[j+1];
				pairs.put(i, pair);
			}
		}
		else
		{
			for (int i = 0; i < (shuffle.length-1)/2 ; i++)
			{
				int j = 2*i;
				int[] pair = new int[2];
				pair[0] = shuffle[j];
				pair[1] = shuffle[j+1];
				pairs.put(i, pair);
			}
			int lastIndex = (shuffle.length-1)/2;
			int[] lastpair = new int[1];
			lastpair[0] = shuffle.length -1;
			pairs.put(lastIndex, lastpair);
		}

		return pairs;
	}


	public void computeInverseHessians(Data data, Parameters params) {

		switch (Hyperparameters.newtonType)
		{


		///////////////////////////////////////////////////////////////////////////////////////////////////////////



		case "diagonal": // Hesse Elemente analytisch berechnen und invertieren!

			switch (Hyperparameters.lossType)
			{

			case "squaredLoss":

				for (int instanceIdx = 0 ; instanceIdx < params.batch.length ; instanceIdx++) 
				{
					int instance = params.batch[instanceIdx];
					int[] keys = data.values[instance].keys();

					for (int dimension = 0; dimension < keys.length ; dimension++)
					{
						int instNonzero = keys[dimension];

						diagHessian[instNonzero] += params.parameters[instNonzero]*params.parameters[instNonzero];
					}

				}

				for (int dimension = 0; dimension < diagHessian.length ; dimension++)
				{
					invDiagHessian[dimension] = 1/diagHessian[dimension];
				}

				break;

			case "logisticLoss":

				for (int instanceIdx = 0 ; instanceIdx < params.batch.length ; instanceIdx++) 
				{
					int instance = params.batch[instanceIdx];
					int[] keys = data.values[instance].keys();

					double vecProd = 0;

					for (int dimension = 0; dimension < keys.length ; dimension++)
					{
						int instNonzero = keys[dimension];
						vecProd += params.parameters[instNonzero]*data.values[instance].get(instNonzero);
					}

					double exponentialTerm = Math.exp(data.labels[instance]*vecProd);

					for (int dimension = 0; dimension < keys.length ; dimension++)
					{
						int instNonzero = keys[dimension];
						diagHessian[instNonzero] += (exponentialTerm)/((1+exponentialTerm)*(1+exponentialTerm))*params.parameters[instNonzero]*params.parameters[instNonzero];
					}	

				}

				for (int dimension = 0; dimension < diagHessian.length ; dimension++)
				{
					invDiagHessian[dimension] = 1/diagHessian[dimension];
				}

				break;

			case "hingeLoss":

				System.out.println("HingeLoss is not twice differentiable :-(");

				break;

			}

			break;



			/////////////////////////////////////////////////////////////////////////////////////////	



		case "blockDiagonal":  //this is only 2x2 since inverting matrices SUCKS ASS!

			switch (Hyperparameters.lossType)
			{

			case "squaredLoss":

				int[] pairkeys = pairs.keys();

				for (int instanceIdx = 0; instanceIdx < params.batch.length ; instanceIdx++)
				{
					int instance = params.batch[instanceIdx];

					for (int keyIdx = 0; keyIdx < pairkeys.length ; keyIdx++)
					{
						int key = pairkeys[keyIdx];

						if(pairs.get(key).length == 2)
						{

							int coord1 = pairs.get(key)[0];
							int coord2 = pairs.get(key)[1];

							double[][] hessianValues = new double[2][2];

							hessianValues[0][0] = data.values[instance].get(coord1)*data.values[instance].get(coord1);
							hessianValues[0][1]	= data.values[instance].get(coord2)*data.values[instance].get(coord1);
							hessianValues[1][0] = hessianValues[0][1];
							hessianValues[0][0] = data.values[instance].get(coord2)*data.values[instance].get(coord2);

							if (hessian.containsKey(key))
							{
								for (int col = 0; col < hessianValues.length ; col++)
								{
									for (int row = 0; row < hessianValues.length ; row++)
									{
										hessianValues[row][col] += hessian.get(key)[row][col];
									}
								}
								hessian.put(key, hessianValues);
							}
							else
							{
								hessian.put(key, hessianValues);
							}
						}
						else if(pairs.get(key).length == 1)
						{
							int coord1 = pairs.get(key)[0];

							double[][] hessianValues = new double[1][1];

							hessianValues[0][0] = data.values[instance].get(coord1)*data.values[instance].get(coord1);


							if (hessian.containsKey(key))
							{
								hessianValues[0][0] += hessian.get(key)[0][0];
								hessian.put(key, hessianValues);
							}
							else
							{
								hessian.put(key, hessianValues);
							}
						}
					}	
				}

				break;



			case "logisticLoss":

				int[] pairkeys2 = pairs.keys();

				for (int instanceIdx = 0; instanceIdx < params.batch.length ; instanceIdx++)
				{
					int instance = params.batch[instanceIdx];

					int[] datakeys = data.values[instance].keys();

					double vecProd = 0;

					for (int dataKeyIdx = 0 ; dataKeyIdx < datakeys.length ; dataKeyIdx++)
					{
						int instNonzero = datakeys[dataKeyIdx];

						vecProd += params.parameters[instNonzero]*data.values[instance].get(instNonzero);					
					}

					double exponentialTerm = Math.exp(data.labels[instance]*vecProd);
					double exponentialDenominator = (1 + exponentialTerm)*(1 + exponentialTerm);


					for (int keyIdx = 0; keyIdx < pairkeys2.length ; keyIdx++)
					{
						int key = pairkeys2[keyIdx];

						if (pairs.get(key).length == 2)
						{

							int coord1 = pairs.get(key)[0];
							int coord2 = pairs.get(key)[1];

							double[][] hessianValues = new double[2][2];

							hessianValues[0][0] = data.values[instance].get(coord1)*data.values[instance].get(coord1)*(exponentialTerm/exponentialDenominator);
							hessianValues[0][1]	= data.values[instance].get(coord2)*data.values[instance].get(coord1)*(exponentialTerm/exponentialDenominator);
							hessianValues[1][0] = hessianValues[0][1];
							hessianValues[0][0] = data.values[instance].get(coord2)*data.values[instance].get(coord2)*(exponentialTerm/exponentialDenominator);

							if (hessian.containsKey(key))
							{
								for (int col = 0; col < hessianValues.length ; col++)
								{
									for (int row = 0; row < hessianValues.length ; row++)
									{
										hessianValues[row][col] += hessian.get(key)[row][col];
									}
								}
								hessian.put(key, hessianValues);
							}
							else
							{
								hessian.put(key, hessianValues);
							}
						}
						else if (pairs.get(key).length == 1)
						{
							int coord1 = pairs.get(key)[0];

							double[][] hessianValues = new double[1][1];

							hessianValues[0][0] = data.values[instance].get(coord1)*data.values[instance].get(coord1)*(exponentialTerm/exponentialDenominator);


							if (hessian.containsKey(key))
							{
								hessianValues[0][0] += hessian.get(key)[0][0];
								hessian.put(key, hessianValues);
							}
							else
							{
								hessian.put(key, hessianValues);
							}
						}
					}

				}

				break;


			case "hingeLoss":

				System.out.println("HingeLoss is not twice differentiable :-(");

				break;




			}












		default:
			System.out.println("1. diagonal  2. blockDiagonal");
			break;




		}


		//invHessian berechnen!!

		int[] keys = pairs.keys();

		for (int keyIdx = 0; keyIdx < keys.length ; keyIdx++)
		{
			int key = keys[keyIdx];

			if (pairs.get(key).length == 2)
			{
				double[][] invHessianValues = new double[2][2];

				double determinant = (hessian.get(key)[0][0]*hessian.get(key)[1][1])-(hessian.get(key)[0][1]*hessian.get(key)[1][0]);

				invHessianValues[0][0] = -hessian.get(key)[1][1]/determinant;
				invHessianValues[1][0] = -hessian.get(key)[1][0]/determinant;
				invHessianValues[0][1] = -hessian.get(key)[0][1]/determinant;
				invHessianValues[1][1] = -hessian.get(key)[0][0]/determinant;

				invHessian.put(key, invHessianValues);
			}
			else if (pairs.get(key).length == 1)
			{
				double[][]	invHessianValues = new double[1][1];

				invHessianValues[0][0] = 1/(hessian.get(key)[0][0]);
			}

		}



	}

	public void saveOldParameters(Parameters params, Gradient grad) {
		for (int dimension = 0; dimension < params.parameters.length ; dimension++)
		{
			params.parametersOld[dimension] = params.parameters[dimension];
			grad.gradient_old[dimension] = grad.gradient[dimension];
		}
	}

	public void computeSGDQNHessians(Data data, Parameters params, Gradient grad) {


		grad.computeGradient(data, params);



		for (int dimension = 0; dimension < params.parameters.length; dimension++)
		{
			gradientDifference[dimension] = grad.gradient[dimension] - grad.gradient_old[dimension];
			parametersDifference[dimension] = params.parameters[dimension] - params.parametersOld[dimension];
		}

		for (int dimension = 0; dimension < params.parameters.length; dimension++)
		{
			invDiagHessian[dimension] += (2/rValue)*((parametersDifference[dimension]/gradientDifference[dimension]) - invDiagHessian[dimension]);
			invDiagHessian[dimension] = Math.max(invDiagHessian[dimension], 1/(eigenvalue*100));	
		}




	}

	public void computeMYHessians(Data data, Parameters params, Gradient grad) {

	}
}
