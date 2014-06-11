package de.ismll.mhh.analyze;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.database.SqlStatement;
import de.ismll.database.dao.IContentHolder;
import de.ismll.database.dao.DataStoreException;
import de.ismll.database.dao.IEntityBacked;
import de.ismll.database.pgsql.PostgresSQL;
import de.ismll.runtime.files.NoQueryFoundException;
import de.ismll.runtime.files.XmlRepository;
import de.ismll.secondversion.Database;
import de.ismll.table.Matrices;
import de.ismll.table.Matrices.FileType;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;

public class GridSearchEval {
	
	private int proband;
	private String experiment;
	private boolean laplacian;
	private String iterName;
	private String runName;

	private static Logger log = LogManager.getLogger(GridSearchEval.class);

	public void evaluateGrid(String[] args) throws IOException, NoQueryFoundException, DataStoreException {

		PostgresSQL pg = new PostgresSQL("schilling");

		
		
		
			
		
		int swallows;
		
		
		
		
		Database db = new Database();
		db.setExperimentTable("experiment3");
		db.init();

		String filenameString = null;


		
		swallows = 10;

		

		for (int currentSwallow = 1 ; currentSwallow <= swallows ; currentSwallow++) {

			log.info("Current Swallow is " + currentSwallow);
			
			System.out.println("SELECT sample_difference,accuracy,run_id,model_parameters,iteration_nr FROM " + getIterName() 
					+" WHERE proband = " + proband
					+ " AND split = " + currentSwallow + " ORDER BY sample_difference ASC LIMIT 1");

			//get the maximum accuracy, model parameters and the run_id to get the hyperparameters

			IEntityBacked query = pg.query(new SqlStatement("SELECT sample_difference,accuracy,run_id,model_parameters,"
					+ "iteration_nr FROM " + getIterName() 
					+" WHERE proband = " + proband 
					+ " AND split = " + currentSwallow + " ORDER BY accuracy DESC LIMIT 1"));
			
			

			Integer iteration_nr = 0;
			Integer run_id = 0;
			System.out.println(run_id);
			String model_parameters = null;
			double sample_difference = 9999;
			double accuracy = 0;

			while (query.hasNext()) {
				IContentHolder next = query.next();
				run_id = next.get("run_id", Integer.class);
				model_parameters = next.get("model_parameters", String.class);
				accuracy = next.get("accuracy", Double.class);
				sample_difference = next.get("sample_difference", Double.class);
				iteration_nr = next.get("iteration_nr", Integer.class);

			}
			
//			write("" + model_parameters, new File("/home/nico/parameters/model-P-"+getProband()+"-S-"+currentSwallow));
			
			query.close();
			
			log.info("Iteration Query has been successfull!");

			// get the hyperparameters
			IEntityBacked hyperQuery = null;

			Double step_size = 0d;
			Double lambda = 0d;
			Double smooth_reg = 0d;
			Integer smooth_window = 0;
			Integer window_extent = 0;

			if (!laplacian) {
				hyperQuery = pg.query(new SqlStatement("SELECT step_size , lambda, window_extent FROM " + getRunName()
						+ " WHERE _id = " + run_id ));

				while (hyperQuery.hasNext()) {
					IContentHolder next = hyperQuery.next();
					step_size = next.get("step_size" , Double.class);
					lambda = next.get("lambda", Double.class);
					window_extent = next.get("window_extent", Integer.class);

				}
				hyperQuery.close();
			}
			else {
				hyperQuery = pg.query(new SqlStatement("SELECT step_size , lambda , smooth_reg, smooth_window, window_extent"
						+ " FROM " + getRunName()
						+ " WHERE _id = " + run_id ));

				while (hyperQuery.hasNext()) {
					IContentHolder next = hyperQuery.next();
					step_size = next.get("step_size" , Double.class);
					lambda = next.get("lambda", Double.class);
					smooth_reg = next.get("smooth_reg", Double.class);
					smooth_window = next.get("smooth_window", Integer.class);
					window_extent = next.get("window_extent", Integer.class);
				}
				
				hyperQuery.close();
			}

			log.info("HyperQuery was successfull!");

			// write the stuff in a joined database!!
			
			System.out.println("Experiment is: " + getExperiment());
			System.out.println("Proband is: " + getProband());
			System.out.println("Split is: " + currentSwallow);
			System.out.println("Iteration Number is: "+ iteration_nr);
			System.out.println("Window Extent is: " + window_extent);
			System.out.println("Sample Difference is: " + sample_difference);
			System.out.println("Step Size is: " + step_size);
			System.out.println("Lambda is: " + lambda);
			System.out.println("SmoothReg is: " + smooth_reg);
			System.out.println("SmoothWindow is: " + smooth_window);

			

			db.addExperiment(getExperiment(), getProband(), currentSwallow, model_parameters, accuracy, sample_difference, step_size,
					lambda, smooth_reg, smooth_window, iteration_nr, window_extent);


		}
		
		

	}
	
	private static void write(String text, File file) throws IOException {
		FileWriter fw = new FileWriter(file);
		fw.write(text);
		fw.close();
	}

	public int getProband() {
		return proband;
	}

	public void setProband(int proband) {
		this.proband = proband;
	}

	public String getExperiment() {
		return experiment;
	}

	public void setExperiment(String experiment) {
		this.experiment = experiment;
	}

	public boolean isLaplacian() {
		return laplacian;
	}

	public void setLaplacian(boolean laplacian) {
		this.laplacian = laplacian;
	}

	public String getIterName() {
		return iterName;
	}

	public void setIterName(String iterName) {
		this.iterName = iterName;
	}

	public String getRunName() {
		return runName;
	}

	public void setRunName(String runName) {
		this.runName = runName;
	}
}