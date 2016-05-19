package de.ismll.secondversion;

public class DatasetFormat {


	public static final int COL_SWALLOW_IDX = 0;
	public static final int COL_ABS_SAMPLE_IDX = 1;
	public static final int COL_REL_SAMPLE_IDX = 2;
	public static final int COL_ANNOTATION_SAMPLE_IDX = 3;
	public static final int COL_PMAX_SAMPLE_IDX = 4;

	/*sample column in labels file*/
	public static final int COL_SAMPLE_IN_LABELS = 0;
	/*labels column in labels file*/
	public static final int COL_LABEL_IN_LABELS = 1;

	/*
	 * when using the method AlgorithmController.createSample2Labels, this column denotes the labels (either predicted or actual ones)
	 */
	public static final int COL_LABEL_IN_SAMPLE2LABEL = 5;

	public static final int LABEL_NICHT_SCHLUCK = -1;
	public static final int LABEL_SCHLUCK = 1;

	/*
	 * meta columns (in order) in data files
	 */
	public static final int[] META_COLUMNS = {
		COL_SWALLOW_IDX,
		COL_ABS_SAMPLE_IDX,
		COL_REL_SAMPLE_IDX,
		COL_ANNOTATION_SAMPLE_IDX,
		COL_PMAX_SAMPLE_IDX		
	};
	
	/*
	 * meta columns (in order) in data files
	 */
	public static final int[] SAMPLE_2_LABELS_META_COLUMNS = {
		COL_SWALLOW_IDX,
		COL_ABS_SAMPLE_IDX,
		COL_REL_SAMPLE_IDX,
		COL_ANNOTATION_SAMPLE_IDX,
		COL_PMAX_SAMPLE_IDX,
		COL_LABEL_IN_SAMPLE2LABEL
	};
}
