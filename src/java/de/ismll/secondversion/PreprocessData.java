package de.ismll.secondversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.bootstrap.Parameter;
import de.ismll.mhh.featureExtractors.AcidFeatureExtractor;
import de.ismll.mhh.featureExtractors.AllExtractor;
import de.ismll.mhh.featureExtractors.LowerExtractor;
import de.ismll.mhh.featureExtractors.LowerMiddleExtractor;
import de.ismll.mhh.featureExtractors.MiddleExtractor;
import de.ismll.mhh.featureExtractors.PatientFeatureExtractor;
import de.ismll.mhh.featureExtractors.TimeFeatureExtractor;
import de.ismll.mhh.featureExtractors.UpperExtractor;
import de.ismll.mhh.featureExtractors.UpperMiddleExtractor;
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
 */
public class PreprocessData  implements Runnable{

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

	@Parameter(cmdline="timeOrder")
	private int timeOrder = 3;

	@Parameter(cmdline="columnSelector", description="Optional. If given, selects a subset of columns. WARNING: If given, make sure to exclude, e.g., the sample Index (col 0)!")
	private IntRange columnSelector;

	@Parameter(cmdline="annotationBaseDir")
	private String annotationBaseDir;

	protected Logger log = LogManager.getLogger(getClass());

	private boolean skipLeading=true;
	private boolean skipBetween=true;

	@Parameter(cmdline="serializeTheData")
	private File serializeTheData;

	private final AllExtractor extractor;

	private final LowerExtractor lowerFeatureExtractor;

	private final MiddleExtractor middleFeatureExtractor;

	private final UpperExtractor upperFeatureExtractor;

	private final LowerMiddleExtractor lowerMiddleFeatureExtractor;

	private final UpperMiddleExtractor upperMiddleFeatureExtractor;


	private final TimeFeatureExtractor timeFeatureExtractor;

	private final AcidFeatureExtractor acidFeatureExtractor;

	private final PatientFeatureExtractor patientFeatureExtractor;

	private final List<SwallowDS> trainingSwallows;
	private final List<SwallowDS> validationSwallows;
	private final List<SwallowDS> testSwallows;

	public PreprocessData() {
		extractor = new AllExtractor();
		lowerFeatureExtractor = new LowerExtractor();
		middleFeatureExtractor = new MiddleExtractor();
		upperFeatureExtractor = new UpperExtractor();
		lowerMiddleFeatureExtractor = new LowerMiddleExtractor();
		upperMiddleFeatureExtractor = new UpperMiddleExtractor();
		
		timeFeatureExtractor = new TimeFeatureExtractor();
		acidFeatureExtractor = new AcidFeatureExtractor();
		patientFeatureExtractor = new PatientFeatureExtractor();
		
		trainingSwallows = new ArrayList<>();
		validationSwallows = new ArrayList<>();
		testSwallows = new ArrayList<>();
	}

	
	@Override
	public void run() {
		
		log.info("Working on " + readSplit);

		readSplit.run();

		if (null != columnSelector) {
			log.info("Using the following column selector: " + columnSelector);
		}

		log.info("Num training folders   : " + readSplit.trainFolders.length);
		log.info("Num test folders       : " + readSplit.testFolders.length);		 
		log.info("Num validation folders : " + readSplit.validationFolders.length);

		// TRAIN!

		for(int i = 0; i < readSplit.trainFolders.length; i ++) {
			log.info("Training data ... " + readSplit.trainFolders[i].getSwallowId());
			DataInterpretation folder = readSplit.trainFolders[i];

			SwallowDS d = null;
			try {
				d = preprocessTestSwallow(folder);
				trainingSwallows.add(d);
			} catch (DataValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (null != serializeTheData) {
				try {
					d.serialize(serializeTheData, 1, 
							String.format("p-%1$s-s-%1$s", folder.getProband(), folder.getSwallowId()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// VALIDATION

		for(int i = 0; i < readSplit.validationFolders.length; i ++) {
			log.info("Validation data ... " + readSplit.validationFolders[i].getSwallowId());
			DataInterpretation folder = readSplit.validationFolders[i];

			SwallowDS d = null;
			try {
				d = preprocessTestSwallow(folder);
				validationSwallows.add(d);
			} catch (DataValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		// TEST

		for(int i = 0; i < readSplit.testFolders.length; i ++) {
			log.info("Test data ... " + readSplit.testFolders[i].getSwallowId());
			DataInterpretation folder = readSplit.testFolders[i];


			SwallowDS d = null;
			try {
				d = preprocessTestSwallow(folder);
				testSwallows.add(d);
			} catch (DataValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
	 * @return 
	 * @throws DataValidationException 
	 */
	private SwallowDS preprocessTestSwallow(DataInterpretation folder) throws DataValidationException {
		SwallowDS ret = new SwallowDS();

		int annotation = folder.getAnnotatedRestitutionTimeSample(annotationBaseDir, annotator);
		int idxAnnotatedPMaxSample = folder.getAnnotatedPmaxSample(annotationBaseDir, annotator);
		Matrix data = concatenate(folder);
		int numRows = data.getNumRows();

		boolean annotate = !Double.isNaN(annotation);

		int rdEndSample = folder.getRdEndSample();
		int rdStartSample = folder.getRdStartSample();

		Matrix instanceWeights = new DefaultMatrix(numRows, 2);
		Vectors.set(Matrices.col(instanceWeights, 1), 1); 

		ArrayList<Integer> throwAway = new ArrayList<>();

		DefaultMatrix labels = new DefaultMatrix(data.getNumRows(), /*SampleId, label*/2);
		// default clazz is -9999 (indicator for missing)
		Vectors.set(Matrices.col(labels, 1), -9999);


		for (int j = 0; j < numRows; j++) {
			float currentDataSampleId = data.get(j, COL_ABS_SAMPLE_IDX);

			// copy over sample indizes to label matrix
			labels.set(j, 0, (int) currentDataSampleId);
			instanceWeights.set(j, 0, (int) currentDataSampleId);

			if (annotate) {
				float distance = Math.abs(currentDataSampleId - annotation);
				
				if (distance < weightLength) {
					instanceWeights.set( j, 1,  1/(weightLength - distance));
				}
			}
			
			if (currentDataSampleId <= rdStartSample) {
				if (skipLeading) {
					throwAway.add(Integer.valueOf(j));
				} else {
					if (annotate)
						labels.set(j, 1, LABEL_NICHT_SCHLUCK);
				}
				continue;

			}
			if (currentDataSampleId > rdStartSample && currentDataSampleId < rdEndSample) {
				if (annotate)
					labels.set(j, 1, LABEL_NICHT_SCHLUCK);
				continue;
			}
			if (currentDataSampleId >= rdEndSample && currentDataSampleId <= idxAnnotatedPMaxSample) {
				if (skipBetween) {
					throwAway.add(Integer.valueOf(j));
				} else {
					if (annotate)
						labels.set(j, 1, LABEL_NICHT_SCHLUCK);
				}
				continue;
			}
			if (currentDataSampleId > idxAnnotatedPMaxSample && currentDataSampleId < annotation) {
				if (annotate)
					labels.set(j, 1, LABEL_SCHLUCK);
			}
			if (currentDataSampleId >= annotation) {
				if (annotate)
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

		ret.data=dataFiltered;
		ret.labels=labelsFiltered;
		ret.instanceWeights=instanceWeights;
		ret.throwAway = throwAway;

		return ret;
	}

	
	/**
	 * Concatenates the Data of a given Read Folder Object, normalizes the data and includes meta data!
	 * @param folder
	 * @param restitutionszeitSample if <0, ignore the annotation and return a dummy (const) column
	 * @param normalize 
	 * @param pmaxSample if < 0 wont be used but will be computed, otherwise it will be used!
	 * @return Matrix of normalized pressure, fft and meta
	 * @throws DataValidationException 
	 */
	protected Matrix concatenate(DataInterpretation folder) throws DataValidationException  {

		int annotatedRestitutionTimeSample = folder.getAnnotatedRestitutionTimeSample(annotationBaseDir, annotator);
		int annotatedPmaxSampleAbsoluteIdx = folder.getAnnotatedPmaxSample(annotationBaseDir, annotator);

		int pMaxAbsoluteSampleIdx;

		Matrix druck = folder.getDruck();
		Matrix fft = folder.getFft();
		
		int startChannel = folder.getChannelstartAsInt();
		int endChannel = folder.getChannelendAsInt();

		int firstAbsoluteSampleIdx = folder.getFirstSample();
		int lastAbsoluteSampleIdx = folder.getLastSample();

		IntRange eSleeve = IntRange.convert(startChannel + "," + endChannel);

		Matrix sleeveDruck = new ColumnSubsetMatrixView(druck, eSleeve.getUsedIndexes());

		float[] maximumPressure = AlgorithmController.getTheMaxCurve(sleeveDruck);

		Vector maximumPressureVector = Vectors.floatArraytoVector(maximumPressure);

		if (annotatedPmaxSampleAbsoluteIdx < 1) {
			int maxSleeveDruckIdx = AlgorithmController.argmax(sleeveDruck);
			pMaxAbsoluteSampleIdx = (int) druck.get(maxSleeveDruckIdx, 0);
		}
		else {
			pMaxAbsoluteSampleIdx = annotatedPmaxSampleAbsoluteIdx;
		}

		if (pMaxAbsoluteSampleIdx < firstAbsoluteSampleIdx || pMaxAbsoluteSampleIdx > lastAbsoluteSampleIdx) {
			throw new DataValidationException("Der angegebene pmax liegt nicht im Schluck. Angegeben: " 
					+ annotatedPmaxSampleAbsoluteIdx + "\n Erstes Sample im schluck: " + firstAbsoluteSampleIdx + " , Letztes Sample im Schluck: " + lastAbsoluteSampleIdx 
					+ ", aufgetreten bei dem Schluck " + folder.getSchluckverzeichnis()
					, annotatedPmaxSampleAbsoluteIdx, lastAbsoluteSampleIdx, firstAbsoluteSampleIdx);
		}

		//Extract additional Features, Matrix sphincterFeatures is then attached to the final data Matrix
		Vector lowerFeatures = lowerFeatureExtractor.extractFeatures(druck, startChannel, endChannel);
		Vector middleFeatures = middleFeatureExtractor.extractFeatures(druck, startChannel, endChannel);
		Vector upperFeatures = upperFeatureExtractor.extractFeatures(druck, startChannel, endChannel);
		Vector lowerMiddleFeatures = lowerMiddleFeatureExtractor.extractFeatures(druck, startChannel, endChannel);
		Vector upperMiddleFeatures = upperMiddleFeatureExtractor.extractFeatures(druck, startChannel, endChannel);
		Vector allFeatures = extractor.extractFeatures(druck, startChannel, endChannel);

		Matrix sphincterFeatures = new DefaultMatrix(new ColumnUnionMatrixView(new Vector[] {
				lowerFeatures,
				middleFeatures,
				upperFeatures,
				lowerMiddleFeatures,
				upperMiddleFeatures,
				allFeatures }));

		Vector sampleIdx = Matrices.col(druck, 0);

		Matrix normalizedDruck = AlgorithmController.normalize(ColumnSubsetMatrixView.create(druck, new DefaultIntVector(new int[] {0})));
		Matrix normalizedFFT = AlgorithmController.normalize(ColumnSubsetMatrixView.create(fft, new DefaultIntVector(new int[] {0})));
		Matrix normalizedMaximumPressure = AlgorithmController.normalize(new VectorAsMatrixView(maximumPressureVector));
		Matrix normalizedSphincterFeatures = AlgorithmController.normalize(sphincterFeatures);

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

		Matrix dataBeforeTimeExtraction = new ColumnUnionMatrixView(new Matrix[] {
				normalizedDruck
				, normalizedMaximumPressure
				, normalizedFFT
				, normalizedSphincterFeatures
		});

		Matrix timeFeatures = timeFeatureExtractor.extractFeatures(dataBeforeTimeExtraction, getTimeOrder());

		log.info("Using approximates of the first derivatives of order: "+ getTimeOrder());
		log.info("Using " + timeFeatures.getNumColumns() + " additional temporal features!");
		
		Matrix acidFeatures = acidFeatureExtractor.extractFeatures(folder);
		
		log.info("Using " + acidFeatures.getNumColumns() + " additional categorical features for acid");
		
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
		if (annotatedRestitutionTimeSample < 0) {
			Vectors.set(Matrices.col(annotationSampleMatrix, 0), 0); // defaults to 0
		} else {
			Vectors.set(Matrices.col(annotationSampleMatrix, 0), annotatedRestitutionTimeSample);
		}
		// the static calculated pMax Sample
		Vectors.set(Matrices.col(pMaxSampleMatrix, 0), pMaxAbsoluteSampleIdx);

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

		return  new DefaultMatrix(ret1);
	}

	public List<SwallowDS> getTrainingSwallowIterator() {
		return Collections.unmodifiableList(trainingSwallows);
	}
	
	public List<SwallowDS> getValidationSwallowIterator() {
		return Collections.unmodifiableList(validationSwallows);
	}
	
	public List<SwallowDS> getTestSwallowIterator() {
		return Collections.unmodifiableList(testSwallows);
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

	public float getWeightLength() {
		return weightLength;
	}

	public void setWeightLength(float weightLength) {
		this.weightLength = weightLength;
	}

	public int getTimeOrder() {
		return timeOrder;
	}

	public void setTimeOrder(int timeOrder) {
		this.timeOrder = timeOrder;
	}

	public File getSerializeTheData() {
		return serializeTheData;
	}

	public void setSerializeTheData(File serializeTheData) {
		this.serializeTheData = serializeTheData;
	}

}
