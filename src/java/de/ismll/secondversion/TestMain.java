package de.ismll.secondversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import de.ismll.bootstrap.CommandLineParser;
import de.ismll.bootstrap.Parameter;
import de.ismll.database.dao.DataStoreException;
import de.ismll.exceptions.ModelApplicationException;
import de.ismll.mhh.analyze.ApplyOnTest;
import de.ismll.mhh.analyze.ApplySVMHMM;
import de.ismll.mhh.analyze.GridSearchEval;
import de.ismll.mhh.io.DataInterpretation;
import de.ismll.mhh.io.Parser;
import de.ismll.mhh.methods.ApplyModel;
import de.ismll.mhh.methods.EvaluateModel;
import de.ismll.mhh.methods.MHHModelFactory;
import de.ismll.runtime.files.NoQueryFoundException;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.impl.DefaultVector;
import de.ismll.table.projections.ColumnSubsetMatrixView;
import de.ismll.table.projections.ColumnUnionMatrixView;
import de.ismll.table.projections.RowUnionMatrixView;

public class TestMain {
	
	
	
	
	public static void main(String[] args) throws ModelApplicationException, IOException, NoQueryFoundException, DataStoreException {
		
		DataInterpretation folder = new DataInterpretation();
		
		folder.setDataInterpretation(new File("/home/nico/acogpr/NormalSwallows/Proband2/Schluck2"));
		
		folder.run();
		
		System.out.println(folder.getChannelend());
		
	}



	

}
