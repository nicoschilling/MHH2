package de.ismll.secondversion;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.database.SqlStatement;
import de.ismll.database.dao.DataStoreException;
import de.ismll.database.dao.IEntityBacked;
import de.ismll.database.dao.PgStore;

public class DBEvaluator {
	
	Database database;
	private Logger log = LogManager.getLogger(getClass());
	
	
	public void getOptimalHyperparameters() {
		try {
			// Initialize Database
			database = new Database();
			
		} catch (IOException | DataStoreException e1) {
			log.fatal("Could not connect to database", e1);
			System.exit(1);
			return;			
		}
		
		PgStore pgstore = database.getPgStore();
		
//		for (int split = 1 ; split <= 12 ; split++) {
			int split =1;
			try (IEntityBacked entity = pgstore.query(new SqlStatement("SELECT accuracy,run_id FROM iterintra WHERE proband = 1 "
						+ "AND split = " + split + "ORDER BY accuracy DESC"))){
								
			} catch (DataStoreException e) {
				e.printStackTrace();
			}
		
//		}
		
	}

	

	
	

	
	
	
	
	

}
