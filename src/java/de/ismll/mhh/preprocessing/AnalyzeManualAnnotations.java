package de.ismll.mhh.preprocessing;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.mhh.io.Parser;
import de.ismll.mhh.io.DataInterpretation;
import de.ismll.mhh.io.SwallowData;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultVector;

public class AnalyzeManualAnnotations {

	static Logger l = LogManager.getLogger(AnalyzeManualAnnotations.class);

	public static void main(String[] args) throws IOException {
		File annotationsDir = new File("M:/manual_annotations");
		File dataDir = new File("M:/Proband1");
		
		Matrix mj_1 = Parser.readAnnotations(new File(annotationsDir,"1-mj.tsv"), 50);
		Matrix mj_2 = Parser.readAnnotations(new File(annotationsDir,"2-mj.tsv"), 50);
		Matrix sm_1 = Parser.readAnnotations(new File(annotationsDir,"1-sm.tsv"), 50);
		Matrix sm_2 = Parser.readAnnotations(new File(annotationsDir,"2-sm.tsv"), 50);

		
		
		Matrix use = sm_1;
		
		DefaultVector dv = new DefaultVector(1);
		for (int r = 0; r < use.getNumRows(); r++) {
			DataInterpretation rf = new DataInterpretation();
			rf.setDataInterpretation(new File(dataDir, "00506534Schluck" + (r+1) + ".ASC.data")); // 1
//			rf.setSchluckverzeichnis(new File(dataDir, "00511910Schluck" + (r+1) + ".ASC.data")); // 2
			rf.run();
			
			dv.set(0, use.get(r, Parser.ANNOTATION_COL_RESTITUTIONSZEIT_SAMPLE)-rf.getDruck().get(0, 0));
			Vectors.write(dv, new File("M:/manual_annotations/Proband1/SM/" + r), false);
		}
		
		
		
		
//		l.info("Statistiken Proband 1");
//		compare (mj_1, sm_1);
//
//		l.info("Statistiken Proband 2");
//		compare (mj_2, sm_2);
//
//		System.out.println(Matrices.toString(mj_1));
		
	}
	
	private static void compare (Matrix m1, Matrix m2) {
		l.info("Korrelation zwischen Maximaldruck-Zeitpunkten: " + Vectors.correlation(Matrices.col(m1, Parser.ANNOTATION_COL_PMAX_SAMPLE), Matrices.col(m2, Parser.ANNOTATION_COL_PMAX_SAMPLE)));
		l.info("Korrelation zwischen Maximaldruck-Druecken: " + Vectors.correlation(Matrices.col(m1, Parser.ANNOTATION_COL_PMAX_PRESSURE), Matrices.col(m2, Parser.ANNOTATION_COL_PMAX_PRESSURE)));
		
		l.info("Korrelation zwischen Wieder-Erreichen des Ruhedrucks: " + Vectors.correlation(Matrices.col(m1, Parser.ANNOTATION_COL_RESTITUTIONSZEIT_SAMPLE), Matrices.col(m2, Parser.ANNOTATION_COL_RESTITUTIONSZEIT_SAMPLE)));
		l.info("Korrelation zwischen den Deltas (die Dauer des Wiedererreichens): " + Vectors.correlation(Matrices.col(m1, Parser.ANNOTATION_COL_RESTITUTIONSZEIT_DELTA), Matrices.col(m2, Parser.ANNOTATION_COL_RESTITUTIONSZEIT_DELTA)));
		
	}
	
}
