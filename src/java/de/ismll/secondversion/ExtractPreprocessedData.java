package de.ismll.secondversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.bootstrap.Parameter;
import de.ismll.exceptions.ModelApplicationException;
import de.ismll.mhh.featureExtractors.AcidFeatureExtractor;
import de.ismll.mhh.featureExtractors.AllExtractor;
import de.ismll.mhh.featureExtractors.LowerExtractor;
import de.ismll.mhh.featureExtractors.LowerMiddleExtractor;
import de.ismll.mhh.featureExtractors.MiddleExtractor;
import de.ismll.mhh.featureExtractors.PatientFeatureExtractor;
import de.ismll.mhh.featureExtractors.TimeFeatureExtractor;
import de.ismll.mhh.featureExtractors.UpperExtractor;
import de.ismll.mhh.featureExtractors.UpperMiddleExtractor;
import de.ismll.mhh.io.Parser;
import de.ismll.mhh.io.DataInterpretation;
import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultBitVector;
import de.ismll.table.impl.DefaultIntVector;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.impl.DefaultVector;
import de.ismll.table.projections.ColumnSubsetMatrixView;
import de.ismll.table.projections.ColumnUnionMatrixView;
import de.ismll.table.projections.RowSubsetMatrixView;
import de.ismll.table.projections.VectorAsMatrixView;
import static de.ismll.secondversion.DatasetFormat.*;

/**
 * build data matrices, dependent on ReadSplit
 * 
 * @author Andre Busche
 * 
 */
public class ExtractPreprocessedData  implements Runnable{

	final static String LINE_SEPARATOR = System.lineSeparator();

	/**
	 * number of leading meta columns
	 */
	public static final int NUM_META_COLUMNS = META_COLUMNS.length;

	@Parameter(cmdline="applyFolder" , description="the folder to apply a learned model on")
	private String applyFolder;

	@Parameter(cmdline="splitFolder", description="the split folder ;-)")
	private ReadSplit readSplit;

	@Parameter(cmdline="weightLength", description="one-sided distance, that specifies, whether instance weights are smaller than 1")
	private float weightLength = 50;

	@Parameter(cmdline="annotator" , description="the annotator, either mj,sm or gemein")
	private String annotator="gemein";

	@Parameter(cmdline="includeRD", description="set to true if RD Data should be included")
	private boolean includeRD;

	@Parameter(cmdline="windowExtent", description="Hyperparameter: The length (one sided) that smoothes the predicted labels")
	private int windowExtent;

	@Parameter(cmdline="normalized", description="true, if normalized data should be used, false, otherwise.")
	private boolean normalize=true;

	@Parameter(cmdline="timeOrder")
	private int timeOrder = 3;

	@Parameter(cmdline="columnSelector", description="Optional. If given, selects a subset of columns. WARNING: If given, make sure to exclude, e.g., the sample Index (col 0)!")
	private IntRange columnSelector;

	@Parameter(cmdline="annotationBaseDir")
	private String annotationBaseDir;

	@Parameter(cmdline="splitNumber")
	private int splitNumber;

	@Parameter(cmdline="probandNumber")
	private int probandNumber;

	@Parameter(cmdline="useAcidFeatures")
	private boolean useAcidFeatures=true;
	
	@Parameter(cmdline="usePatientFeatures")
	private boolean usePatientFeatures=true;

	public String outputFolder;

	private int nrAttributes;


	protected Logger log = LogManager.getLogger(getClass());

	public MhhDataset data = new MhhDataset();

	private Matrix annotations;
	private boolean skipLeading=true;
	private boolean skipBetween=true;

	private Vector finalParameters;
	private float[] finalParametersArray;

	private AllExtractor extractor;

	private LowerExtractor lowerFeatureExtractor;

	private MiddleExtractor middleFeatureExtractor;

	private UpperExtractor upperFeatureExtractor;

	private LowerMiddleExtractor lowerMiddleFeatureExtractor;

	private UpperMiddleExtractor upperMiddleFeatureExtractor;

	@Parameter(cmdline="serializeTheData")
	private boolean serializeTheData;

	public ExtractPreprocessedData() {
		extractor = new AllExtractor();
		lowerFeatureExtractor = new LowerExtractor();
		middleFeatureExtractor = new MiddleExtractor();
		upperFeatureExtractor = new UpperExtractor();
		lowerMiddleFeatureExtractor = new LowerMiddleExtractor();
		upperMiddleFeatureExtractor = new UpperMiddleExtractor();
	}


	@Override
	public void run() {
		
		log.info("Working on " + readSplit.getSplitFolder());
		readSplit.run();

		File[] trainList = readSplit.trainList;

		log.info("Working on " + trainList.length + " training files");
		log.info("Working on " + columnSelector.getUsedIndexes().length + " predictor variables");

		if (columnSelector.getUsedIndexes() == null) {
			log.warn("Column Selector not yet used!");
		}

		log.info("Raw Data Object will be built with " + readSplit.trainFolders.length + " Training Swallows, "
				+ "with " + readSplit.testFolders.length + " Test Swallows, and with " + readSplit.validationFolders.length 
				+" Validation Swallows!");

		// TRAIN!

		for(int i = 0; i < readSplit.trainFolders.length; i ++) {
			log.info("Training data ... " + readSplit.trainFolders[i].getSwallowId());
			DataInterpretation folder = readSplit.trainFolders[i];

			int absoluteAnnotation = getAnnotation(folder);

			int pmax = getPmax(folder);

			SwallowDS d = null;
			try {
				d = preprocessSwallow(folder, absoluteAnnotation, pmax, skipLeading, skipBetween);
			} catch (ModelApplicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		// VALIDATION

		for(int i = 0; i < readSplit.validationFolders.length; i ++) {
			log.info("Validation data ... " + readSplit.validationFolders[i].getSwallowId());
			DataInterpretation folder = readSplit.validationFolders[i];

			int absoluteAnnotation = getAnnotation(folder);

			int pmax = getPmax(folder);

			SwallowDS d = null;
			try {
				d = preprocessSwallow(folder, absoluteAnnotation, pmax, skipLeading, skipBetween);
			} catch (ModelApplicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		// TEST

		for(int i = 0; i < readSplit.testFolders.length; i ++) {
			log.info("Test data ... " + readSplit.testFolders[i].getSwallowId());
			DataInterpretation folder = readSplit.testFolders[i];

			int absoluteAnnotation = getAnnotation(folder);

			int pmax = getPmax(folder);

			SwallowDS d = null;
			try {
				d = preprocessTestSwallow(folder, absoluteAnnotation, pmax, skipLeading, skipBetween);
			} catch (ModelApplicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private int getPmax(DataInterpretation folder) {
		int pmax = 0;

		int probandId = folder.getProband();
		int swallowId = folder.getSwallowId();

		String annotationPath = annotationBaseDir + File.separator + probandId + "-" + annotator + ".tsv";

		// Parser modifizieren!

		try {
			annotations = Parser.readAnnotations(new File(annotationPath), folder.getSamplerateAsInt());
			pmax = (int) annotations.get(swallowId-1, Parser.ANNOTATION_COL_PMAX_SAMPLE);
			log.info("Pmax is given, will continue...");
		} catch (IOException e) {
			File dataInterpretation = folder.getDataInterpretation();
			log.info("Pmax has not been provided for:  " + (null==dataInterpretation?"null":dataInterpretation.toString()));
			log.info("The annotation path is: " + annotationPath);
			pmax = -1;
		} catch (ArrayIndexOutOfBoundsException t){
			log.info("There was an array out of bounds when reading annotations.... did you access the right file?");
			pmax = -1;
		}

		if(pmax == 0) { pmax = -1;}

		return pmax;
	}

	/**
	 * Returns the absolute Annotation for a given Swallow in a Read Folder Object. If there is no annotation, "NaN" will be returned
	 * @param folder
	 * @return
	 */
	private int getAnnotation(DataInterpretation folder) {
		int restitutionszeitSample;

		int probandId = folder.getProband();
		int swallowId = folder.getSwallowId();

		String annotationPath = annotationBaseDir + File.separator + probandId + "-" + annotator + ".tsv";

		try {
			annotations = Parser.readAnnotations(new File(annotationPath), folder.getSamplerateAsInt());
			restitutionszeitSample = (int) annotations.get(swallowId-1, Parser.ANNOTATION_COL_RESTITUTIONSZEIT_SAMPLE);
			log.info("Annotation is given, will continue...");
		} catch (IOException e) {
			log.info("Annotations have not been provided for:  " + folder.getDataInterpretation().toString());
			log.info("The annotation path is: " + annotationPath);
			restitutionszeitSample = (int) Double.NaN;
		}
		catch (ArrayIndexOutOfBoundsException t){
			log.info("There was an array out of bounds when reading annotations.... did you access the right file?");
			restitutionszeitSample = (int) Double.NaN;
		}

		return restitutionszeitSample;
	}

	/**
	 * Reads a (training) swallow; preprocessing the data, s.t.:
	 * 
	 * <ol>
	 * <li>Data is aggregated (e.g., concatenating FFT and Pressure samples, adding metadata, see {@link #concatenate(DataInterpretation)})
	 * <li>Data is narrowed (e.g., removing non-informative samples (between rdend and pmaxsample)
	 * <li>Labeling data (based on annotation) 
	 * </ol>
	 * @param colSelector 
	 * 
	 * 
	 * @return 
	 * @throws ModelApplicationException 
	 */
	private SwallowDS preprocessSwallow(DataInterpretation folder, int annotation, int pmax,  boolean skipLeading,
			boolean skipBetween)
					throws ModelApplicationException {
		SwallowDS ret = new SwallowDS();

		Matrix data = concatenate(folder, annotation, normalize, pmax);
		int numRows = data.getNumRows();

		int idxMaxSample2 = pmax;

		int rdEndSample = folder.getRdEndSample();
		int rdStartSample = folder.getRdStartSample();

		//get instance Weight Matrix, set all Weights to 1

		Matrix instanceWeights = new DefaultMatrix(numRows, 2);
		Vectors.set(Matrices.col(instanceWeights, 1), 1); // 

		ArrayList<Integer> throwAway = new ArrayList<>();

		DefaultMatrix labels = new DefaultMatrix(data.getNumRows(), /*SampleId, label*/2);
		// default clazz is -9999 (indicator for missing)
		Vectors.set(Matrices.col(labels, 1), -9999);

		//TODO: Add regression values as distance from annotation, depending on whether one wants classification or regression

		for (int j = 0; j< numRows; j++) {
			// copy over sample indizes to label matrix and to instanceWeight Matrix
			float absSampleIdx = data.get(j, COL_ABS_SAMPLE_IDX);
			labels.set(j, 0, (int) absSampleIdx); 
			instanceWeights.set(j, 0, (int) absSampleIdx);

			// now, determine label  TODO: Add regression values as distance from annotation, depending on whether one wants classification or regression
			float currentDataSampleId = absSampleIdx;

			// determine a weight for instance j

			if (Math.abs(currentDataSampleId - annotation) < weightLength) {
				float distance = Math.abs(currentDataSampleId - annotation);

				instanceWeights.set( j, 1,  1/(weightLength - distance));
			}

			if (currentDataSampleId <= rdStartSample) {
				if (skipLeading) {
					throwAway.add(Integer.valueOf(j));
				} else {
					labels.set(j, 1, LABEL_NICHT_SCHLUCK);
				}
				continue;
			}
			
			if (currentDataSampleId>rdStartSample && currentDataSampleId < rdEndSample) {
				labels.set(j, 1, LABEL_NICHT_SCHLUCK);
				continue;
			}
			if (currentDataSampleId>=rdEndSample && currentDataSampleId <= idxMaxSample2) {
				if (skipBetween) {
					throwAway.add(Integer.valueOf(j));
				} else {
					labels.set(j, 1, LABEL_NICHT_SCHLUCK);
				}
				continue;
			}
			if (currentDataSampleId>idxMaxSample2 && currentDataSampleId < annotation) {
				labels.set(j, 1, LABEL_SCHLUCK);
			}
			if (currentDataSampleId >= annotation) {
				labels.set(j, 1, LABEL_NICHT_SCHLUCK);
			}
		}

		DefaultBitVector throwVector = new DefaultBitVector(numRows);
		Vectors.set(throwVector, true);
		for (Integer j : throwAway) {
			throwVector.set(j.intValue(), false);
		}


		RowSubsetMatrixView dataFiltered = new RowSubsetMatrixView(data, throwVector, true);
		RowSubsetMatrixView labelsFiltered = new RowSubsetMatrixView(labels, throwVector, true);
		RowSubsetMatrixView instanceWeightsFiltered = new RowSubsetMatrixView(instanceWeights, throwVector, true);

		ret.data=new DefaultMatrix(dataFiltered);
		ret.labels=new DefaultMatrix(labelsFiltered);
		ret.instanceWeights = new DefaultMatrix(instanceWeightsFiltered);
		ret.throwAway = throwAway;

		return ret;
	}

	/**
	 * Reads a (test) swallow; preprocessing the data, s.t.:
	 * 
	 * <ol>
	 * <li>Data is aggregated (e.g., concatenating FFT and Pressure samples, adding metadata, see {@link #concatenate(DataInterpretation)})
	 * <li>Data is narrowed (e.g., removing non-informative samples (between rdend and pmaxsample)
	 * <li>RuheDruck is ectracted
	 * <li>Labeling data (based on annotation) 
	 * </ol>
	 * @param colSelector 
	 * 
	 * 
	 * @return 
	 * @throws ModelApplicationException 
	 */
	private SwallowDS preprocessTestSwallow(DataInterpretation folder, int annotation, int pmax, boolean skipLeading, boolean skipBetween) throws ModelApplicationException {
		SwallowDS ret = new SwallowDS();

		Matrix data = concatenate(folder, annotation, normalize, pmax);
		int numRows = data.getNumRows();

		int idxMaxSample2 = pmax;

		int rdEndSample = folder.getRdEndSample();
		int rdStartSample = folder.getRdStartSample();


		ArrayList<Integer> throwAway = new ArrayList<>();
		ArrayList<Integer> ruheDruckSamples = new ArrayList<>();

		DefaultMatrix labels = new DefaultMatrix(data.getNumRows(), /*SampleId, label*/2);
		// default clazz is -9999 (indicator for missing)
		Vectors.set(Matrices.col(labels, 1), -9999);

		//TODO: Add regression values as distance from annotation, depending on whether one wants classification or regression

		if (Double.isNaN(annotation)) {
			for (int j = 0; j< numRows; j++) {
				// copy over sample indizes to label matrix 
				labels.set(j, 0, (int) data.get(j, COL_ABS_SAMPLE_IDX)); 

				// now, determine label  
				float currentDataSampleId = data.get(j, COL_ABS_SAMPLE_IDX);

				if (currentDataSampleId <= rdStartSample) {
					if (skipLeading) {
						throwAway.add(Integer.valueOf(j));
					} else {
						labels.set(j, 1, -9999);
					}
					continue;

				}
				if (currentDataSampleId>rdStartSample && currentDataSampleId < rdEndSample) {
					ruheDruckSamples.add(Integer.valueOf(j));
					labels.set(j, 1, -9999);
					continue;
				}
				if (currentDataSampleId>=rdEndSample && currentDataSampleId <= idxMaxSample2) {
					if (skipBetween) {
						throwAway.add(Integer.valueOf(j));
					} else {
						labels.set(j, 1, -9999);
					}
					continue;
				}
			}
		}
		else {
			for (int j = 0; j< numRows; j++) {
				// copy over sample indizes to label matrix 
				labels.set(j, 0, (int) data.get(j, COL_ABS_SAMPLE_IDX)); 

				// now, determine label  TODO: Add regression values as distance from annotation, depending on whether one wants classification or regression
				float currentDataSampleId = data.get(j, COL_ABS_SAMPLE_IDX);

				if (currentDataSampleId <= rdStartSample) {
					if (skipLeading) {
						throwAway.add(Integer.valueOf(j));
					} else {
						labels.set(j, 1, LABEL_NICHT_SCHLUCK);
					}
					continue;

				}
				if (currentDataSampleId>rdStartSample && currentDataSampleId < rdEndSample) {
					ruheDruckSamples.add(Integer.valueOf(j));
					labels.set(j, 1, LABEL_NICHT_SCHLUCK);
					continue;
				}
				if (currentDataSampleId>=rdEndSample && currentDataSampleId <= idxMaxSample2) {
					if (skipBetween) {
						throwAway.add(Integer.valueOf(j));
					} else {
						labels.set(j, 1, LABEL_NICHT_SCHLUCK);
					}
					continue;
				}
				if (currentDataSampleId>idxMaxSample2 && currentDataSampleId < annotation) {
					labels.set(j, 1, LABEL_SCHLUCK);
				}
				if (currentDataSampleId >= annotation) {
					labels.set(j, 1, LABEL_NICHT_SCHLUCK);
				}
			}

		}

		DefaultBitVector ruheDruckVector = new DefaultBitVector(numRows);
		Vectors.set(ruheDruckVector, false);
		for (Integer j : ruheDruckSamples) {
			ruheDruckVector.set(j.intValue(), true);
		}


		DefaultBitVector throwVector = new DefaultBitVector(numRows);
		Vectors.set(throwVector, true);
		for (Integer j : throwAway) {
			throwVector.set(j.intValue(), false);
		}

		RowSubsetMatrixView ruheDruck = new RowSubsetMatrixView(data, ruheDruckVector, true);
		RowSubsetMatrixView ruheDruckLabels = new RowSubsetMatrixView(labels, ruheDruckVector, true);

		// Hier alle labels des Ruhedrucks auf -1 setzen, da kein Schluck!!!! In neue Matrix giessen weil es sonst nicht geht!!

		Matrix ruheDruckLabels2 = new DefaultMatrix(ruheDruckLabels);

		for (int row = 0; row < ruheDruckLabels.getNumRows() ; row++) {
			ruheDruckLabels2.set(row, COL_LABEL_IN_LABELS, LABEL_NICHT_SCHLUCK);
		}

		RowSubsetMatrixView dataFiltered = new RowSubsetMatrixView(data, throwVector, true);
		RowSubsetMatrixView labelsFiltered = new RowSubsetMatrixView(labels, throwVector, true);

		//		System.out.println(ruheDruck.getNumRows() + " ist gleich " +  ruheDruckLabels2.getNumRows());

		ret.data=dataFiltered;
		ret.labels=labelsFiltered;
		ret.ruheDruck = ruheDruck;
		ret.throwAway = throwAway;
		ret.ruheDruckLabels = ruheDruckLabels2;

		return ret;
	}

	
	/**
	 * Concatenates the Data of a given Read Folder Object, normalizes the data and includes meta data!
	 * @param folder
	 * @param restitutionszeitSample if <0, ignore the annotation and return a dummy (const) column
	 * @param normalize 
	 * @param pmaxSample if < 0 wont be used but will be computed, otherwise it will be used!
	 * @return Matrix of normalized pressure, fft and meta
	 * @throws ModelApplicationException 
	 */
	private Matrix concatenate(DataInterpretation folder, 
			int restitutionszeitSample,
			boolean normalize, int pmaxSample) throws ModelApplicationException {

		int sampleIdxOfpMaxSample;

		Matrix druck = folder.getDruck();
		Matrix fft = folder.getFft();
		Matrix normalizedMaximumPressure = null;
		String channelstart = folder.getChannelstart();
		if (channelstart.startsWith("P"))
			channelstart=channelstart.substring(1);
		int start = Integer.parseInt(channelstart);
		String channelend = folder.getChannelend();
		if (channelend.startsWith("P"))
			channelend = channelend.substring(1);
		int end = Integer.parseInt(channelend);

		IntRange eSleeve = IntRange.convert(start + "," + end);

		Matrix sleeveDruck = new ColumnSubsetMatrixView(druck, eSleeve.getUsedIndexes());

		float[] maximumPressure = AlgorithmController.getTheMaxCurve(sleeveDruck);

		Vector maximumPressureVector = Vectors.floatArraytoVector(maximumPressure);

		if (pmaxSample < 1) {
			sampleIdxOfpMaxSample = (int) druck.get(AlgorithmController.getMax(sleeveDruck), 0);
		}
		else {
			sampleIdxOfpMaxSample = pmaxSample;
		}

		int firstSample = folder.getFirstSample();
		int lastSample = folder.getLastSample();

		if (sampleIdxOfpMaxSample < firstSample || sampleIdxOfpMaxSample > lastSample) {
			throw new ModelApplicationException("Der angegebene pmax liegt nicht im Schluck. Angegeben: " 
					+ pmaxSample + "\n Erstes Sample im schluck: " + firstSample + " , Letztes Sample im Schluck: " + lastSample 
					+ ", aufgetreten bei dem Schluck " + folder.getSchluckverzeichnis()
					, pmaxSample, lastSample, firstSample);
		}

		//Extract additional Features, Matrix sphincterFeatures is then attached to the final data Matrix
		Vector lowerFeatures = lowerFeatureExtractor.extractFeatures(druck, start, end);
		Vector middleFeatures = middleFeatureExtractor.extractFeatures(druck, start, end);
		Vector upperFeatures = upperFeatureExtractor.extractFeatures(druck, start, end);
		Vector lowerMiddleFeatures = lowerMiddleFeatureExtractor.extractFeatures(druck, start, end);
		Vector upperMiddleFeatures = upperMiddleFeatureExtractor.extractFeatures(druck, start, end);
		Vector allFeatures = extractor.extractFeatures(druck, start, end);

		Matrix sphincterFeatures = new DefaultMatrix(new ColumnUnionMatrixView(new Vector[] {
				lowerFeatures,
				middleFeatures,
				upperFeatures,
				lowerMiddleFeatures,
				upperMiddleFeatures,
				allFeatures }));

		Matrix normalizedDruck = null;
		Matrix normalizedFFT = null;
		Matrix normalizedSphincterFeatures = null;

		Vector sampleIdx = Matrices.col(druck, 0);

		if (normalize) {
			normalizedDruck = AlgorithmController.normalize(ColumnSubsetMatrixView.create(druck, new DefaultIntVector(new int[] {0})));
			normalizedFFT = AlgorithmController.normalize(ColumnSubsetMatrixView.create(fft, new DefaultIntVector(new int[] {0})));
			normalizedMaximumPressure = AlgorithmController.normalize(new VectorAsMatrixView(maximumPressureVector));
			normalizedSphincterFeatures = AlgorithmController.normalize(sphincterFeatures);
			log.info("The swallow: " + folder.getSwallowId() + " by Proband " + folder.getProband() + " is normalized");
		}

		int numRows = druck.getNumRows();

		log.info("Using " + normalizedDruck.getNumColumns() + " pressure features, " + normalizedMaximumPressure.getNumColumns()
				+ " maximum Pressure feature, \n "
				+ normalizedFFT.getNumColumns() + " fft features and " + normalizedSphincterFeatures.getNumColumns() + " sphincter features");

		//				log.info("For choosing the right feature subsets:");
		//				log.info("");
		//				log.info("Pressure features start at 5!");
		//				log.info("Pmax is at feature 32!");
		//				log.info("FFT starts at 33!");
		//				log.info("Sphincter Features start at 161! ");

		Matrix dataBeforeTimeExtraction;

		dataBeforeTimeExtraction = new ColumnUnionMatrixView(new Matrix[] {
				normalizedDruck
				, normalizedMaximumPressure
				, normalizedFFT
				, normalizedSphincterFeatures
		});

		

		TimeFeatureExtractor timeFeatureExtractor = new TimeFeatureExtractor();

		Matrix timeFeatures = timeFeatureExtractor.extractFeatures(dataBeforeTimeExtraction, getTimeOrder());

		log.info("Using approximates of the first derivatives of order: "+ getTimeOrder());
		log.info("Using " + timeFeatures.getNumColumns() + " additional temporal features!");
		
		
		AcidFeatureExtractor acidFeatureExtractor = new AcidFeatureExtractor();
		
		Matrix acidFeatures = acidFeatureExtractor.extractFeatures(folder);
		
		log.info("Using " + acidFeatures.getNumColumns() + " additional categorical features for acid");
		
		PatientFeatureExtractor patientFeatureExtractor = new PatientFeatureExtractor();
		
		Matrix patientFeatures = patientFeatureExtractor.extractFeatures(folder);
		
		log.info("Using " + patientFeatures.getNumColumns() + " additional categorical patient features");

		DefaultMatrix swallowId = new DefaultMatrix(numRows, 1);
		DefaultMatrix annotationSampleMatrix = new DefaultMatrix(numRows, 1);
		DefaultMatrix pMaxSampleMatrix = new DefaultMatrix(numRows, 1);

		// set swallow as meta 0 column idx
		Vectors.set(Matrices.col(swallowId, COL_SWALLOW_IDX), folder.getSwallowId());

		// the absolute sample id:
		Vector absSampleId = sampleIdx;

		// compute relative samples:
		DefaultVector relativeSampleId = new DefaultVector(absSampleId);
		Vectors.add(relativeSampleId, -1*absSampleId.get(0));

		// the static annotation sample
		if (restitutionszeitSample < 0) {
			Vectors.set(Matrices.col(annotationSampleMatrix, 0), 0); // defaults to 0
		} else {
			Vectors.set(Matrices.col(annotationSampleMatrix, 0), restitutionszeitSample);
		}
		// the static calculated pMax Sample
		Vectors.set(Matrices.col(pMaxSampleMatrix, 0), sampleIdxOfpMaxSample);

		//Concatenate Matrices without sample indexes for fft and druck
		ColumnUnionMatrixView ret1 = new ColumnUnionMatrixView(new Matrix[] {
				swallowId
				, new VectorAsMatrixView(absSampleId)
				, new VectorAsMatrixView(relativeSampleId)
				, annotationSampleMatrix
				, pMaxSampleMatrix
				, normalizedDruck
				, normalizedMaximumPressure
				, normalizedFFT
				, normalizedSphincterFeatures
				, timeFeatures
				, acidFeatures
				, patientFeatures
		}); 

		Matrix ret = new DefaultMatrix(ret1);

		if (isSerializeTheData()) {
			
			File dataInterpretation = folder.getDataInterpretation();
			File output = new File (dataInterpretation, "preprocessed_" + annotator + ".csv");
		
			try {
				Matrices.write(ret, output);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return ret;


	}

	
	// Getters and Setters
	public ReadSplit getReadSplit() {
		return readSplit;
	}

	public void setReadSplit(ReadSplit readSplit) {
		this.readSplit = readSplit;
	}

	public IntRange getColumnSelector() {
		return columnSelector;
	}

	public void setColumnSelector(IntRange columnSelector) {
		this.columnSelector = columnSelector;
	}

	public int getWindowExtent() {
		return windowExtent;
	}

	public void setWindowExtent(int windowExtent) {
		this.windowExtent = windowExtent;
	}
	
	public Vector getFinalParameters() {
		return finalParameters;
	}

	public void setFinalParameters(Vector finalParameters) {
		this.finalParameters = finalParameters;
	}

	public String getAnnotator() {
		return annotator;
	}

	public void setAnnotator(String annotator) {
		this.annotator = annotator;
	}

	public String getAnnotationBaseDir() {
		return annotationBaseDir;
	}

	public void setAnnotationBaseDir(String annotationBaseDir) {
		this.annotationBaseDir = annotationBaseDir;
	}

	public boolean isIncludeRD() {
		return includeRD;
	}

	public void setIncludeRD(boolean includeRD) {
		this.includeRD = includeRD;
	}

	public String getApplyFolder() {
		return applyFolder;
	}

	public void setApplyFolder(String applyFolder) {
		this.applyFolder = applyFolder;
	}

	public float[] getFinalParametersArray() {
		return finalParametersArray;
	}

	public void setFinalParametersArray(float[] finalParametersArray) {
		this.finalParametersArray = finalParametersArray;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public boolean isNormalize() {
		return normalize;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	public float getWeightLength() {
		return weightLength;
	}

	public void setWeightLength(float weightLength) {
		this.weightLength = weightLength;
	}

	public int getSplitNumber() {
		return splitNumber;
	}

	public void setSplitNumber(int splitNumber) {
		this.splitNumber = splitNumber;
	}

	public int getProbandNumber() {
		return probandNumber;
	}

	public void setProbandNumber(int probandNumber) {
		this.probandNumber = probandNumber;
	}
	
	public int getNrAttributes() {
		return nrAttributes;
	}

	public void setNrAttributes(int nrAttributes) {
		this.nrAttributes = nrAttributes;
	}

	public boolean isExtractAcidFeatures() {
		return isUseAcidFeatures();
	}

	public void setExtractAcidFeatures(boolean extractAcidFeatures) {
		this.setUseAcidFeatures(extractAcidFeatures);
	}


	public boolean isExtractPatientFeatures() {
		return isUsePatientFeatures();
	}

	public void setExtractPatientFeatures(boolean extractPatientFeatures) {
		this.setUsePatientFeatures(extractPatientFeatures);
	}

	public int getTimeOrder() {
		return timeOrder;
	}

	public void setTimeOrder(int timeOrder) {
		this.timeOrder = timeOrder;
	}

	public boolean isUsePatientFeatures() {
		return usePatientFeatures;
	}

	public void setUsePatientFeatures(boolean usePatientFeatures) {
		this.usePatientFeatures = usePatientFeatures;
	}

	public boolean isUseAcidFeatures() {
		return useAcidFeatures;
	}

	public void setUseAcidFeatures(boolean useAcidFeatures) {
		this.useAcidFeatures = useAcidFeatures;
	}

	public boolean isSerializeTheData() {
		return serializeTheData;
	}

	public void setSerializeTheData(boolean serializeTheData) {
		this.serializeTheData = serializeTheData;
	}
}
