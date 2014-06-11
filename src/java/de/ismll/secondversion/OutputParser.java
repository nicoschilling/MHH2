package de.ismll.secondversion;

import java.io.IOException;

import de.ismll.bootstrap.Parameter;
import de.ismll.database.dao.IContentHolder;
import de.ismll.database.dao.DataStoreException;
import de.ismll.database.dao.Datatypes;
import de.ismll.database.dao.Entity;
import de.ismll.database.dao.IEntityBacked;
import de.ismll.database.dao.PgStore;
import de.ismll.database.dao.Table;
import de.ismll.database.pgsql.PostgresSQL;

public class OutputParser implements Runnable{
	
	@Parameter(cmdline="iterName", description="name of Database")
	private String iterName;
	
	@Parameter(cmdline="splitName" , description="name of Split Database")
	private String splitName;
	
	
	
	/**
	 * Table with iterations, accuracy and a unique split number
	 */
	private Table iterations;
	
	/**
	 * Table with a unique split number and given Hyperparameter combinations
	 */
	private Table splitrun;

	private PgStore p1;

	@Override
	public void run() {
		
		try {
			p1 = new PgStore(new PostgresSQL());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
//		iterations = new Table(iterName);
//		
//		iterations.addColumns(new String[] {
//				"Key",
//				"iteration",
//				"accuracy",
//				"prediction",
//				"annotation",
//				"absoluteSampleDifference"
//		}, Datatypes.VString);
		
		splitrun = new Table(splitName);
		
		splitrun.addColumns(new String[] {
				"Key",
				"ProbandId",
				"SplitId"				
		}, Datatypes.Integer);
		splitrun.addColumns(new String[] {
				"stepSize",
				"lambda",
				"windowExtent"
		}, Datatypes.Double);
		
		try {
			p1.ensureTableExists(iterations);
		} catch (DataStoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Entity createInstance = splitrun.createInstance();
		createInstance.set("Key", -1);
		createInstance.set("ProbandId", 5);
		createInstance.set("SplitId", 3);
		
		// store to DB
		try {
			p1.insertOrUpdate(createInstance);
		} catch (DataStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		try (IEntityBacked query = p1.query(splitrun)){
			
			while (query.hasNext()) {
				IContentHolder next = query.next();
				Object object = next.get("Key");
				System.out.println(object);
			}
			
		} catch (DataStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	

}
