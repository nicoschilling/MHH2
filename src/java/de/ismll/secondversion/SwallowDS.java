package de.ismll.secondversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.table.Matrices;
import de.ismll.table.Matrix;

public class SwallowDS {

	protected Logger log = LogManager.getLogger(getClass());

	final static String LINE_SEPARATOR = System.lineSeparator();

	// Dataset for a single swallow
	public Matrix data;
	public Matrix labels;
	// public Matrix ruheDruck;
	// public Matrix ruheDruckLabels;
	public Matrix instanceWeights;
	public ArrayList<Integer> throwAway;

	private int absoluteIdxOfAnnotation;

	public String toString() {
		String datadesc;
		String labeldesc;
		if (data == null) {
			datadesc = "No data given";
		} else {
			datadesc = "Data Matrix of dimension " + data.getNumRows() + " x " + data.getNumColumns();
		}
		if (labels == null) {
			labeldesc = "No labels given";
		} else {
			labeldesc = "Label Matrix of dimension " + labels.getNumRows() + " x " + labels.getNumColumns();
		}
		return "SwallowDS container:" + LINE_SEPARATOR + datadesc + LINE_SEPARATOR + labeldesc;
	}

	public void serialize(File directory, String filename) throws IOException {
		if (!directory.isDirectory()) {
			throw new IOException("The first parameter needs to be a directory");
		}
		File datafile = new File(directory, filename + ".data.csv");
		log.info("Writing data file to " + datafile);
		Matrices.write(data, datafile);
		
		
	}

	public int getAbsoluteIdxOfAnnotation() {
		return absoluteIdxOfAnnotation;
	}

	public void setAbsoluteIdxOfAnnotation(int absoluteIdxOfAnnotation) {
		this.absoluteIdxOfAnnotation = absoluteIdxOfAnnotation;
	}
}