package de.ismll.mhh.analyze;

import static de.ismll.secondversion.DatasetFormat.COL_LABEL_IN_SAMPLE2LABEL;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.database.dao.DataStoreException;
import de.ismll.evaluation.Accuracy;
import de.ismll.exceptions.ModelApplicationException;
import de.ismll.mhh.io.DataInterpretation;
import de.ismll.secondversion.AlgorithmController;
import de.ismll.secondversion.SwallowDS;
import de.ismll.secondversion.ApplyMHHModelImpl;
import de.ismll.secondversion.Database;
import de.ismll.secondversion.DatasetFormat;
import de.ismll.secondversion.ReadSplit;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.impl.DefaultVector;

public class ApplySVMHMM {
	
	private int proband;
	private String experiment;
	private String saveExperiment;

	private static Logger log = LogManager.getLogger(ApplySVMHMM.class);

	public void applySVMHMM(String[] args) throws IOException, DataStoreException, ModelApplicationException {

		
		
		int windowExtent;
		
		int[] windowSizes = new int[] { 10 , 75, 150 };


		String folder = "/home/nico/acogpr/mhhPredictions/svmHMM/"+getExperiment()+"/Proband"+getProband();


		Accuracy acc = new Accuracy();
		
		Database db = new Database();
		db.init();


		int swallows = 10;


		



		for (int split = 1; split <=swallows ; split++) {

			Vector validationPredictions = null;
			Vector testPredictions = null;


			try {
				validationPredictions = Vectors.readDense(new File(folder+"/split-"+split+".val"));
				testPredictions = Vectors.readDense(new File(folder+"/split-"+split+".test"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (int i = 0; i < validationPredictions.size() ; i++) {
				if (validationPredictions.get(i) == 2) {
					validationPredictions.set(i, -1);
				}
			}

			for (int i = 0; i < testPredictions.size() ; i++) {
				if (testPredictions.get(i) == 2) {
					testPredictions.set(i, -1);
				}
			}





			ReadSplit rs = new ReadSplit(new File("/home/nico/acogpr/Splits/"+experiment+"Proband/"
					+ "Proband"+proband+"/split-"+split));

			rs.run();

			for (int i = 0; i < windowSizes.length ; i++) {
				windowExtent = windowSizes[i];



				ApplyMHHModelImpl apply = new ApplyMHHModelImpl();
				
				apply.setAnnotator("mj");
				apply.setAnnotationBaseDir("/home/nico/acogpr/manual_annotations/NormalAnnotations/");


				Vector[] validationPredictionsArray = new Vector[rs.validationFolders.length];


				int count = 0;

				// Get the validation Folders

				float[] validationAccuracy = new float[rs.validationFolders.length];
				float[] validationSampleDiff = new float[rs.validationFolders.length];

				for (int val = 0; val < rs.validationFolders.length ; val++) {
					DataInterpretation valFolder = rs.validationFolders[val];
					
					int valAnnotation = apply.getAnnotation(valFolder);
					int relValAnnotation = valAnnotation - valFolder.getFirstSample();

					SwallowDS d = apply.preprocessTestSwallow(valFolder, valAnnotation, true, true);
					Matrix valData = d.data;
					Matrix valLabels = d.labels;
					
					int samples = valData.getNumRows();
					
					
					validationPredictionsArray[val] = new DefaultVector(samples);

					for (int row = 0; row < samples ; row++) {
						validationPredictionsArray[val].set(row, validationPredictions.get(row + count));
					}

					count += samples;


					Matrix[] sample2Labels = AlgorithmController.createSample2Labels(d.data);

					Matrix predictedS2L = new DefaultMatrix(sample2Labels[0]);
					Matrix averageS2L = new DefaultMatrix(sample2Labels[1]);

					for (int row = 0; row < predictedS2L.getNumRows(); row++) {
						predictedS2L.set(row, COL_LABEL_IN_SAMPLE2LABEL, validationPredictionsArray[val].get(row));
					}

					AlgorithmController.computeSample2avgLabel(windowExtent, predictedS2L, averageS2L);
					
					float validationAcc = acc.evaluate(Vectors.col(valLabels, DatasetFormat.COL_LABEL_IN_LABELS),
							validationPredictionsArray[val]);

					int predictedAnnotation = AlgorithmController.predictAnnotation(averageS2L, log);
					
					int valSampleDifference = Math.abs(predictedAnnotation-relValAnnotation);
					
					validationAccuracy[val] = validationAcc;
					validationSampleDiff[val] = valSampleDifference;


				}
				float validationAcc = 0;
				float validationSampleDifference = 0;

				for (int k = 0; k < validationAccuracy.length ; k++) {
				validationAcc += validationAccuracy[k];
				validationSampleDifference += validationSampleDiff[k];
				}
				
				validationAcc = validationAcc/validationSampleDiff.length;
				validationSampleDifference = validationSampleDifference/validationSampleDiff.length;
				


				
				

				DataInterpretation testSwallow = rs.testFolders[0];



				int testAnnotation = apply.getAnnotation(testSwallow);
				int relativeTestAnnotation = testAnnotation - testSwallow.getFirstSample();

				SwallowDS d = apply.preprocessTestSwallow(testSwallow, testAnnotation, true, true);
				Matrix testData=d.data;
				Matrix testLabels = d.labels;
				
				float testAccuracy = acc.evaluate(Vectors.col(testLabels, DatasetFormat.COL_LABEL_IN_LABELS), testPredictions);


				Matrix[] sampletoLabels = AlgorithmController.createSample2Labels(d.data);

				Matrix predictedS2L = new DefaultMatrix(sampletoLabels[0]);
				Matrix averageS2L = new DefaultMatrix(sampletoLabels[1]);

				for (int row = 0; row < predictedS2L.getNumRows(); row++) {
					predictedS2L.set(row, COL_LABEL_IN_SAMPLE2LABEL, testPredictions.get(row));
				}



				AlgorithmController.computeSample2avgLabel(windowExtent, predictedS2L, averageS2L);

				int predictedAnnotation = AlgorithmController.predictAnnotation(averageS2L, log);
				
				float testSampleDiff = Math.abs(relativeTestAnnotation - predictedAnnotation);

				// IN DATENBANK SCHREIBEN

				db.addEvaluation(saveExperiment, proband, split, windowExtent, validationAcc,
						validationSampleDifference, testAccuracy, testSampleDiff, predictedAnnotation, relativeTestAnnotation);
				
				
				
				

			}

		}




	}

	public int getProband() {
		return proband;
	}

	public void setProband(int proband) {
		this.proband = proband;
	}

	public String getExperiment() {
		return experiment;
	}

	public void setExperiment(String experiment) {
		this.experiment = experiment;
	}

	public String getSaveExperiment() {
		return saveExperiment;
	}

	public void setSaveExperiment(String saveExperiment) {
		this.saveExperiment = saveExperiment;
	}

}
