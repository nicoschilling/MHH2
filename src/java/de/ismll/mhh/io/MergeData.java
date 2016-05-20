package de.ismll.mhh.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import de.ismll.mhh.io.Parser;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.io.weka.ArffEncoder;
import de.ismll.table.io.weka.ArffEncoder.Type;
import de.ismll.utilities.Tools;



public class MergeData {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		int proband = 3;

		int annotator = 0;   // 0 ist MichaelJungheim   1 ist SimoneMiller  2 ist gemein

		File dataDir = new File("/home/nico/Documents/MHH/Proband"+proband);
		
		String fileNameString;
		
		int[] startSamples;



		int countSwallow;
		
		if (proband == 1)
		{
			countSwallow = 10;
			fileNameString = "00506534";
		}
		else if (proband == 2 )
		{
			countSwallow = 11;
			fileNameString = "00511910";
		}
		else
		{
			countSwallow = 12;
			fileNameString = "00518402";
		}
		
		startSamples = new int[countSwallow];

		int rows = 0;

		int dataColumns = 0;
		int fourierColumns = 0;

		Matrix[] data = new Matrix[countSwallow];
		Matrix[] fourier = new Matrix[countSwallow];
		Matrix[] pMax = new Matrix[countSwallow];
		Matrix[] annotations = new Matrix[3];

		float[][] trueFirstSamples = new float[countSwallow][1];
		float[][] trueLastSamples = new float[countSwallow][1];
		
		float[][] firstSamples = new float[countSwallow][1];
		float[][] lastSamples = new float[countSwallow][1];
		
		float[][] trueAnnotationSamples = new float[countSwallow][1];
		float[][] annotationSamples = new float[countSwallow][1];



		//		File save = new File("/home/nico/Documents/MHH/Proband1/00506534Schluck1.ASC.data/save.csv");
		
		
		
		

		for( int i=1; i <= countSwallow ; i++){

			File sensorData = new File(dataDir, fileNameString+"Schluck"+i+".ASC.data/data.csv");

			File fft = new File(dataDir, fileNameString+"Schluck"+i+".ASC.data/fft.csv");

			File pressureMax = new File(dataDir, fileNameString+"Schluck"+i+".ASC.data/subset-max.csv"); 
			
			


			data[i-1] = Parser.readCSVFile(sensorData);
			fourier[i-1] = Parser.readCSVFile(fft);
			pMax[i-1] = Parser.readCSVFile(pressureMax);

			int dataRows = data[i-1].getNumRows();
			
			trueFirstSamples[i-1][0] = data[i-1].get(0, 0);  
			trueLastSamples[i-1][0] = data[i-1].get(dataRows - 1, 0); 
			


			rows += dataRows;
		}
		
		// get the first and last Samples and annotationSamples :-(
		
		firstSamples[0][0] = 0;
		lastSamples[0][0] = trueLastSamples[0][0] - trueFirstSamples[0][0];
		
		for (int i = 1 ; i < countSwallow ; i++)
		{
			float sampleDiff = trueLastSamples[i][0] - trueFirstSamples[i][0];
			
			firstSamples[i][0] = lastSamples[i-1][0] + 1;
			lastSamples[i][0] = firstSamples[i][0] + sampleDiff;
		}
		
//		annotationSamples[0][0] = 
		
		
		Matrix firstSamplesMatrix = DefaultMatrix.wrap(firstSamples);
		Matrix lastSamplesMatrix = DefaultMatrix.wrap(lastSamples);
		
		Matrix trueFirstSamplesMatrix = DefaultMatrix.wrap(trueFirstSamples);
		Matrix trueLastSamplesMatrix = DefaultMatrix.wrap(trueLastSamples);
		
		
		//write firstSamples
		
		
		
		if (annotator == 0)
		{
			Matrices.write(firstSamplesMatrix, new File("/home/nico/Documents/MHH/Datasets/MJ/Proband"+proband+"/firstSamples"), false);
			Matrices.write(lastSamplesMatrix, new File("/home/nico/Documents/MHH/Datasets/MJ/Proband"+proband+"/lastSamples"), false);
			Matrices.write(trueFirstSamplesMatrix, new File("/home/nico/Documents/MHH/Datasets/MJ/Proband"+proband+"/trueFirstSamples"), false);
			Matrices.write(trueLastSamplesMatrix, new File("/home/nico/Documents/MHH/Datasets/MJ/Proband"+proband+"/trueLastSamples"), false);
		}
		else if (annotator == 1)
		{
			Matrices.write(firstSamplesMatrix, new File("/home/nico/Documents/MHH/Datasets/SM/Proband"+proband+"/firstSamples"), false);
			Matrices.write(lastSamplesMatrix, new File("/home/nico/Documents/MHH/Datasets/SM/Proband"+proband+"/lastSamples"), false);
			Matrices.write(trueFirstSamplesMatrix, new File("/home/nico/Documents/MHH/Datasets/SM/Proband"+proband+"/trueFirstSamples"), false);
			Matrices.write(trueLastSamplesMatrix, new File("/home/nico/Documents/MHH/Datasets/SM/Proband"+proband+"/trueLastSamples"), false);
		}
		else if (annotator == 2)
		{
			Matrices.write(firstSamplesMatrix, new File("/home/nico/Documents/MHH/Datasets/gemein/Proband"+proband+"/firstSamples"), false);
			Matrices.write(lastSamplesMatrix, new File("/home/nico/Documents/MHH/Datasets/gemein/Proband"+proband+"/lastSamples"), false);
			Matrices.write(trueFirstSamplesMatrix, new File("/home/nico/Documents/MHH/Datasets/gemein/Proband"+proband+"/trueFirstSamples"), false);
			Matrices.write(trueLastSamplesMatrix, new File("/home/nico/Documents/MHH/Datasets/gemein/Proband"+proband+"/trueLastSamples"), false);
		}
		else
		{
			System.out.println("STFU!");
		}
		
		// StartSamples berechnen, bitte!!
		
		for (int i = 0; i < countSwallow ; i++)
		{
			startSamples[i] = Parser.computeStartSample(data[i]) + (int) data[i].get(0, 0); // keine normalisierten Samples!!
		}

		//Annotationen lesen


//		annotations[0] = Parser.readAnnotations(new File("/home/nico/Documents/MHH/manual_annotations/"+proband+"-MJ.tsv"), 50);
//		annotations[1] = Parser.readAnnotations(new File("/home/nico/Documents/MHH/manual_annotations/"+proband+"-SM.tsv"), 50);
//		annotations[2] = Parser.readAnnotations(new File("/home/nico/Documents/MHH/manual_annotations/"+proband+"-gemein.tsv"), 50);


		dataColumns = data[0].getNumColumns();
		fourierColumns = fourier[0].getNumColumns();
		



		//		Parser.time2Sample(time)


		final DefaultMatrix m = new DefaultMatrix(rows, (dataColumns + fourierColumns + 1 ));  //Was kommt noch alles rein?

		int continuousRowIdx = 0;

		HashMap<Integer, Integer> sample2row = new HashMap<Integer, Integer>();


		for(int mat = 0; mat < countSwallow; mat++)
		{
//			ReadFolder readFolder = new ReadFolder();
//			readFolder.setSchluckverzeichnis(new File("/home/nico/Documents/MHH/Proband"+proband+"/"+fileNameString+"Schluck"+(mat+1)+".ASC.data"));
//			readFolder.run();
//			int rdEnd = Parser.time2Sample(readFolder.getRdend(), Tools.parseInt(readFolder.getSamplerate()));

			for(int row = 0; row < data[mat].getNumRows(); row++)
			{
				// Pressure Values
				int currentCol=0;

				for (int col = 0; col < data[mat].getNumColumns(); col++)
				{
					m.set(continuousRowIdx, currentCol++, data[mat].get(row , col));
				}



//				FFT Values
				for (int col = 1; col < fourier[mat].getNumColumns(); col++)
				{
					m.set(continuousRowIdx, currentCol++, fourier[mat].get(row , col));
				}

				//Subset-Max

				m.set(continuousRowIdx, currentCol++, pMax[mat].get(row, 1));
				
				//Schluckzugehoerigkeit
				
//				m.set(continuousRowIdx, currentCol++,  mat);

				// Binaere Annotation

				if (annotator == 0)
				{
//					if(m.get(continuousRowIdx,0) <= startSamples[mat]  || m.get(continuousRowIdx,0) >= annotations[0].get(mat, Parser.ANNOTATION_COL_RESTITUTIONSZEIT_SAMPLE))
//					{
//						m.set(continuousRowIdx,  currentCol++, -1);
//					}
//					else
//					{
//						m.set(continuousRowIdx, currentCol++, 1);
//					}
					m.set(continuousRowIdx, currentCol++, 1);
				}
				else if (annotator == 1)
				{
					if(m.get(continuousRowIdx,0) <= startSamples[mat]  || m.get(continuousRowIdx,0) >= annotations[1].get(mat, Parser.ANNOTATION_COL_RESTITUTIONSZEIT_SAMPLE))
					{
						m.set(continuousRowIdx,  currentCol++, -1);
					}
					else 
					{
						m.set(continuousRowIdx, currentCol++, 1);
					}
				}
				else if (annotator == 2)
				{
					if(m.get(continuousRowIdx,0) <= startSamples[mat]  || m.get(continuousRowIdx,0) >= annotations[2].get(mat, Parser.ANNOTATION_COL_RESTITUTIONSZEIT_SAMPLE))
					{
						m.set(continuousRowIdx,  currentCol++, -1);
					}
					else 
					{
						m.set(continuousRowIdx, currentCol++, 1);
					}
				}
				else if (annotator == 3)
				{
					m.set(continuousRowIdx, currentCol++, 1);
				}
				else
				{
					System.out.println("annotator is wrongly chosen!!");
				}

				sample2row.put(Integer.valueOf((int)m.get(continuousRowIdx, 0)), Integer.valueOf(continuousRowIdx));
//				sample2row.put(Integer.valueOf((int)m.get(continuousRowIdx, 0)), Integer.valueOf(continuousRowIdx));
				continuousRowIdx++;

			}
		}





		TreeMap<Integer, Vector> sample2Data = new TreeMap<Integer, Vector>();
		HashMap<Integer, List<Integer>> files = new HashMap<>();

		for(int mat = 0; mat < countSwallow; mat++) {
			Integer iMat = Integer.valueOf(mat);
			List<Integer> list = files.get(iMat);
			if (list == null) {
				list = new ArrayList<Integer>();
				files.put(iMat, list);
			}




			for(int row = 0; row < data[mat].getNumRows(); row++) {
				Integer sampleId = Integer.valueOf((int)data[mat].get(row, 0)); // sampleId

				list.add(sampleId);
				Vector old = sample2Data.put(sampleId, Matrices.row(m, sample2row.get(sampleId).intValue()));

//				if (old != null) {
//					files.get(Integer.valueOf(mat-1)).remove(sampleId);
//				}
			}
		}







		//		Matrices.writeArff(outputFile, m, new ArffEncoder() {
		//			
		//			@Override
		//			public String getName() {
		//				return "MHH-train-2";
		//			}
		//			
		//			@Override
		//			public Type getAttributeType(int column) {
		//				if (column == m.getNumColumns()-1) 
		//					return Type.Nominal;
		//				return Type.Numeric;
		//			}
		//			
		//			@Override
		//			public String getAttributeName(int column) {
		//				if (column == m.getNumColumns()-1)
		//					return "clazz";
		//				return "attr" + column;
		//			}
		//			
		//			@Override
		//			public String encode(int column, float value) {
		//				if (column == m.getNumColumns()-1)
		//					return (int)value + "";
		//				
		//				return value + "";
		//			}
		//		}, 10000);

		for (int foldTrain = 0; foldTrain < files.size(); foldTrain++) {
			int numTest  = 0;
			int numTrain = 0;

			for (int inner = 0; inner < files.size(); inner++) {
				if (foldTrain == inner) {
					numTest+=files.get(Integer.valueOf(inner)).size();
				} else {
					numTrain+=files.get(Integer.valueOf(/*stimmt: */inner)).size();
				}
			}

			Matrix train = new DefaultMatrix(numTrain, m.getNumColumns());
			Matrix test = new DefaultMatrix(numTest, m.getNumColumns());

			int trainIdx = 0;
			int testIdx = 0;

			for (int inner = 0; inner < files.size(); inner++) {
				if (foldTrain == inner) {
					for (Integer i : files.get(Integer.valueOf(inner))) 						
						Matrices.setRow(test, testIdx++, sample2Data.get(i));
				} else {
					for (Integer i : files.get(Integer.valueOf(inner))) 						
						Matrices.setRow(train, trainIdx++, sample2Data.get(i));
				}
			}

			/////Scale the output between -1 and 1  !!!

			Matrix trainScale = new DefaultMatrix(numTrain, m.getNumColumns());
			Matrix testScale = new DefaultMatrix(numTest, m.getNumColumns());

			double[] estTrainExpectationValues = new double[m.getNumColumns()];
			double[] estTrainVariances = new double[m.getNumColumns()];

			double[] estTestExpectationValues = new double[m.getNumColumns()];
			double[] estTestVariances = new double[m.getNumColumns()];

			for (int column = 1; column < m.getNumColumns() - 1  /*label und Schluckzugehoerigkeit und index nicht skalieren*/ ; column++)
			{
				double trainColSum = 0;
				double testColSum = 0;

				for (int row = 0; row < numTrain ; row++)
				{
					trainColSum += train.get(row, column);
				}

				for (int row = 0; row < numTest ; row++)
				{
					testColSum += test.get(row, column);
				}

				double trainExpectationValue = trainColSum/(train.getNumRows());
				double testExpectationValue = testColSum/(test.getNumRows());

				estTrainExpectationValues[column] = trainExpectationValue;
				estTestExpectationValues[column] = testExpectationValue;


			}

			for (int column = 1; column < m.getNumColumns() - 1 /*label und Schluckzugehoerigkeit und index nicht skalieren*/; column++)
			{
				double trainVariance = 0;
				double testVariance = 0;

				for (int row = 0; row < numTrain ; row++)
				{
					trainVariance += (train.get(row, column) - estTrainExpectationValues[column])*(train.get(row, column) - estTrainExpectationValues[column]);
				}
				for (int row = 0; row < numTest ; row++)
				{
					testVariance += (test.get(row, column) - estTestExpectationValues[column])*(test.get(row, column) - estTestExpectationValues[column]);
				}

				trainVariance = Math.sqrt(trainVariance/(train.getNumRows()-1));
				testVariance = Math.sqrt(testVariance/(test.getNumRows()-1));

				estTrainVariances[column] = trainVariance;
				estTestVariances[column] = testVariance;

			}

			for (int column = 0; column < m.getNumColumns()  ; column++)
			{
				if (column == 0 || column == (m.getNumColumns() - 1))  /*label und Schluckzugehoerigkeit und index nicht skalieren*/
				{
					for (int row = 0; row < numTrain ; row++)
					{
						trainScale.set(row, column, train.get(row, column));
					}
					for (int row = 0; row < numTest ; row++)
					{
						testScale.set(row, column, test.get(row, column));
					}
				}
				else
				{
					for (int row = 0; row < numTrain ; row++)
					{
						if (estTrainVariances[column] != 0)
						{
							float scaleTrainValue = (float) ((train.get(row, column)-estTrainExpectationValues[column])/estTrainVariances[column]);
							trainScale.set(row, column, scaleTrainValue );
						}
					}
					for (int row = 0; row < numTest ; row++)
					{
						if (estTestVariances[column] != 0)
						{
							float scaleTestValue = (float) ((test.get(row, column)-estTestExpectationValues[column])/estTestVariances[column]);
							testScale.set(row, column, scaleTestValue);
						}
					}
				}
			}
			
			
			if (annotator == 0)
			{
				Matrices.write(testScale, new File("/home/nico/Documents/MHH/Datasets/MJ/Proband"+proband+"/test-s-" + foldTrain + ".csv"), false);
				Matrices.write(trainScale, new File("/home/nico/Documents/MHH/Datasets/MJ/Proband"+proband+"/train-s-" + foldTrain + ".csv"), false);
			}
			else if (annotator == 1)
			{
				Matrices.write(testScale, new File("/home/nico/Documents/MHH/Datasets/SM/Proband"+proband+"/test-s-" + foldTrain + ".csv"), false);
				Matrices.write(trainScale, new File("/home/nico/Documents/MHH/Datasets/SM/Proband"+proband+"/train-s-" + foldTrain + ".csv"), false);
			}
			else if (annotator == 2)
			{
				Matrices.write(testScale, new File("/home/nico/Documents/MHH/Datasets/gemein/Proband"+proband+"/test-s-" + foldTrain + ".csv"), false);
				Matrices.write(trainScale, new File("/home/nico/Documents/MHH/Datasets/gemein/Proband"+proband+"/train-s-" + foldTrain + ".csv"), false);
			}
			else
			{
				System.out.println("annotator is wrongly chosen!!");
			}


			/// very ugly!!	


//			Matrices.write(test, new File("/home/nico/Documents/MHH/Datasets/test" + foldTrain + ".csv"), false);
//			Matrices.write(train, new File("/home/nico/Documents/MHH/Datasets/train" + foldTrain + ".csv"), false);

			//			Matrices.writeArff(outputFile, m, new ArffEncoder() {
			//				
			//				@Override
			//				public String getName() {
			//					return "MHH-train-2";
			//				}
			//				
			//				@Override
			//				public Type getAttributeType(int column) {
			//					if (column == m.getNumColumns()-1) 
			//						return Type.Nominal;
			//					return Type.Numeric;
			//				}
			//				
			//				@Override
			//				public String getAttributeName(int column) {
			//					if (column == m.getNumColumns()-1)
			//						return "clazz";
			//					return "attr" + column;
			//				}
			//				
			//				@Override
			//				public String encode(int column, float value) {
			//					if (column == m.getNumColumns()-1)
			//						return (int)value + "";
			//					
			//					return value + "";
			//				}
			//			}, 10000);



		}








		// write dataset to file
		//		Matrices.write(m, outputFile, false);








	}

}
