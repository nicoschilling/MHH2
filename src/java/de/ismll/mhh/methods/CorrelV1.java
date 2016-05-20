package de.ismll.mhh.methods;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.bootstrap.Parameter;
import de.ismll.console.Generic;
import de.ismll.distance.ChebyshevDistance;
import de.ismll.distance.ChiSquare;
import de.ismll.distance.Correlation;
import de.ismll.distance.CosineSimilarity;
import de.ismll.distance.Covariance;
import de.ismll.distance.IDistanceMeasure;
import de.ismll.distance.EuclideanDistanceMeasure;
import de.ismll.distance.JeffreyDivergence;
import de.ismll.distance.MinkowskiMeasure;
import de.ismll.mhh.io.Parser;
import de.ismll.table.IntVector;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vectors;
import de.ismll.table.Vector;
import de.ismll.table.impl.DefaultIntVector;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.impl.DefaultVector;
import de.ismll.table.projections.VectorSubset;
import de.ismll.utilities.Tools;

public class CorrelV1 implements Runnable {

	private static final String FFT_FILENAME = "result.csv";
	@Parameter(cmdline="factor")
	private float correlFactor;
	@Parameter(cmdline="input")
	private File input;
	@Parameter(cmdline="output")
	private File output;
	
	Logger log = LogManager.getLogger(getClass());

	@Override
	public void run() {
		Matrix m;
		
		String samplerateS;
		String rdstartS;
		String rdendS;
		
		try {
			m = Parser.readCSVFile(new File(input, FFT_FILENAME));
			rdstartS = Parser.readFileCompletely(new File(input, "rdstart"));
			rdendS = Parser.readFileCompletely(new File(input, "rdend"));
			samplerateS = Parser.readFileCompletely(new File(input, "samplerate"));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		
		int firstSample = (int)m.get(0, 0);
		int lastSample = (int)m.get(m.getNumRows()-1, 0);
		log.info("Erstes Sample in der Datei ist " + firstSample);
		
		int samplerate = Integer.parseInt(samplerateS.trim());
		log.info("Samplerate ist " + samplerate);
		
		int rdStartTime = Parser.time2Sample(rdstartS.trim());
		int rdStartSample = rdStartTime * samplerate; 
		log.info("Ruhedruck faengt an bei Zeit " + rdStartTime + " (Sample: " + rdStartSample + ", relativ: " + (rdStartSample-firstSample) + ")");
		int rdEndTime = Parser.time2Sample(rdendS.trim());
		int rdEndSample = rdEndTime * samplerate; 
		log.info("Ruhedruck hoert auf bei Zeit " + rdEndTime + " (Sample: " + rdEndSample + "), relativ: " + (rdEndSample-firstSample) + "");
		
		int numFFTbins = 32;
		
		
		
		IntVector trainSelector = new DefaultIntVector(rdEndSample-rdStartSample+1);
		IntVector dataSelector = new DefaultIntVector(numFFTbins);
		
		for (int r = rdStartSample; r <=rdEndSample; r++) {
			trainSelector.set(r-rdStartSample, r-firstSample);
		}
		Matrix train = Matrices.rows(m, trainSelector, false);
		
		Vector avgTrain = new DefaultVector(numFFTbins);
		Vector varTrain = new DefaultVector(numFFTbins);
		for (int c = 1; c < 1+numFFTbins; c++) {
			
			double avgCol = Vectors.avg(Matrices.col(train,c));
			double varCol = Vectors.var(Matrices.col(train,c));
			avgTrain.set(c-1, (float) avgCol);
			varTrain.set(c-1, (float) varCol);
			
//			avgTrain.set(c-1, Matrices.maxCol(train, c));
			
			dataSelector.set(c-1, c);
		}
//		log.info("Average on training proportion is " + Vectors.toString(avgTrain));
//		log.info("Variance on training proportion is " + Vectors.toString(varTrain));
		
		int startAnalysis = rdEndSample;
		startAnalysis=firstSample;
		
		Matrix results = new DefaultMatrix(lastSample-startAnalysis-1, 2);
		IDistanceMeasure dm = new CosineSimilarity();
		
		
		for (int r = startAnalysis+1; r < lastSample; r++) {
			Vector currentRow = Matrices.row(m, r-firstSample);
			VectorSubset data = new VectorSubset(currentRow, dataSelector);
			
			double correl = Vectors.correlation(data, avgTrain);
			correl = dm.distance(avgTrain, data);
			
			results.set(r-startAnalysis-1, 0, m.get(r-firstSample, 0));// copy PK
			results.set(r-startAnalysis-1, 1, (float) correl);// 
		}
		
		try {
			Matrices.write(results, output, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public File getOutput() {
		return output;
	}


	public void setOutput(File output) {
		this.output = output;
	}


	public File getInput() {
		return input;
	}


	public void setInput(File input) {
		this.input = input;
	}


	public float getCorrelFactor() {
		return correlFactor;
	}


	public void setCorrelFactor(float correlFactor) {
		this.correlFactor = correlFactor;
	}

}
