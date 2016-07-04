package de.ismll.blocknewton;

import java.io.IOException;

public class AccuracyStart {

	public static void main(String[] args) throws IOException {
		AccuracyParse accp = new AccuracyParse();
		
		accp.readAccuracies();
		accp.computeExpectationValue();
		accp.computeVariance();
		accp.wrapOverAllAcc();
		accp.saveAcc();

	}

}
