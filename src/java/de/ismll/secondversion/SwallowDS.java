package de.ismll.secondversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import de.ismll.table.Matrices;
import de.ismll.table.Matrix;

public class SwallowDS {

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

	public void serialize(File serializeTheData, int scheme, String format) throws IOException {
		switch (scheme) {
		case 1:
			if (!serializeTheData.isDirectory()) {
				throw new IOException("The file parameter needs to be a directory when using scheme '1'!");
			}
			Matrices.write(data, new File(serializeTheData, format + ".data.csv"));
			
			break;
		default:
			System.out.println("Unknown serialization scheme!");
		}

	}

	public int getAbsoluteIdxOfAnnotation() {
		return absoluteIdxOfAnnotation;
	}

	public void setAbsoluteIdxOfAnnotation(int absoluteIdxOfAnnotation) {
		this.absoluteIdxOfAnnotation = absoluteIdxOfAnnotation;
	}
}