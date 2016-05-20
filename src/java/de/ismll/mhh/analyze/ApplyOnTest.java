package de.ismll.mhh.analyze;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.database.SqlStatement;
import de.ismll.database.dao.IContentHolder;
import de.ismll.database.dao.DataStoreException;
import de.ismll.database.dao.IEntityBacked;
import de.ismll.database.pgsql.PostgresSQL;
import de.ismll.exceptions.ModelApplicationException;
import de.ismll.mhh.io.DataInterpretation;
import de.ismll.secondversion.ApplyMHHModel;
import de.ismll.secondversion.ApplyMHHModelImpl;
import de.ismll.secondversion.AnalysisResult;
import de.ismll.secondversion.Database;
import de.ismll.secondversion.IntRange;
import de.ismll.secondversion.ReadSplit;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;

public class ApplyOnTest {

	private int proband;
	private boolean laplacian;
	private String experimentName;
	private String annotationBaseDir;
	private String experimentString;

	private static Logger log = LogManager.getLogger(ApplyOnTest.class);



	public void applyOnTest(String[] args) throws DataStoreException, IOException, ModelApplicationException {

		PostgresSQL pg = new PostgresSQL("schilling");

		Database db = new Database();
		db.init();

		String model_parameters = null;




		annotationBaseDir = "/home/nico/Documents/MHH/manual_annotations/NormalAnnotations/";


		//		String experiment = uExperiment.toLowerCase(); //whatever name that fits the experiment names	



		int swallows = 10;



		// Ueber alle Splits des Probanden

		for (int split = 1; split <= swallows ; split++) {



			IEntityBacked query = pg.query(new SqlStatement("SELECT model_parameters,window_extent, accuracy FROM experiment3"
					+ " WHERE proband = " + getProband() 
					+ " AND split = " + split 
					+ " AND experiment_name = '" + getExperimentName() +"'" ));

			int windowExtent = 0;
			float accuracy = 0;

			while (query.hasNext()) { 
				IContentHolder next = query.next();
				accuracy = next.get("accuracy", Float.class);
				model_parameters = next.get("model_parameters", String.class);
				windowExtent = next.get("window_extent", Integer.class);
			}

			query.close();

			Vector parameterVector = Vectors.convert(model_parameters);


			// ADD THIS ONLY WHEN OPTIMIZATION WAS FOR ACCURACY!!!! ALSO THE LOOP OVER WINDOWEXTENTS!!!
			
			int[] windowExtents = new int[] { 10,75,150 };

			for (int k = 0; k < windowExtents.length ; k++) {

				ApplyMHHModelImpl apply = new ApplyMHHModelImpl();

				IntRange columnSelector = new IntRange("33,166");

				apply.setParameters(new Vector[] {parameterVector });
				apply.setWindowExtent(windowExtents[k]);
				apply.setAnnotationBaseDir(annotationBaseDir);
				apply.setAnnotator("mj");
				apply.setColumnSelector(columnSelector);
				apply.setSkipBetween(true);
				apply.setSkipLeading(true);
				
				//VALIDATION
				
				String readSplitValidationString = "/home/nico/acogpr/Splits/"+getExperimentString()+"/Proband"+getProband()
						+ "/split-"+split+"/validation";
				
				File validationDir = new File(readSplitValidationString);
				
				File[] validationDirs = validationDir.listFiles();
				
				float[] validationAccuracies = new float[validationDirs.length];
				float[] validationSampleDiffs = new float[validationDirs.length];
				
				for (int t = 0; t < validationDirs.length ; t++) {
					DataInterpretation rf = new DataInterpretation();
					rf.setDataInterpretation(validationDirs[t]);
//					rf.run();
					AnalysisResult out = apply.predictWithParameters(rf, parameterVector);
					validationAccuracies[t] = out.getAccuracy();
					validationSampleDiffs[t] = out.getSampleDifference();
				}
				
				float sumValAcc = 0;
				float sumValDiff = 0;
				for (int i = 0; i < validationDirs.length ; i++) {
					sumValAcc += validationAccuracies[i];
					sumValDiff += validationSampleDiffs[i];
				}

				float avgValAccuracy = sumValAcc/validationDirs.length;
				float avgValSampleDiff = sumValDiff/validationDirs.length;
				
				


				//TEST!!

				String readSplitTestString = "/home/nico/Documents/MHH/NormalSwallows/Proband"+getProband()+"/Schluck"+split+"/";

				//			String testString = "/home/nico";
				//			File testDir = new File(testString);
				//			File[] content = testDir.listFiles();

				File readSplitTestDir = new File(readSplitTestString);


				File[] allTestDirs = new File[] {readSplitTestDir};

				float[] testAccuracies = new float[allTestDirs.length];
				float[] testSampleDiffs = new float[allTestDirs.length];


				for (int i = 0; i < allTestDirs.length ; i++) {
					DataInterpretation rf = new DataInterpretation();
					rf.setDataInterpretation(allTestDirs[i]);
					rf.run();
					AnalysisResult out = apply.predictWithParameters(rf, parameterVector);
					System.out.println("Accuracy on Test: " + out.getAccuracy());
					System.out.println("SampleDiff on Test: " + out.getSampleDifference()) ;
					testAccuracies[i] = out.getAccuracy();
					testSampleDiffs[i] = out.getSampleDifference();

				}


				float sumAcc = 0;
				float sumDiff = 0;
				for (int i = 0; i < allTestDirs.length ; i++) {
					sumAcc += testAccuracies[i];
					sumDiff += testSampleDiffs[i];
				}

				float avgTestAccuracy = sumAcc/allTestDirs.length;
				float avgTestSampleDiff = sumDiff/allTestDirs.length;


				System.out.println("Split is: " + split);
				System.out.println("Average Sample Difference on Test is: " + avgTestSampleDiff);
				System.out.println("Average Accuracy on Test is: " + avgTestAccuracy);


				//WRITE STUFF INTO DATABASE!!!!

				db.addEvaluation(getExperimentName()+"_test", getProband(), split, windowExtents[k], avgValAccuracy,
						avgValSampleDiff, avgTestAccuracy, avgTestSampleDiff,
						0, 0);

			}

		}

	}

	public int getProband() {
		return proband;
	}

	public void setProband(int proband) {
		this.proband = proband;
	}

	public boolean isLaplacian() {
		return laplacian;
	}

	public void setLaplacian(boolean laplacian) {
		this.laplacian = laplacian;
	}

	public String getExperimentName() {
		return experimentName;
	}

	public void setExperimentName(String experimentName) {
		this.experimentName = experimentName;
	}

	public String getExperimentString() {
		return experimentString;
	}

	public void setExperimentString(String experimentString) {
		this.experimentString = experimentString;
	}



}
