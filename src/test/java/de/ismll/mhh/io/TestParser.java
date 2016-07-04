package de.ismll.mhh.io;

import java.io.File;
import java.io.IOException;

import de.ismll.table.Matrices;
import de.ismll.table.Matrix;

public class TestParser {

	public static void main(String[] args) throws IOException {
		File annotationFile = new File("M:/manual_annotations/1-sm.tsv");
		Matrix readAnnotations = Parser.readAnnotations(annotationFile, 50);
		System.out.println(Matrices.toString(readAnnotations));
	}
	
}
