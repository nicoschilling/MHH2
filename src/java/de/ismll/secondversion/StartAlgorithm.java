package de.ismll.secondversion;

import de.ismll.bootstrap.CommandLineParser;
import de.ismll.table.IntVector;
import de.ismll.table.projections.ColumnSubsetMatrixView;

public class StartAlgorithm {

	public static void main(String[] args) {
		// Modell trainieren
		// schreibt Modelle in Datenbank.
		
		System.out.println("System starting!!!");

		AlgorithmController algcon = new AlgorithmController();

		System.out.println("Algorithm Controller initated!!");

		CommandLineParser.parseCommandLine(args, algcon);

		System.out.println("Command Line has been parsed! Will run Algcon now...");

//ColumnSubsetMatrixView v;
//v.get(5,4);

		algcon.run();

	}

}
