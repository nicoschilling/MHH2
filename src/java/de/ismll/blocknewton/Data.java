package de.ismll.blocknewton;

import java.io.File;
import java.io.IOException;

import de.ismll.mhh.io.Parser;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.io.CSV;
import gnu.trove.map.hash.TIntDoubleHashMap;

public class Data {

	float[][] unsmoothWindows;

	double[][] array;
	double[] labels;
	TIntDoubleHashMap[] values; 
	int[] samples;

	float[][] sample2label;

	float[][] sample2avgLabel;

	float[][] sample2Sigmoid;

	float[][] sample2avgSigmoid;

	int[] firstSamples;
	int[] lastSamples;

	int[] trueFirstSamples;
	int[] trueLastSamples;
	
	
	int[] foldFirstSamples;
	int[] foldLastSamples;
	int[] foldPMaxSamples;
	int[] foldAnnotationSamples;
	
	float avgUnSmoothness;
	float varUnSmoothness;
	
	int pMaxSample;

	int[] pMaxSamples;

	Matrix annotations;

	int[] annotationSamples;

	int[] trainAnnotations;

	Matrix firstSampleMatrix;

	Matrix lastSampleMatrix;
	
	Matrix trueFirstSampleMatrix;
	
	Matrix trueLastSampleMatrix;


	double[] variances;
	double[] expectationValues;
	
	public void initializeFoldSamples() {
		this.foldFirstSamples = new int[Hyperparameters.countSwallow];
		this.foldLastSamples = new int[Hyperparameters.countSwallow];
		this.foldPMaxSamples = new int[Hyperparameters.countSwallow];
		this.foldAnnotationSamples = new int[Hyperparameters.countSwallow];
	}
	
	public void setFoldSamples() {
		
		int sampleDifference = 0;

		for (int i = 0; i < Hyperparameters.countSwallow ; i++)
		{


			if (i == Hyperparameters.fold)
			{
				sampleDifference = lastSamples[i] + 1 - firstSamples[i];
				foldFirstSamples[i] = 0;
				foldLastSamples[i] = 0;
				continue;
			}

			foldFirstSamples[i] = firstSamples[i] - sampleDifference;
			foldLastSamples[i] = lastSamples[i] - sampleDifference;
			

		}
		
		for (int i = 0; i < Hyperparameters.countSwallow ; i++)
		{
			if (i == Hyperparameters.fold)
			{
				foldPMaxSamples[i] = 0;
				foldAnnotationSamples[i] = 0;
				continue;
			}
			int diffSamples = pMaxSamples[i] - trueFirstSamples[i];
			int diffSamples2 = annotationSamples[i] - trueFirstSamples[i];

			foldPMaxSamples[i] = foldFirstSamples[i] + diffSamples;
			foldAnnotationSamples[i] = foldFirstSamples[i] + diffSamples2;

		}
		
	}

	public void getAnnotations() throws IOException {
		switch (Hyperparameters.annotator)
		{
		case 0:
			this.annotations = Parser.readAnnotations(new File("/home/"+Hyperparameters.myDir+"/Documents/MHH/manual_annotations/"+Hyperparameters.proband+"-"+"MJ.tsv"), 50);
			break;
		case 1:
			this.annotations = Parser.readAnnotations(new File("/home/"+Hyperparameters.myDir+"/Documents/MHH/manual_annotations/"+Hyperparameters.proband+"-"+"SM.tsv"), 50);
			break;
		case 2:
			this.annotations = Parser.readAnnotations(new File("/home/"+Hyperparameters.myDir+"/Documents/MHH/manual_annotations/"+Hyperparameters.proband+"-"+"gemein.tsv"), 50);
			break;
		default:
			System.out.println("Annotator is wrongly chosen");
			break;
		}
	}

	public void getAnnotationSamples() {
		initializeAnnotationSamples();
		for (int i = 0; i < Hyperparameters.countSwallow ; i++)
		{
			annotationSamples[i] = (int) annotations.get(i, Parser.ANNOTATION_COL_RESTITUTIONSZEIT_SAMPLE);
		}
	}

	public void initializeFirstSamples() {
		this.firstSamples = new int[Hyperparameters.countSwallow];
	}

	public void initializeTrueFirstSamples() {
		this.trueFirstSamples = new int[Hyperparameters.countSwallow];
	}

	public void initializeLastSamples() {
		this.lastSamples = new int[Hyperparameters.countSwallow];
	}

	public void initializeTrueLastSamples() {
		this.trueLastSamples = new int[Hyperparameters.countSwallow];
	}

	
	public void readLastSamples() {

		initializeLastSamples();

		try {
			this.lastSampleMatrix = CSV.read(new File(Hyperparameters.saveDir+"/lastSamples"));
		} catch (IOException e) {
			System.out.println("File not found @ reading LAST samples :-(");
		}
	}

	public void readTrueLastSamples() {

		initializeTrueLastSamples();

		try {
			this.trueLastSampleMatrix = CSV.read(new File(Hyperparameters.saveDir+"/trueLastSamples"));
		} catch (IOException e) {
			System.out.println("File not found @ reading True LAST samples :-(");
		}
	}

	public void readTrueFirstSamples() {

		initializeTrueFirstSamples();

		try {
			this.trueFirstSampleMatrix = CSV.read(new File(Hyperparameters.saveDir+"/trueFirstSamples"));
		} catch (IOException e) {
			System.out.println("File not found @ reading True FIRST samples :-(");
		}
	}

	public void readFirstSamples() {

		initializeFirstSamples();

		try {
			this.firstSampleMatrix = CSV.read(new File(Hyperparameters.saveDir+"/firstSamples"));
		} catch (IOException e) {
			System.out.println("File not found @ reading FIRST samples :-(");
		}
	}

	public void setLastSamples() {
		for (int i = 0; i < Hyperparameters.countSwallow ; i++)
		{
			this.lastSamples[i] = (int) lastSampleMatrix.get(i, 0);
		}
	}
	
	public void setTrueLastSamples() {
		for (int i = 0; i < Hyperparameters.countSwallow ; i++)
		{
			this.trueLastSamples[i] = (int) trueLastSampleMatrix.get(i, 0);
		}
	}

	public void setFirstSamples() {
		for (int i = 0; i < Hyperparameters.countSwallow ; i++)
		{
			this.firstSamples[i] = (int) firstSampleMatrix.get(i, 0);
		}
	}
	
	public void setTrueFirstSamples() {
		for (int i = 0; i < Hyperparameters.countSwallow ; i++)
		{
			this.trueFirstSamples[i] = (int) trueFirstSampleMatrix.get(i, 0);
		}
	}

	public void initializePMaxSamples() {
		this.pMaxSamples = new int[Hyperparameters.countSwallow];
	}

	public void initializeAnnotationSamples() {
		this.annotationSamples = new int[Hyperparameters.countSwallow];
	}


	public int computeUnSmoothness() {
		int unSmoothness = unsmoothWindows.length;
		return unSmoothness;
	}
	
	public void computeAvgUnSmoothness() {
		float sum = 0;
		
		for (int i = 0; i < unsmoothWindows.length ; i++)
		{
			sum += unsmoothWindows[i][1];
		}
		
		this.avgUnSmoothness = (sum/unsmoothWindows.length);
		
	}
	
	public void computeVarUnSmoothness() {
		float var = 0; 
		
		for (int i = 0; i < unsmoothWindows.length ; i++)
		{
			var += (unsmoothWindows[i][1] - avgUnSmoothness)*(unsmoothWindows[i][1] - avgUnSmoothness);
		}
		
		this.varUnSmoothness = (float) Math.sqrt(var/(unsmoothWindows.length -1));
		
	}

	public void computeUnsmoothWindows() {
		int count = 1;
		int countChange = 0;
		boolean positive = false;
		boolean negative = false;
		float[][] dummieCounter = new float[1000][2];

		if (sample2label[0][1] == 1)
		{
			positive = true;
		}
		else if (sample2label[0][1] == -1)
		{
			negative = true;
		}


		for (int sampleIdx = 1; sampleIdx < sample2label.length ; sampleIdx++)
		{
			if (sample2label[sampleIdx][1] == 1 && positive == true)
			{
				if (sampleIdx == sample2label.length - 1)
				{
					count++;
					countChange++;
					dummieCounter[countChange-1][0] = 1;
					dummieCounter[countChange-1][1] = count;	
				}
				else
				{
					count++;	
				}
			}
			else if (sample2label[sampleIdx][1] == -1 && positive == true)
			{	
				if (sampleIdx == sample2label.length - 1)
				{
					countChange++;
					dummieCounter[countChange-1][0] = 1;
					dummieCounter[countChange-1][1] = count;
					// da Ende ist:
					countChange++;
					dummieCounter[countChange-1][0] = -1;
					dummieCounter[countChange-1][1] = 1;
				}
				else
				{
					countChange++;
					dummieCounter[countChange-1][0] = 1;
					dummieCounter[countChange-1][1] = count;
					count = 1;
					positive = false;
					negative = true;
				}


			}
			else if (sample2label[sampleIdx][1] == -1 && negative == true)
			{
				if (sampleIdx == sample2label.length - 1)
				{
					count++;
					countChange++;
					dummieCounter[countChange-1][0] = -1;
					dummieCounter[countChange-1][1] = count;
				}
				else
				{
					count++;
				}
			}
			else if (sample2label[sampleIdx][1] == 1 && negative == true)
			{
				if (sampleIdx == sample2label.length - 1)
				{
					countChange++;
					dummieCounter[countChange-1][0] = -1;
					dummieCounter[countChange-1][1] = count;
					// da Ende ist:
					countChange++;
					dummieCounter[countChange-1][0] = 1;
					dummieCounter[countChange-1][1] = 1;
				}
				else
				{
					countChange++;
					dummieCounter[countChange-1][0] = -1;
					dummieCounter[countChange-1][1] = count;
					count = 1;
					negative = false;
					positive = true;
				}
			}

		}

		unsmoothWindows = new float[countChange][2];

		for (int i = 0; i < countChange ; i++)
		{
			unsmoothWindows[i][0] = dummieCounter[i][0];
			unsmoothWindows[i][1] = dummieCounter[i][1];
		}
	}

	public void saveUnsmoothWindows() throws IOException {
		Matrix saveUnsmoothWindows = DefaultMatrix.wrap(unsmoothWindows);
		Matrices.write(saveUnsmoothWindows, new File(Hyperparameters.saveDir+"/windowSizes_"+Hyperparameters.fold), false);
	}

	public void initializeSample2avgLabel(int size) {
		sample2avgLabel = new float[size][2];
	}

	public void initializeSample2avgSigmoid(int size) {
		sample2avgSigmoid = new float[size][2];
	}

	public void initializeSample2Sigmoid(int size) {
		sample2Sigmoid = new float[size][2];
	}

	public void getPMaxSample() {
		this.pMaxSample = (int) annotations.get(Hyperparameters.fold, Parser.ANNOTATION_COL_PMAX_SAMPLE);
	}

	public void getPMaxSamples() throws IOException {

		initializePMaxSamples();

		for (int i = 0; i < Hyperparameters.countSwallow ; i++)
		{
			this.pMaxSamples[i] = (int) annotations.get(i, Parser.ANNOTATION_COL_PMAX_SAMPLE);
		}
	}

	public void computeSample2avgLabel(int windowExtent) {

		int leftSide;
		int rightSide;

		//Gleiche Samples!
		for (int sample = 0; sample < sample2label.length ; sample++) 
		{
			sample2avgLabel[sample][0] = sample2label[sample][0];
		}

		for (int sample = 0; sample < sample2label.length ; sample++)
		{
			leftSide = sample - windowExtent;
			rightSide = sample + windowExtent;
			float avgSum = 0;

			if (leftSide >= 0 && rightSide < sample2label.length)  // alles okay!!
			{
				for (int sampleIdx = -windowExtent ; sampleIdx <= windowExtent ; sampleIdx++)
				{
					avgSum += sample2label[sample + sampleIdx][1];
				}
				avgSum = (avgSum/(2*windowExtent+1));
				sample2avgLabel[sample][1] = avgSum;
			}
			else if (leftSide < 0 && rightSide < sample2label.length)  // links zu kurz
			{
				int leftSide2 = windowExtent + leftSide;
				for (int sampleIdx = -leftSide2 ; sampleIdx <= windowExtent ; sampleIdx++)
				{
					avgSum += sample2label[sample + sampleIdx][1];
				}
				avgSum = (avgSum/(windowExtent+1+leftSide2));
				sample2avgLabel[sample][1] = avgSum;
			}
			else if (leftSide >= 0 && rightSide >= sample2label.length) // rechts zu kurz
			{
				int lastIdx = (sample2label.length - 1);
				int rightSide2 = (lastIdx - sample);

				for (int sampleIdx = -windowExtent; sampleIdx <= rightSide2 ; sampleIdx++)
				{
					avgSum += sample2label[sample + sampleIdx][1];
				}
				avgSum = (avgSum/(windowExtent+1+rightSide2));
				sample2avgLabel[sample][1] = avgSum;
			}
			else if ( leftSide < 0 && rightSide >= sample2label.length) // beidseitig zu kurz
			{
				System.out.println("WindowExtent is tooo high!!");
			}
			else  // default alles falsch :-)
			{
				System.out.println("Something is wrong!!! Go Home Nico, you are drunk!!"); 
			}
		}

	}

	public void computeSample2avgSigmoid(int windowExtent) {

		int leftSide;
		int rightSide;

		//Gleiche Samples!
		for (int sample = 0; sample < sample2label.length ; sample++) 
		{
			sample2avgSigmoid[sample][0] = sample2Sigmoid[sample][0];
		}

		for (int sample = 0; sample < sample2label.length ; sample++)
		{
			leftSide = sample - windowExtent;
			rightSide = sample + windowExtent;
			float avgSum = 0;

			if (leftSide >= 0 && rightSide < sample2Sigmoid.length)  // alles okay!!
			{
				for (int sampleIdx = -windowExtent ; sampleIdx <= windowExtent ; sampleIdx++)
				{
					avgSum += sample2Sigmoid[sample + sampleIdx][1];
				}
				avgSum = (avgSum/(2*windowExtent+1));
				sample2avgSigmoid[sample][1] = avgSum;
			}
			else if (leftSide < 0 && rightSide < sample2Sigmoid.length)  // links zu kurz
			{
				int leftSide2 = windowExtent + leftSide;
				for (int sampleIdx = -leftSide2 ; sampleIdx <= windowExtent ; sampleIdx++)
				{
					avgSum += sample2Sigmoid[sample + sampleIdx][1];
				}
				avgSum = (avgSum/(windowExtent+1+leftSide2));
				sample2avgSigmoid[sample][1] = avgSum;
			}
			else if (leftSide >= 0 && rightSide >= sample2Sigmoid.length) // rechts zu kurz
			{
				int lastIdx = (sample2Sigmoid.length - 1);
				int rightSide2 = (lastIdx - sample);

				for (int sampleIdx = -windowExtent; sampleIdx <= rightSide2 ; sampleIdx++)
				{
					avgSum += sample2Sigmoid[sample + sampleIdx][1];
				}
				avgSum = (avgSum/(windowExtent+1+rightSide2));
				sample2avgSigmoid[sample][1] = avgSum;
			}
			else if ( leftSide < 0 && rightSide >= sample2Sigmoid.length) // beidseitig zu kurz
			{
				System.out.println("WindowExtent is tooo high!!");
			}
			else  // default alles falsch :-)
			{
				System.out.println("Something is wrong!!! Go Home Nico, you are drunk!!"); 
			}
		}

	}

	public void initializeSample2Label(int size) {
		sample2label = new float[size][2];
	}

	public void mergeSample2Label( Evaluation eval) {
		for (int row = 0; row < sample2label.length; row++)
		{
			sample2label[row][0] = (samples[row]);
			sample2label[row][1] = (eval.predictedLabels[row]);
		}
	}

	public void mergeSample2Sigmoid( Evaluation eval) {
		for (int row = 0; row < sample2label.length; row++)
		{
			sample2Sigmoid[row][0] = (samples[row]);
			sample2Sigmoid[row][1] = (eval.sigmoidLabels[row]);
		}
	}

	public void initializeVariances() {
		variances = new double[Hyperparameters.variables];
	}

	public void initializeExpectationValues() {
		expectationValues = new double[Hyperparameters.variables];
	}

	public void initializeSamples(int size) {
		samples = new int[size];
	}

	public void predictTrainAnnotations() {

		this.trainAnnotations = new int[Hyperparameters.countSwallow];

		for (int i = 0 ; i < Hyperparameters.countSwallow ; i++)
		{
			if (i == Hyperparameters.fold)
			{
				trainAnnotations[i] = 0;
				continue;
			}

			int firstSample = foldFirstSamples[i];
			int pMaxSample = foldPMaxSamples[i];

			int runIndex = pMaxSample;

			while (sample2avgLabel[runIndex][1] >= 0)
			{
				runIndex++;
			}
			

			trainAnnotations[i] = runIndex;



		}

	}

	public float computeTrainSampleError() {

		float error = 0;

		for (int i = 0; i < Hyperparameters.countSwallow ; i++)
		{
			if (i == Hyperparameters.fold)
			{
				continue;
			}

			error += Math.abs(trainAnnotations[i] - foldAnnotationSamples[i]);
		}

		return (error/(Hyperparameters.countSwallow-1));
	}
	
	public void computePMaxSample(){
		int index = 0;
		float max = 0;
		for (int i = 0; i < labels.length ; i++)
		{
//			System.out.println(array[i][156]);
			if (max < array[i][156])
			{
				max = (float) array[i][156];
				index = i;
//				System.out.println(array[i][156]);
			}
		}
		
		this.pMaxSample = index;
		System.out.println("computed: " + pMaxSample);
		System.out.println("annotation: " + (int) annotations.get(Hyperparameters.fold, Parser.ANNOTATION_COL_PMAX_SAMPLE));
	}

	public int predictAnnotation() {

		int sampleIdx = pMaxSample;



		@SuppressWarnings("unused")
		int leftAnnotation = 0;
		int rightAnnotation = 0;
		
		int correctedSampleIdx = sampleIdx;
		System.out.println(correctedSampleIdx);

		//		System.out.println(correctedSampleIdx);

		while (sample2avgLabel[correctedSampleIdx][1] >= 0 )
		{

			correctedSampleIdx++;
			System.out.println(sample2avgLabel[correctedSampleIdx][1]);
		}


		rightAnnotation = correctedSampleIdx;  // Stimmt so!
		return rightAnnotation;
	}

	public int predictSigmoidalAnnotation() {
		int sampleIdx = pMaxSample;
		@SuppressWarnings("unused")
		int leftAnnotation = 0;
		int rightAnnotation = 0;

//		 - (int) sample2label[0][0];
		
		
		int correctedSampleIdx = sampleIdx;

		//		System.out.println(correctedSampleIdx);
		while (sample2avgSigmoid[correctedSampleIdx][1] >= 0.5 )
		{
			correctedSampleIdx++;
		}
		rightAnnotation = correctedSampleIdx;  // Stimmt so!
		return rightAnnotation;
	}



	public void initializeArray(int rowNr, int colNr)  { 
		array = new double[rowNr][colNr];
	}

	public void initializeLabels(int rowNr) {
		labels = new double[rowNr];
	}

	public void zeroMatrices(int rowNr, int colNr)  {
		for (int col = 0; col < colNr ; col++)
		{

			for (int row = 0; row < rowNr ; row++)
			{
				array[row][col] = 0;
			}


		}
	}

	public void setZeroToOne()
	{
		for (int i = 0; i < labels.length ; i++)
		{
			if(labels[i] == 0)
			{
				labels[i] = -1;
			}
		}
	}

	public void initializeHashMap(int rowNr)
	{

		this.values = new TIntDoubleHashMap[rowNr];
		for (int i = 0 ; i < rowNr ; i++)
		{
			this.values[i] = new TIntDoubleHashMap();
		}


	}

	public int countPositive()
	{
		int count = 0;
		for (int i = 0; i < labels.length ; i++)
		{
			if(labels[i] == 1)
			{
				count++;
			}

		}
		return count;
	}


}
