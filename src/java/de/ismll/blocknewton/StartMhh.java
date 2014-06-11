package de.ismll.blocknewton;

import java.io.File;
import java.io.IOException;

import de.ismll.blocknewton.Data;
import de.ismll.blocknewton.Evaluation;
import de.ismll.blocknewton.Gradient;
import de.ismll.blocknewton.Hyperparameters;
import de.ismll.blocknewton.IO;
import de.ismll.blocknewton.Newton;
import de.ismll.blocknewton.Parameters;
import de.ismll.bootstrap.CommandLineParser;
import de.ismll.mhh.io.Parser;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.impl.DefaultMatrix;



public class StartMhh {


	public static void main(String[] args) throws IOException {

		Hyperparameters hyper = new Hyperparameters();
		CommandLineParser.parseCommandLine(args, hyper);

		Hyperparameters.setDirectories();

		Hyperparameters.setSaveDirectory();


		Hyperparameters.setCountSwallow();


		double overAllTrainAcc = 0;
		double overAllTestAcc = 0;

		int unSmoothness = 0;

		Matrix annotationMatrix = null;

		float[][] sampleDifferences = new float[Hyperparameters.countSwallow][1];

		if (Hyperparameters.annotator == 0)
		{
			annotationMatrix = Parser.readAnnotations(new File("/home/nico/Documents/MHH/manual_annotations/"+Hyperparameters.proband+"-MJ.tsv"), 50);
		}
		else if (Hyperparameters.annotator == 1)
		{
			annotationMatrix = Parser.readAnnotations(new File("/home/nico/Documents/MHH/manual_annotations/"+Hyperparameters.proband+"-SM.tsv"), 50);
		}
		else if (Hyperparameters.annotator == 2)
		{
			annotationMatrix = Parser.readAnnotations(new File("/home/nico/Documents/MHH/manual_annotations/"+Hyperparameters.proband+"-gemein.tsv"), 50);
		}




		for (int fold = 0; fold < Hyperparameters.countSwallow ; fold++)
		{
			Hyperparameters.setFold(fold);

			int[] foldFirstSamples = new int[Hyperparameters.countSwallow];
			int[] foldLastSamples = new int[Hyperparameters.countSwallow];

			int[] foldPMaxSamples = new int[Hyperparameters.countSwallow];


			IO input = new IO();

			Data trainData = new Data();

			Data testData = new Data();

			Parameters params = new Parameters();

			Gradient grad = new Gradient();

			Newton newton = new Newton();

			Evaluation trainEval = new Evaluation();

			Evaluation testEval = new Evaluation();



			Hyperparameters.setDataSizeFile(Hyperparameters.baseDir+"/data_sizes_n_proband"+Hyperparameters.proband);
			
			


			input.readDataSizes(Hyperparameters.dataSizeFile);

			Hyperparameters.setTrainInstancesFiles(Hyperparameters.fold, input);

			Hyperparameters.setBatchSize(Hyperparameters.trainInstances);
			Hyperparameters.setSmoothSize(Hyperparameters.trainInstances);





			input.readDenseMHHData(Hyperparameters.trainfile, trainData, "train");
			input.readLabels(Hyperparameters.labelTrainFile, trainData, "train");
			input.readSamples(Hyperparameters.trainfile, trainData, "train");
			
//			System.out.println("hallo");
			
			Hyperparameters.setDataSizeFile(Hyperparameters.baseDir+"/data_sizes_n_proband3");
			input.readDataSizes(Hyperparameters.dataSizeFile);
			Hyperparameters.setTestInstancesFiles(Hyperparameters.fold, input);

			input.readDenseMHHData(Hyperparameters.testfile, testData, "test");
			input.readLabels(Hyperparameters.labelTestFile, testData, "test");
			input.readSamples(Hyperparameters.testfile, testData, "test");

			int annotation = (int) annotationMatrix.get(Hyperparameters.fold, Parser.ANNOTATION_COL_RESTITUTIONSZEIT_SAMPLE) - testData.samples[0];

			params.setExpectationValue(0);
			params.setVariance(1);

			params.initializeParameters();
			params.initializeParametersOld();
			params.initializeParametersBestSoFar();



			double trainAccuracy = 0;
			double testAccuracy = 0;
			double trainAccuracyBestSoFar = 0;

			testData.getAnnotations();
			testData.getPMaxSample();


			trainData.getAnnotations();
			trainData.getAnnotationSamples();
			trainData.readFirstSamples();
			trainData.setFirstSamples();
			trainData.readLastSamples();
			trainData.setLastSamples();
			trainData.readTrueFirstSamples();
			trainData.setTrueFirstSamples();
			trainData.readTrueLastSamples();
			trainData.setTrueLastSamples();
			trainData.getPMaxSamples();

			trainData.initializeFoldSamples();
			trainData.setFoldSamples();


	
			for (int i = 0; i < Hyperparameters.countSwallow ; i++)
			{
				System.out.println(trainData.pMaxSamples[i] + "\t" + trainData.annotationSamples[i]);
			}

			// Compute the right PMAX SAMPLES!!!

			

//			for (int i = 0; i < Hyperparameters.countSwallow; i++)
//			{
//				System.out.println("First: " + foldFirstSamples[i] + " Last: " + foldLastSamples[i] + " Pmax: " + foldPMaxSamples[i]);
//			}



			float[][] iteration2Acc = new float[Hyperparameters.maxIterations][2];
			float[][] iteration2SampleDiff = new float[Hyperparameters.maxIterations][2];
			float[][] iteration2testSampleDiff = new float[Hyperparameters.maxIterations][2];


			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			for (int update = 0 ; update < Hyperparameters.maxIterations ; update++)
			{

				params.computeRandomBatch();


				if (Hyperparameters.laplacian == true)
				{
					params.computeSmoothBatch();

					grad.computeLaplacianGradient(trainData, params, Hyperparameters.windowSize);
				}
				else
				{
					grad.computeGradient(trainData, params);
				}



				params.updateParameters(trainData, grad, newton, update);

				trainEval.predictLabels(trainData, params);
				testEval.predictLabels(testData, params);

				trainAccuracy = trainEval.computeAccuracy(trainData);
				testAccuracy = testEval.computeAccuracy(testData);

//				trainData.initializeSample2Label(Hyperparameters.trainInstances);
//				trainData.mergeSample2Label(trainEval);
//
//				trainData.initializeSample2avgLabel(Hyperparameters.trainInstances);
//				trainData.computeSample2avgLabel(Hyperparameters.windowExtent);
//
//				trainData.predictTrainAnnotations();


//				float trainSampleError = trainData.computeTrainSampleError();
//
				testData.initializeSample2Label(Hyperparameters.testInstances);
				testData.mergeSample2Label(testEval);
//				testData.computeUnsmoothWindows();
//				
//				testData.computeAvgUnSmoothness();
//				testData.computeVarUnSmoothness();



				testData.initializeSample2avgLabel(Hyperparameters.testInstances);
				testData.computeSample2avgLabel(Hyperparameters.windowExtent);

//				int testAnnotation = testData.predictAnnotation();

//				int samplediff = Math.abs(testAnnotation - annotation);

				

				if (trainAccuracy > trainAccuracyBestSoFar)
				{
					params.setParametersBestSoFar(params.parameters);
					trainAccuracyBestSoFar = trainAccuracy;
				}

				//				 Here come the Bolt Drivers!!

				//				if ( trainAccuracy < trainAccuracyOld)
				//				{
				//					params.setParameters(params.parametersOld);
				//					Hyperparameters.setStepSize(0.7*Hyperparameters.stepSize);
				//					update--;
				//				}
				//				else
				//				{
				//					Hyperparameters.setStepSize(1.1*Hyperparameters.stepSize);
				//					trainAccuracyOld = trainAccuracy;
				//				}

				
				iteration2Acc[update][0] = update;
				iteration2Acc[update][1] = (float) trainAccuracy;
				
//				iteration2SampleDiff[update][0] = update;
//				iteration2SampleDiff[update][1] = trainSampleError;
				
//				iteration2testSampleDiff[update][0] = update;
//				iteration2testSampleDiff[update][1] = samplediff;
				
				


//				System.out.println(" Iteration: " + update + " TrainAcc: " + trainAccuracy + " TestAcc: " + testAccuracy + " TestDiff: " + samplediff  + " TrainDiff: " + trainSampleError + " avgWindowSize: " + testData.avgUnSmoothness + " windowSizeVar: " + testData.varUnSmoothness);
				System.out.println(" Iteration: " + update + " TrainAcc: " + trainAccuracy);
			}

			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


			params.setParameters(params.parametersBestSoFar);

			trainEval.predictLabels(trainData, params);
			testEval.predictLabels(testData, params);
			testEval.predictSigmoidLabels(testData, params);




			trainAccuracy = trainEval.computeAccuracy(trainData);
			testAccuracy = testEval.computeAccuracy(testData);



			overAllTestAcc += testAccuracy;
			overAllTrainAcc += trainAccuracy;

			testData.initializeSample2Label(Hyperparameters.testInstances);
			testData.initializeSample2Sigmoid(Hyperparameters.testInstances);


			testData.mergeSample2Label(testEval);
			testData.mergeSample2Sigmoid(testEval);

			
			
			testData.computePMaxSample();

			Matrix testSample2Label = DefaultMatrix.wrap(testData.sample2label);
			Matrix testSample2Sigmoid = DefaultMatrix.wrap(testData.sample2Sigmoid);
			
			Matrix iteration2accuracyMatrix = DefaultMatrix.wrap(iteration2Acc);
			Matrix iteration2trainsampleDiff = DefaultMatrix.wrap(iteration2SampleDiff);
			Matrix iteration2testsampleDiff = DefaultMatrix.wrap(iteration2testSampleDiff);
			
			Matrices.write(iteration2accuracyMatrix, new File(Hyperparameters.saveDir+"/iteration2trainAcc"+Hyperparameters.fold), false);
			Matrices.write(iteration2trainsampleDiff, new File(Hyperparameters.saveDir+"/iteration2trainSampleDiff"+Hyperparameters.fold), false);
			Matrices.write(iteration2testsampleDiff, new File(Hyperparameters.saveDir+"/iteration2testSampleDiff"+Hyperparameters.fold), false);



			Matrices.write(testSample2Label, new File(Hyperparameters.saveDir+"/test"+Hyperparameters.fold+"Predictions"), false);
			//			Matrices.write(testSample2Sigmoid, new File(Hyperparameters.saveDir+"/test"+Hyperparameters.fold+"SigPredictions"), false);

			testData.initializeSample2avgLabel(Hyperparameters.testInstances);
			testData.initializeSample2avgSigmoid(Hyperparameters.testInstances);

			testData.computeSample2avgLabel(Hyperparameters.windowExtent);
			testData.computeSample2avgSigmoid(Hyperparameters.windowExtent);




			float[][] accuracies = new float[1][1];

			float[][] parametersFinal = new float[params.parameters.length][1];

			for (int i = 0; i < params.parameters.length ; i++)
			{
				parametersFinal[i][0] = (float) (params.parametersBestSoFar[i]);
			}

			@SuppressWarnings("unused")
			Matrix saveParameters = DefaultMatrix.wrap(parametersFinal);

			accuracies[0][0] = (float) testAccuracy;

			float[][] testSigAnnotation = new float[1][1];
			float[][] testAnnotation = new float[1][1];
			float[][] testSampleDiff = new float[1][1];
			
			System.out.println(testData.pMaxSample);


			testAnnotation[0][0] = testData.predictAnnotation();
			testSigAnnotation[0][0] = testData.predictSigmoidalAnnotation();


			float testAnnot = testAnnotation[0][0];

			float sampleDiff = Math.abs(testAnnot - annotation);

			sampleDifferences[Hyperparameters.fold][0] = sampleDiff;

			testSampleDiff[0][0] = sampleDiff;

			Matrix testSampleDiffMatrix = DefaultMatrix.wrap(testSampleDiff);

			Matrix accuracyMatrix = DefaultMatrix.wrap(accuracies);

			Matrix testAnnotationMatrix = DefaultMatrix.wrap(testAnnotation);
			Matrix testAnnotationSigMatrix = DefaultMatrix.wrap(testSigAnnotation);

			testData.computeUnsmoothWindows();
			//			testData.saveUnsmoothWindows();

			unSmoothness += testData.unsmoothWindows.length;


			Matrices.write(testAnnotationMatrix, new File(Hyperparameters.saveDir+"/test"+Hyperparameters.fold+"_annotation"), false);
			//			Matrices.write(testAnnotationSigMatrix, new File(Hyperparameters.saveDir+"/test"+Hyperparameters.fold+"sig_annotation"), false);

			//			Matrices.write(testSampleDiffMatrix, new File(Hyperparameters.saveDir+"/test"+Hyperparameters.fold+"sampleDiff") , false);

			Matrices.write(accuracyMatrix, new File(Hyperparameters.saveDir+"/accuracies_"+Hyperparameters.fold), false);
			System.out.println();
		}

		float[][] unSmoothnessArray = new float[1][1];
		unSmoothnessArray[0][0] = unSmoothness/Hyperparameters.countSwallow;

		Matrix unSmoothnessMatrix = DefaultMatrix.wrap(unSmoothnessArray);

		Matrix sampleDifferencesMatrix = DefaultMatrix.wrap(sampleDifferences);

		Matrices.write(sampleDifferencesMatrix, new File(Hyperparameters.saveDir+"/sampleDifferences") , false);








		float[][] overAllAccuracies = new float[1][2];

		overAllAccuracies[0][0] = (float) (overAllTrainAcc/Hyperparameters.countSwallow);
		overAllAccuracies[0][1] = (float) (overAllTestAcc/Hyperparameters.countSwallow);
		Matrix overAllAccMatrix = DefaultMatrix.wrap(overAllAccuracies);


		Matrices.write(overAllAccMatrix, new File(Hyperparameters.saveDir+"/overAllAcc"), false);

		Matrices.write(unSmoothnessMatrix, new File(Hyperparameters.saveDir+"/avgUnSmoothness"), false);



		AccuracyParse accp = new AccuracyParse();

		accp.setValues();
		accp.readAccuracies();
		accp.computeExpectationValue();
		accp.computeVariance();
		accp.wrapOverAllAcc();
		accp.saveAcc();

	}

}
