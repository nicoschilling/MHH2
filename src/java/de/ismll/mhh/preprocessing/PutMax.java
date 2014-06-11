package de.ismll.mhh.preprocessing;

import java.io.File;
import java.io.IOException;

import de.ismll.bootstrap.Parameter;
import de.ismll.mhh.io.Parser;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.impl.DefaultMatrix;

public class PutMax implements Runnable{

	@Parameter(cmdline="input", description="is a file")
	private File input;
	@Parameter(cmdline="output", description="is also a file")
	private File output;
	public File getOutput() {
		return output;
	}
	public void setOutput(File output) {
		this.output = output;
	}
	public File getInput() {
		return input;
	}
	public void setInput(File input) {
		this.input = input;
	}
	@Override
	public void run() {
		Matrix readCSVFile;
		try {
			readCSVFile = Parser.readCSVFile(input);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		int numRows = readCSVFile.getNumRows();
		int numColumns = readCSVFile.getNumColumns();
		Matrix max = new DefaultMatrix(numRows, 2);
		
		for (int r = 0; r < numRows; r++) {
			max.set(r, 0, readCSVFile.get(r, 0));
			float maxValue = readCSVFile.get(r, 1);
			for (int c = 2; c < numColumns; c++) {
				maxValue = Math.max(maxValue, readCSVFile.get(r, c));
			}
			max.set(r, 1, maxValue);
			
			
		}
		
		try {
			Matrices.write(max, output, false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
