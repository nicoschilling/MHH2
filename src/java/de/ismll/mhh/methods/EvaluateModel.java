package de.ismll.mhh.methods;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import de.ismll.bootstrap.Parameter;
import de.ismll.database.dao.DataStoreException;
import de.ismll.mhh.io.Parser;
import de.ismll.secondversion.Database;
import de.ismll.storage.FileStorageTarget;
import de.ismll.storage.StorageTargetFactory;
import de.ismll.table.Matrix;
import de.ismll.table.Vectors;

public class EvaluateModel {
	
	@Parameter(cmdline="what")
	String what="bestSampleDiff";
	
	@Parameter(cmdline="experiment")
	String experiment="normal_on_normal";
	
	@Parameter(cmdline="resultDir")
	String resultDir="/home/nico/acogpr/Results/Normal-on-Acid-Inter-Experiment/split-4";
	
	@Parameter(cmdline="splitDir")
	String splitDir="/home/nico/acogpr/Splits/Normal-on-Acid-Inter-Experiment/split-4";
	
	
	
	
	
	int samplerate = 50;

	
	public void evaluate() throws IOException, DataStoreException {
		
		Database db;
		db = new Database();
		db.setPredictionTable(experiment+"_" + what.toLowerCase());
		db.init();
		
		for (int proband = 2 ; proband < 17; proband++) {
			
			for (int swallow = 1 ; swallow < 11 ; swallow++) {
				
				File targetDir = new File("/home/nico/acogpr/Results/AllSwallows/Proband"+proband+"Schluck"+swallow+"/bestSampleDiff");
				
				
				int predictedAnnotation = Integer.parseInt(Parser.readFileCompletely(new File(targetDir+"/end_sample")));
				String predictedAnnotationTime = Parser.sample2Time(predictedAnnotation, samplerate);
				
				int predictedAbsoluteAnnotation = Integer.parseInt(Parser.readFileCompletely(new File(targetDir+"/absolute_end_sample")));
				String predictedAbsoluteAnnotationTime = Parser.sample2Time(predictedAbsoluteAnnotation, samplerate);
				
				int pmax = Integer.parseInt(Parser.readFileCompletely(new File(targetDir+"/pmax_sample")));
				String pmaxTime = Parser.sample2Time(pmax, samplerate );
				
				int annotation = Integer.parseInt(Parser.readFileCompletely(new File(targetDir+"/true_sample")));
				String annotationTime = Parser.sample2Time(annotation, samplerate);
				
				int sampleDifference = Math.abs(annotation - predictedAbsoluteAnnotation);
				String timeDifference = Parser.sample2Time(sampleDifference, samplerate);
				
				
//				int restitutionTimeInSamples = Math.abs(pmax - annotation );
//				String restitutionTime = Parser.sample2Time(restitutionTimeInSamples, samplerate);
				
				int predictedRestitutionTimeInSamples = Math.abs(pmax - predictedAnnotation);
				String predictedRestitutionTime = Parser.sample2Time(predictedRestitutionTimeInSamples, samplerate);
					
				
				
				db.addPrediction(proband, swallow, pmax, pmaxTime, annotation, predictedAnnotation, sampleDifference, annotationTime,
						predictedAnnotationTime, timeDifference, 0, "empty", predictedRestitutionTimeInSamples, predictedRestitutionTime);
				
				
				
				
				
				
			}
			
			
			
			
			
		}
		
		
		
	}

}
