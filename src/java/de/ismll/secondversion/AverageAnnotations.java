package de.ismll.secondversion;

import java.io.File;
import java.io.IOException;

import de.ismll.mhh.io.Parser;
import de.ismll.myfm.util.IO;
import de.ismll.table.Matrix;

public class AverageAnnotations {

	public static void main(String[] args) throws IOException {
		
		String annotationDir = "/home/nico/acogpr/manual_annotations/NormalAnnotations";
		
		for (int proband = 1; proband <= 16 ; proband++) {
			
			String[][] annotations = IO.readData(annotationDir+File.separator+proband+"-sm.tsv","\t");
			
			String[][] writeAnnotations = new String[10][8];
			
			for (int i = 0; i < 10; i++) {
				for (int j = 0; j < 8; j++) {
					writeAnnotations[i][j] = "";
				}
			}
			
			Matrix annSimone = Parser.readAnnotations(new File(annotationDir+File.separator+proband+"-sm.tsv"), 50);
			Matrix annMichael = Parser.readAnnotations(new File(annotationDir+File.separator+proband+"-mj.tsv"), 50);
			
			for (int swallow=1; swallow <= 10; swallow++) {
				
				int sm = (int) annSimone.get(swallow-1, Parser.ANNOTATION_COL_RESTITUTIONSZEIT_SAMPLE);
				int mj = (int) annMichael.get(swallow-1, Parser.ANNOTATION_COL_RESTITUTIONSZEIT_SAMPLE);
				
				int first = (int) annMichael.get(swallow-1, 0);
				
				int average = sm + mj;
				average = average/2;
				
				String averageAsTime = Parser.sample2Time(average, 50);
				
				System.out.println("Simones Annotation: " + sm);
				System.out.println("Michaels Annotation: " + mj);
				System.out.println("Average: " + average);
				System.out.println("Average as time: " + averageAsTime);
				
				writeAnnotations[swallow-1][0] = String.valueOf(first);
				writeAnnotations[swallow-1][Parser.ANNOTATION_COL_RESTITUTIONSZEIT_SAMPLE] = String.valueOf(averageAsTime);
			}
			
			IO.writeData(annotationDir+File.separator+proband+"-av.tsv", "\t", writeAnnotations);
			

			
			
			
			
		}

	}

}
