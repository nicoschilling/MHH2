package de.ismll.mhh.analyze;

import java.io.File;
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
import de.ismll.table.Matrices;
import de.ismll.table.Matrices.FileType;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;

public class Tester {
	
	private static Logger logger = LogManager.getLogger(Tester.class);


	public static void main(String[] args) throws IOException, DataStoreException {
//		XmlRepository repo = new XmlRepository();
//		File repoFile = PostgresSQL.findFile("sqlQueries.sql.xml", logger, true);
//		if (!repoFile.exists())
//			throw new RuntimeException("File " + repoFile.getAbsolutePath() + " does not exist!");
//		repo.setXmlFile(repoFile);
//		
//		System.out.println("Kombis-Query ist:");
//		System.out.println(repo.compileQuery("kombis", ""));
//
//		System.out.println("best_result ist:");
//		String compileQuery = repo.compileQuery("best_result", 1, 2, 0.846757054328918);
//		System.out.println(compileQuery);
		
		
		
		PostgresSQL pg = new PostgresSQL("schilling");
		String experiment = "iterintra";
		

		
		Integer run_id = 0;
		String parameters = null;
		
		try (IEntityBacked query = pg.query(new SqlStatement("SELECT accuracy,run_id,model_parameters FROM " + experiment +" WHERE proband = 1 "
				+ "AND split = " + 2 + "ORDER BY accuracy DESC LIMIT 1"))){
			while (query.hasNext()) {
				IContentHolder next = query.next();
				run_id = next.get("run_id", Integer.class);
				parameters = next.get("model_parameters", String.class);
						
			}
		}
		
	    Vector testVector = Vectors.convert(parameters);
		
		for (int i = 0; i < testVector.size(); i++) {
			System.out.println(testVector.get(i));
		}
		
		
	}
}
