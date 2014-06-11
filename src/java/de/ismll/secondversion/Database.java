package de.ismll.secondversion;

import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.bootstrap.Parameter;
import de.ismll.database.dao.DataStoreException;
import de.ismll.database.dao.Datatypes;
import de.ismll.database.dao.Entity;
import de.ismll.database.dao.PgStore;
import de.ismll.database.dao.Table;
import de.ismll.database.pgsql.PostgresSQL;
import de.ismll.table.Vectors;

public class Database {


	private Logger log = LogManager.getLogger(getClass());

	@Parameter(cmdline="iterationTable")
	private String iterationTable="iterations";

	@Parameter(cmdline="runTable")
	private String runTable="run";

	@Parameter(cmdline="runTable")
	private String runLapTable="runlap";

	@Parameter(cmdline="experimentTable")
	private String experimentTable="experiment";

	@Parameter(cmdline="validationTable")
	private String validationTable="evaluation";

	@Parameter(cmdline="predictionTable")
	private String predictionTable="prediction";

	private PgStore pgStore;

	private IterationTable iter;

	private RunTable run;

	private RunLaplacian runlap;

	private ExperimentTable experiment;

	private ValidationTable evaluation;

	private PredictionTable prediction;

	public Database() throws IOException, DataStoreException {
		this(new PostgresSQL());
	}

	public Database(PostgresSQL pgsql) throws DataStoreException {
		super();

		pgStore = new PgStore(pgsql);


	}

	public void init() throws DataStoreException {
		iter = new IterationTable(iterationTable);
		pgStore.ensureTableExists(iter);

		run = new RunTable(runTable);
		pgStore.ensureTableExists(run);

		setRunlap(new RunLaplacian(getRunLapTable()));
		pgStore.ensureTableExists(getRunlap());

		experiment = new ExperimentTable(getExperimentTable());
		pgStore.ensureTableExists(experiment);

		evaluation = new ValidationTable(getValidationTable());
		pgStore.ensureTableExists(evaluation);

		prediction = new PredictionTable(getPredictionTable());
		pgStore.ensureTableExists(prediction);

		log.info("Database Tables have been created!");
	}

	public long addPrediction(int proband, int swallow, int pmax, String pmaxTime,
			int annotation, int predictedAnnotation, int sampleDifference,
			String annotationTime,
			String predictedAnnotationTime, String timeDifference, int restitutionTimeInSamples, String restitutionTime,
			int predictedRestitutionTimeInSamples, String predictedRestitutionTime) throws DataStoreException {

		Entity predictionInstance = prediction.createInstance();

		predictionInstance.set(prediction.proband , proband);
		predictionInstance.set(prediction.swallow, swallow);
		predictionInstance.set(prediction.pmax, pmax);
		predictionInstance.set(prediction.pmaxTime, pmaxTime);
		predictionInstance.set(prediction.annotation, annotation);
		predictionInstance.set(prediction.predictedAnnotation, predictedAnnotation);
		predictionInstance.set(prediction.sampleDifference, sampleDifference);
		predictionInstance.set(prediction.annotationTime, annotationTime);
		predictionInstance.set(prediction.predictedAnnotationTime, predictedAnnotationTime);
		predictionInstance.set(prediction.timeDifference, timeDifference);
		predictionInstance.set(prediction.restitutionTimeInSamples, restitutionTimeInSamples);
		predictionInstance.set(prediction.restitutionTime, restitutionTime);
		predictionInstance.set(prediction.predictedRestitutionTimeInSamples, predictedRestitutionTimeInSamples);
		predictionInstance.set(prediction.predictedRestitutionTime, predictedRestitutionTime);

		return pgStore.insertOrUpdate(predictionInstance);
	}


	public long addExperiment(String experiment_name, int proband, int split, String model_parameters, double accuracy,
			double sample_difference,
			double step_size, double lambda, double smooth_reg, double smooth_window, int iteration_nr, int window_extent) throws DataStoreException {
		Entity experimentInstance = experiment.createInstance();
		experimentInstance.set(experiment.experimentName, experiment_name);
		experimentInstance.set(experiment.proband, proband);
		experimentInstance.set(experiment.split, split);
		experimentInstance.set(experiment.modelParameters, model_parameters);
		experimentInstance.set(experiment.accuracy, accuracy);
		experimentInstance.set(experiment.sampleDifference, sample_difference);
		experimentInstance.set(experiment.stepSize, step_size);
		experimentInstance.set(experiment.lambda, lambda);
		experimentInstance.set(experiment.smoothReg, smooth_reg);
		experimentInstance.set(experiment.smoothWindow, smooth_window);
		experimentInstance.set(experiment.iterationNr, iteration_nr);
		experimentInstance.set(experiment.windowExtent, window_extent);

		return pgStore.insertOrUpdate(experimentInstance);
	}

	public long addEvaluation(String experiment_name, int proband, int split, int window_extent, float validationAccuracy, float validationSampleDiff ,
			float testAccuracy, float testSampleDiff, int predictedAnnotation, int trueAnnotation) throws DataStoreException {
		Entity validationInstance = evaluation.createInstance();
		validationInstance.set(evaluation.experimentName, experiment_name);
		validationInstance.set(evaluation.proband, proband);
		validationInstance.set(evaluation.split, split);
		validationInstance.set(evaluation.windowExtent, window_extent);
		validationInstance.set(evaluation.validationAccuracy, validationAccuracy);
		validationInstance.set(evaluation.validationSampleDifference, validationSampleDiff);
		validationInstance.set(evaluation.testAccuracy, testAccuracy);
		validationInstance.set(evaluation.testSampleDifference, testSampleDiff);
		validationInstance.set(evaluation.predictedAnnotation, predictedAnnotation);
		validationInstance.set(evaluation.trueAnnotation, trueAnnotation);

		return pgStore.insertOrUpdate(validationInstance);
	}

	public long addIteration(int iterationNr, double accuracy, float sampleDifference, float overshootPercentage,
			float[] parameters, int splitNumber, int probandNumber, long runKey) throws DataStoreException {
		Entity iterInstance = iter.createInstance();
		iterInstance.set(iter.iterationNumber, iterationNr);
		iterInstance.set(iter.accuracy, accuracy);
		iterInstance.set(iter.sampleDifference, sampleDifference);
		iterInstance.set(iter.overshootPercentage, overshootPercentage);
		iterInstance.set(iter.modelParameters, Vectors.toString( Vectors.floatArraytoVector(parameters) ) );
		iterInstance.set(iter.splitNumber, splitNumber);
		iterInstance.set(iter.probandNumber, probandNumber);
		iterInstance.set(iter.fkRunId, runKey);

		return pgStore.insertOrUpdate(iterInstance);
	}

	public long addRun(float lambda, float stepSize, int windowExtent, int batchSize, String splitPath) throws DataStoreException {
		Entity runInstance = run.createInstance();
		runInstance.set(run.lambda, lambda);
		runInstance.set(run.stepSize, stepSize);
		runInstance.set(run.batchSize, batchSize);
		runInstance.set(run.windowExtent, windowExtent);
		runInstance.set(run.splitPath, splitPath);

		return pgStore.insertOrUpdate(runInstance);

	}

	public long addRunLaplacian(float lambda, float stepSize, int windowExtent, int batchSize, String splitPath,
			float smoothReg, int smoothWindow) throws DataStoreException {
		Entity runInstance = getRunlap().createInstance();
		runInstance.set(getRunlap().lambda, lambda);
		runInstance.set(getRunlap().stepSize, stepSize);
		runInstance.set(getRunlap().batchSize, batchSize);
		runInstance.set(getRunlap().windowExtent, windowExtent);
		runInstance.set(getRunlap().splitPath, splitPath);
		runInstance.set(getRunlap().smoothReg, smoothReg);
		runInstance.set(getRunlap().smoothWindow, smoothWindow);

		return pgStore.insertOrUpdate(runInstance);

	}


	public String getIterationTable() {
		return iterationTable;
	}

	public void setIterationTable(String iterationTable) {
		this.iterationTable = iterationTable;
	}

	public String getRunTable() {
		return runTable;
	}

	public void setRunTable(String runTable) {
		this.runTable = runTable;
	}

	public Table getIter() {
		return iter;
	}

	public Table getRun() {
		return run;
	}

	public PgStore getPgStore() {
		return pgStore;
	}

	public void setPgStore(PgStore pgStore) {
		this.pgStore = pgStore;
	}

	public RunLaplacian getRunlap() {
		return runlap;
	}

	public void setRunlap(RunLaplacian runlap) {
		this.runlap = runlap;
	}

	public String getRunLapTable() {
		return runLapTable;
	}

	public void setRunLapTable(String runLapTable) {
		this.runLapTable = runLapTable;
	}

	public String getExperimentTable() {
		return experimentTable;
	}

	public void setExperimentTable(String experimentTable) {
		this.experimentTable = experimentTable;
	}

	public String getValidationTable() {
		return validationTable;
	}

	public void setValidationTable(String validationTable) {
		this.validationTable = validationTable;
	}

	public PredictionTable getPrediction() {
		return prediction;
	}

	public void setPrediction(PredictionTable prediction) {
		this.prediction = prediction;
	}

	public String getPredictionTable() {
		return predictionTable;
	}

	public void setPredictionTable(String predictionTable) {
		this.predictionTable = predictionTable;
	}



}
