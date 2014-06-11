package de.ismll.secondversion;

import java.util.ArrayList;

import de.ismll.table.Matrix;

public class SwallowDS {
	
	final static String LINE_SEPARATOR = System.lineSeparator();
	
	// Dataset for a single swallow
	public Matrix data;
	public Matrix labels;
	public Matrix ruheDruck;
	public Matrix ruheDruckLabels;
	public Matrix instanceWeights;
	public ArrayList<Integer> throwAway;

	public String toString() {
		String datadesc;
		String labeldesc;
		if (data == null) {
			datadesc = "No data given";
		} else {
			datadesc = "Data Matrix of dimension " + data.getNumRows() + " x " + data.getNumColumns();
		}
		if (labels== null) {
			labeldesc = "No labels given";
		} else {
			labeldesc = "Label Matrix of dimension " + labels.getNumRows() + " x " + labels.getNumColumns();
		}
		return "SwallowDS container:" + LINE_SEPARATOR + datadesc + LINE_SEPARATOR + labeldesc; 
	}
}