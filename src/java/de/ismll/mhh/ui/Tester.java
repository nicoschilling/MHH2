package de.ismll.mhh.ui;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

import de.ismll.mhh.io.Parser;
import de.ismll.secondversion.IntRange;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;

public class Tester {

	public static void main(String[] args) throws IOException {
		
		File in = new File("M:/Proband4/00519402Schluck5.ASC.data/data.csv");
		
		JSchluckdiagramm s = new JSchluckdiagramm();
		
		Matrix readCSVFile = Parser.readCSVFile(in);
		
		Matrix m = Matrices.cols(readCSVFile, IntRange.convert("1,20").getUsedIndexes());
		
		s.setData(m);
		System.out.println(Matrices.toString(readCSVFile));
		JFrame jf = new JFrame();
		
		jf.add(s);
		jf.setVisible(true);
		
		
	}
}
