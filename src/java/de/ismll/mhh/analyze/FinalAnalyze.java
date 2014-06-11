package de.ismll.mhh.analyze;

import de.ismll.database.SqlStatement;
import de.ismll.database.dao.IContentHolder;
import de.ismll.database.dao.DataStoreException;
import de.ismll.database.dao.Datatypes;
import de.ismll.database.dao.IEntityBacked;
import de.ismll.database.pgsql.PostgresSQL;

public class FinalAnalyze {

	public static void main(String[] args) throws DataStoreException {
		PostgresSQL pg = new PostgresSQL("schilling");

		String experiment = "inter_lapbuff";
		
		
		int probandCount = 11;
		
		double[] avgAccuracies = new double[probandCount];
		double[] avgSampleDiff = new double[probandCount];
		
		double[] confAccuracies = new double[probandCount];
		double[] confSampleDiffs = new double[probandCount];
		
		double confAcc = 0;
		double confSampleDiff = 0;
		
		
		for (int proband = 2 ; proband <= probandCount ; proband++) {
			
			int swallows = 10;
			
			double[] testSampleDiff = new double[swallows];
			double[] testAccuracy = new double[swallows];
			
			for (int split = 1 ; split <= swallows ; split++) {
				
//				if (proband == 2 && split == 4) { continue; }
				
				IEntityBacked query = pg.query(new SqlStatement("SELECT * FROM evaluation WHERE proband = " + proband + 
						" AND split = " + split + " AND experiment_name = '"+ experiment + "' ORDER BY validation_sample_diff"
								+ " ASC LIMIT 1"));
				
				
				
				while (query.hasNext()) {
					IContentHolder next = query.next();
					testSampleDiff[split-1] =  next.get("test_sample_diff", Double.class);
					testAccuracy[split-1] = next.get("test_accuracy", Double.class);
				}
				
				query.close();
				
			}
			
			double avgAccuracy = 0;
			double avgSamplediff = 0;
			
			double varAccuracy = 0;
			double varSampleDiff = 0;
			
			for (int i = 0; i < testSampleDiff.length ; i++) {
				avgAccuracy += testAccuracy[i];
				avgSamplediff += testSampleDiff[i];
			}
			
			
			avgAccuracy = avgAccuracy/swallows;
			avgSamplediff = avgSamplediff/swallows;
			
			avgAccuracies[proband-1] = avgAccuracy;
			avgSampleDiff[proband-1] = avgSamplediff;
			
			
			
			
			for (int j = 0; j < testSampleDiff.length ; j++) {
				varAccuracy += (avgAccuracy - testAccuracy[j])*(avgAccuracy - testAccuracy[j]);
				varSampleDiff += (avgSamplediff - testSampleDiff[j])*(avgSamplediff - testSampleDiff[j]);
			}
			
			varAccuracy = varAccuracy/(swallows-1);
			varSampleDiff = varSampleDiff/(swallows-1);
			
			double stdAccuracy = Math.sqrt(varAccuracy);
			double stdSampleDiff = Math.sqrt(varSampleDiff);
			
			confAcc = 1.96*stdAccuracy/(Math.sqrt(swallows));
			confSampleDiff = 1.96*stdSampleDiff/(Math.sqrt(swallows));
			
			confAccuracies[proband-1] = confAcc;
			confSampleDiffs[proband-1] = confSampleDiff;
			
		}
		
		
		for (int j = 1 ; j <=probandCount ; j++) {
			System.out.println("Proband: " + j  );
			System.out.println();
			System.out.println("Average Accuracy for " + experiment + " at Proband " + j +  ": " + avgAccuracies[j-1]);
			System.out.println("Confidence Interval for Accuracy: " + confAccuracies[j-1]);
			System.out.println("Average SampleDiff for " + experiment + " at Proband " + j +  ": " + avgSampleDiff[j-1]/50);
			System.out.println("Confidence Interval for SampleDiff: " + confSampleDiffs[j-1]/50);
			System.out.println();
		}

	}

}
