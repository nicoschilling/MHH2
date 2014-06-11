package de.ismll.modelFunctions;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		ModelFunctions model = new LinearRegressionPrediction();
		
		if (model.getClass() == FmModel.class) {
			System.out.println("Ja wir haben ein FM Modell");
		}
		else if (model.getClass() == LinearRegressionPrediction.class) {
			System.out.println("Wir haben nur ne lineare regression....");
		}
		

	}

}
