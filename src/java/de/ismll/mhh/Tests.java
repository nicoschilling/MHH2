package de.ismll.mhh;

import java.io.File;

import de.ismll.mhh.io.Parser;
import de.ismll.mhh.io.DataInterpretation;
import de.ismll.utilities.Tools;

public class Tests {
	
	public static void main(String[] args) {
		File dataDir = new File("/home/nico/Documents/MHH"); // root-Verzeichnis; auf dem Server /home/mhh
		
		System.out.println("Datenverzeichnis Proband 1, Schluck 1:\t" + Probanden.Proband1.expand(dataDir, 1));
		System.out.println("Datenverzeichnis Proband 1, Schluck 5:\t" + Probanden.Proband1.expand(dataDir, 5));
		
		System.out.println("Michael ist immer:\t" + People.Michael.kuerzel);
	
		
		
		DataInterpretation rf = new DataInterpretation();
		rf.setDataInterpretation(Probanden.Proband1.expand(dataDir, 5));
		rf.run();
		System.out.println("Training Start f�r Proband 1, Schluck 5: " + rf.getRdstart());
		System.out.println("Training Start f�r Proband 1, Schluck 5: " + Parser.time2Sample(rf.getRdstart(), Integer.parseInt(rf.getSamplerate())));
	}
}
