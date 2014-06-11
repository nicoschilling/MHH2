package de.ismll.mhh.featureExtractors;

import java.io.File;
import java.io.IOException;

import de.ismll.mhh.io.Parser;
import de.ismll.table.Matrices;
import de.ismll.table.Matrices.FileType;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;

public class TestExtraction {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		int low = 3;
		int high = 5;
		
		AllExtractor extractor = new AllExtractor();
		LowerExtractor lower = new LowerExtractor();
		MiddleExtractor middle = new MiddleExtractor();
		UpperExtractor upper = new UpperExtractor();
		LowerMiddleExtractor lowerMiddle = new LowerMiddleExtractor();
		UpperMiddleExtractor upperMiddle = new UpperMiddleExtractor();
		
		Matrix test = Parser.readCSVFile(new File("/home/nico/Documents/MHH/jobs/job86/data.csv"));
		
		extractor.extractFeatures(test, low, high);
		lower.extractFeatures(test, low, high);
		middle.extractFeatures(test, low, high);
		upper.extractFeatures(test, low, high);
		lowerMiddle.extractFeatures(test, low, high);
		upperMiddle.extractFeatures(test, low, high);
		
		
		

	}

}
