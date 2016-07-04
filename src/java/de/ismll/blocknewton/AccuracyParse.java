package de.ismll.blocknewton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.ReaderConfig;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.utilities.Buffer;


public class AccuracyParse {


	int countSwallow;


	Matrix[] accuracies;

	float[][] overAllAcc = new float[1][2];

	double expectationValue = 0;

	double variance = 0;

	Matrix overAll;

	public void setValues() {
		if (Hyperparameters.proband == 1)
		{
			this.countSwallow = 10;
		}
		else if (Hyperparameters.proband == 2)
		{
			this.countSwallow = 11;
		}

		accuracies = new Matrix[this.countSwallow];

	}

	public void readAccuracies() throws IOException {
		for (int i=0 ; i < countSwallow ; i++)
		{
			try(InputStream newInputStream = Buffer.newInputStream(new File(Hyperparameters.saveDir+"/accuracies_"+i))) {
				accuracies[i] = Matrices.readDense(newInputStream, ReaderConfig.CSV);
			} 

		}
	}

	public void computeExpectationValue() {
		for (int i = 0; i < countSwallow ; i++)
		{
			expectationValue += accuracies[i].get(0, 0);
		}
		expectationValue = expectationValue/countSwallow;

	}

	public void computeVariance() {
		for (int i=0; i < countSwallow ; i++)
		{
			variance += (accuracies[i].get(0, 0) - expectationValue)*(accuracies[i].get(0, 0) - expectationValue);
		}
		variance = Math.sqrt((variance/(countSwallow-1)));

	}


	public void wrapOverAllAcc() {
		overAllAcc[0][0] = (float) expectationValue;
		overAllAcc[0][1] = (float) variance;
		this.overAll = DefaultMatrix.wrap(overAllAcc);
	}

	public void saveAcc() throws IOException {
		Matrices.write(overAll, new File(Hyperparameters.saveDir+"/overAllAccVar"), false);
	}













}
