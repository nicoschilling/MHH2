package de.ismll.blocknewton;

import java.io.File;
import java.io.IOException;

import de.ismll.bootstrap.CommandLineParser;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.impl.DefaultMatrix;

public class SampleLabelAverage {

	
	public static void main(String[] args) throws IOException {
		
		Hyperparameters hyper = new Hyperparameters();
		CommandLineParser.parseCommandLine(args, hyper);
		
		IO input = new IO();
		
		Data data = new Data();
		
		input.readSample2Data(Hyperparameters.sample2Label, data);
		
		data.initializeSample2avgLabel(data.sample2label.length);
		
		data.computeSample2avgLabel(Hyperparameters.windowExtent);
		
		Matrix sample2avgLabel = DefaultMatrix.wrap(data.sample2avgLabel);
		
//		Matrices.write(sample2avgLabel, new File("/home/nico/testresult"), false);
		
		float[][] annotation = new float[1][1];
		annotation[0][0] = data.predictAnnotation();
		
		Matrix annotationMatrix = DefaultMatrix.wrap(annotation);
		
		Matrices.write(sample2avgLabel, new File("/home/nico/Documents/MHH/prediction"), false);
		
		
	}

}
