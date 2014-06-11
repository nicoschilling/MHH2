package de.ismll.secondversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import de.ismll.bootstrap.CommandLineParser;
import de.ismll.bootstrap.Parameter;
import de.ismll.database.dao.DataStoreException;
import de.ismll.exceptions.ModelApplicationException;
import de.ismll.mhh.analyze.ApplyOnTest;
import de.ismll.mhh.analyze.ApplySVMHMM;
import de.ismll.mhh.analyze.GridSearchEval;
import de.ismll.mhh.io.DataInterpretation;
import de.ismll.mhh.io.Parser;
import de.ismll.mhh.methods.ApplyModel;
import de.ismll.mhh.methods.EvaluateModel;
import de.ismll.mhh.methods.MHHModelFactory;
import de.ismll.runtime.files.NoQueryFoundException;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.impl.DefaultVector;
import de.ismll.table.projections.ColumnSubsetMatrixView;
import de.ismll.table.projections.ColumnUnionMatrixView;
import de.ismll.table.projections.RowUnionMatrixView;

public class TestMain {
	
	
	
	
	public static void main(String[] args) throws ModelApplicationException, IOException, NoQueryFoundException, DataStoreException {
		
//		File targetDir = new File("/home/nico/acogpr/Results/AllSwallows/bestSampleDiff/Proband-2/Schluck-1");
		
//		System.out.println(Parser.readFileCompletely(new File(targetDir+"/end_sample")));
		

		
//		EvaluateModel eval = new EvaluateModel();
//		eval.evaluate();
		
		
//		DBEvaluator eval = new DBEvaluator();
//		
//		eval.getOptimalHyperparameters();
		
//		float[][] test = new float[10][988];
//		System.out.println(test.length);
//		System.out.println(test[2].length);
		
		
//		Database db;
//		try {
//			db = new Database();
//			db.setIterationTable("test1234");
//			db.init();
//			db.addIteration(1, 2, 3, 4, new float[] {1,3}, 2, 12, 2);
//		} catch (IOException | DataStoreException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		System.out.println("Done!");
		
		
		
		
//		AlgorithmController algcon = new AlgorithmController();
//		algcon.setAnnotationBaseDir("/home/nico/acogpr/manual_annotations/NormalAnnotations/");
//		algcon.setAnnotator("sm");
//		
//		DataInterpretation di = new DataInterpretation();
//		di.setDataInterpretation(new File("/home/nico/acogpr/Splits/AllSwallows/Proband1/split-1/train/Proband2Schluck1"));
//		di.run();
//		
//		int test = algcon.getPmax(di);
//		
//		System.out.println(test);

		
		

//		System.out.println("System starting!!!");
//		
//		AlgorithmController algcon = new AlgorithmController();
//		
//		System.out.println("Algorithm Controller initated!!");
//		
//		CommandLineParser.parseCommandLine(args, algcon);
//		
//		System.out.println("Command Line has been parsed! Will run Algcon now...");
//		
//
//		
//		algcon.run();
		
//		
//		
//		algcon.computeAnnotation(new File("/home/nico/acogpr/mhhPredictions/svmHMM/Intra/Proband4/split-5.out"));
		
//		algcon.buildDataforSVMStruct();
		
		
		
		
		
//		ApplyMHHModelImpl apply = new ApplyMHHModelImpl();
//		
//		
//		ReadFolder rf = new ReadFolder();
//		rf.setSchluckverzeichnis(new File("/home/nico/Documents/MHH/jobs/job86"));
//		
//		apply.setModelFile(new File("/home/nico/Documents/MHH/models/intraModels/learnedP1"));
//	
//		DT predict = apply.predict(rf, 39000);
////		DT predict = apply.predict(rf);
//		
//		System.out.println(predict.getPmaxSample());
//		
//		System.out.println("Accuracy is: " + predict.getAccuracy());
		
		
		
		
		
		
		
//		System.out.println(Vectors.toString(predict.getPredictions()));
		
		
//		ReadFolder rf = new ReadFolder();
//		
//		rf.setSchluckverzeichnis(new File("/tmp/job39"));
//		
//		rf.run();
		
//		for ( int proband = 2; proband < 12 ; proband++) {
//		
//		GridSearchEval eval = new GridSearchEval();
//		eval.setExperiment("inter_lap");
//		eval.setIterName("iter_lap_inter_new");
//		eval.setRunName("run_lap_inter_new");
//		eval.setLaplacian(true);
//		eval.setProband(proband);
//		
//		eval.evaluateGrid(args);
//
//		}
//		
		
//		for ( int proband = 2; proband < 12 ; proband++) {
//			ApplySVMHMM apply = new ApplySVMHMM();
//			apply.setExperiment("Inter");
//			apply.setProband(proband);
//			apply.setSaveExperiment("SVM-Inter");
//			
//			apply.applySVMHMM(args);
//		}
		

//		for (int proband = 8; proband < 9; proband++) {
//			ApplyOnTest apply = new ApplyOnTest();
//			apply.setExperimentName("intra_lap");
//			apply.setExperimentString("IntraProband");
//			apply.setProband(proband);
//			apply.setLaplacian(true);
//			
//			apply.applyOnTest(args);
//		}
		

		
		
		
	}



	

}
