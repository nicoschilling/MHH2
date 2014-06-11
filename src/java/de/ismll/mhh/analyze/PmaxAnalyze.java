package de.ismll.mhh.analyze;

import java.io.File;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.database.SqlStatement;
import de.ismll.database.dao.IContentHolder;
import de.ismll.database.dao.DataStoreException;
import de.ismll.database.dao.IEntityBacked;
import de.ismll.database.pgsql.PostgresSQL;
import de.ismll.exceptions.ModelApplicationException;
import de.ismll.mhh.io.DataInterpretation;
import de.ismll.secondversion.AlgorithmController;
import de.ismll.secondversion.AlgorithmController.SwallowDS;
import de.ismll.secondversion.ApplyMHHModelImpl;
import de.ismll.secondversion.AnalysisResult;
import de.ismll.secondversion.DatasetFormat;
import de.ismll.table.Matrix;
import de.ismll.table.impl.DefaultMatrix;

public class PmaxAnalyze {

	private static Logger log = LogManager.getLogger(PmaxAnalyze.class);

	public static void main(String[] args) throws DataStoreException, ModelApplicationException {

		int swallows = 10;
		

		float[] sampleDiff =  null;

		float[] avgSampleDiff = new float[10];
		float[] conf = new float[10];

		PostgresSQL pg = new PostgresSQL("schilling");

		for (int proband = 2 ; proband <= 11 ; proband++) {

		

			sampleDiff = new float[swallows];
			
			for (int swallow = 1 ; swallow <= swallows ; swallow++) {

//				if (proband == 2 && swallow == 4) { continue;}

				IEntityBacked query = pg.query(new SqlStatement("SELECT true_annotation FROM evaluation WHERE proband = "+proband
						+ " AND split = " + swallow  + " AND experiment_name = 'SVM-Intra' "+ "LIMIT 1"));

				int trueAnnotation = 0;

				while(query.hasNext()) {
					IContentHolder next = query.next();
					trueAnnotation = next.get("true_annotation", Integer.class);
				}

				query.close();

				

				DataInterpretation schluck = new DataInterpretation();
				schluck.setDataInterpretation(new File("/home/nico/acogpr/NormalSwallows/Proband" + proband + "/" + "Schluck" + swallow ));

				schluck.run();

				ApplyMHHModelImpl apply = new ApplyMHHModelImpl();

				SwallowDS data = apply.preprocessSwallow(schluck, true, true);

				float[] pMaxCurve = AlgorithmController.concatenateForPmax(log, schluck, -1, true);

				int start = schluck.getRdStartSample() - schluck.getFirstSample();
				int end = schluck.getRdEndSample() - schluck.getFirstSample();

				int pMaxSample = (int) data.data.get(1, DatasetFormat.COL_PMAX_SAMPLE_IDX) - schluck.getFirstSample();

				System.out.println("Berechnetes PMAX Sample ist: " + pMaxSample);

				int length = end - start;

				double sum = 0;
				
				if (start < 0) { continue; }

				for (int i = 0; i < length ; i++) {
					sum += pMaxCurve[i+start];
				}

				double avgRestingPressure = sum/length;





				int startSearch = pMaxSample -1;
				
				if (startSearch < 0) { continue; }


				while (pMaxCurve[startSearch] > avgRestingPressure) {
					startSearch++;
					if (startSearch == pMaxCurve.length) { break;}
				}
				
				System.out.println("Wahre Annotation ist: " + trueAnnotation);
				System.out.println("Berechnete Annotation ist: "  + startSearch);


				int sampleDifference = Math.abs(startSearch - trueAnnotation);

				sampleDiff[swallow-1] = sampleDifference;


			}
			
			
			float sum = 0;

			for (int i = 0; i < sampleDiff.length ; i++) {
				
				sum += sampleDiff[i];
			}

			float avgSampleDifference = sum/sampleDiff.length;

			avgSampleDiff[proband-2] = avgSampleDifference;


			float varSum = 0;

			for (int j = 0; j < sampleDiff.length ; j++) {
				varSum += (sampleDiff[j] - avgSampleDifference)*(sampleDiff[j] - avgSampleDifference);
			}

			float std = (float) Math.sqrt(varSum/(sampleDiff.length -1));

			float confidence = (float) (1.96*std/Math.sqrt(sampleDiff.length));

			conf[proband-2] = confidence;



		}


		for (int k = 0; k < 10 ; k++) {

			System.out.println();
			System.out.println();
			System.out.println("Proband: " + (k+2));
			System.out.println("average Sample Difference: " + avgSampleDiff[k]/50);
			System.out.println("confidence: " + conf[k]/50);

		}

	}
}
