package de.ismll.secondversion;

public class Calculations {
	
	public static double computeSigmoid(float value) {
		return (1/(1 + Math.exp(-value)));
	}

	public static double computeSquaredSigmoid(float value) {
		double result;
		double exp = Math.exp(-value);
		result = (exp/((1 + exp)*(1 + exp)));
		return result;
	}

}
