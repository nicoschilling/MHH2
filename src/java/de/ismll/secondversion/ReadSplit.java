package de.ismll.secondversion;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.ismll.bootstrap.BootstrapException;
import de.ismll.mhh.io.DataInterpretation;

public class ReadSplit implements Runnable{
	
	public static ReadSplit convert(Object args) {
		return new ReadSplit(new File(args.toString()));
	}
	
	
	
	/**
	 * Pfad des zu lesenden Splits
	 * @author nico
	 * 
	 */
	public File splitFolder;
	
	/**
	 * Array von Train ReadFolder Objekten
	 * @author nico
	 *	
	 */
	public DataInterpretation[] trainFolders;
	/**
	 * Array von Test ReadFolder Objekten
	 * @author nico
	 *	
	 */
	public DataInterpretation[] testFolders;
	/**
	 * Array von Validation ReadFolder Objekten
	 * @author nico
	 *	
	 */
	public DataInterpretation[] validationFolders;
	
	/**
	 * Liste der Files in /train
	 * @author nico
	 *
	 */
	public File[] trainList;
	/**
	 * Liste der Files in /test
	 * @author nico
	 *
	 */
	public File[] testList;
	
	/**
	 * Liste der Files in /validation
	 * @author nico
	 *
	 */
	public File[] validationList;
	
	
	/**
	 * Anzahl der Train Schluecke
	 * @author nico
	 *	
	 */
	public int trainFoldersLength;
	/**
	 * Anzahl der Test Schluecke
	 * @author nico
	 *	
	 */
	public int testFoldersLength;
	/**
	 * Anzahl der Validation Schluecke
	 * @author nico
	 *	
	 */
	public int validationFoldersLength;
	
	public ReadSplit(File newSplitFolder) {
		setFields(newSplitFolder);
	}

	@Override
	public void run() {
		
		testFolders = new DataInterpretation[testFoldersLength];
		trainFolders = new DataInterpretation[trainFoldersLength];
		validationFolders = new DataInterpretation[validationFoldersLength];
		
		for (int i = 0; i < testFoldersLength ; i++) {
			testFolders[i] = new DataInterpretation();
		}
		
		for (int i = 0; i < trainFoldersLength ; i++) {
			trainFolders[i] = new DataInterpretation();
		}
		
		for (int i = 0; i < validationFoldersLength ; i++) {
			validationFolders[i] = new DataInterpretation();
		}
		
		
		// at least one thread (slave) in the threadpool
		// BUG-2014-05-11: Reading multi-thread is NOT possible! Use a single thread as a workaround instead. 
//		ExecutorService slaves = Executors.newFixedThreadPool(
//				Math.max(
//						1, 
//						Runtime.getRuntime().availableProcessors()/2));
		ExecutorService slaves = Executors.newFixedThreadPool(1);
		
		
		int i = 0;
		for (File file : testList)
		{
			testFolders[i].setDataInterpretation(file);
			slaves.submit(testFolders[i]);
			i++;
		}
		
		i = 0;
		for (File file : trainList) 
		{
			trainFolders[i].setDataInterpretation(file);
			slaves.submit(trainFolders[i]);
			i++;
		}
		
		i = 0;
		for (File file : validationList)
		{
			validationFolders[i].setDataInterpretation(file);
			slaves.submit(validationFolders[i]);
			i++;
		}
		
		slaves.shutdown();
		// wait 180 seconds to read all files (or interrupt otherwise)
		try {
			slaves.awaitTermination(360, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new BootstrapException("Unable to read all data in 360 seconds");
		}
		
	}

	public File getSplitFolder() {
		return splitFolder;
	}

	public void setFields(File splitFolder) {
		this.setSplitFolder(splitFolder);

		File trainFolder = new File(splitFolder.getAbsolutePath()+"/train");
		if (!trainFolder.isDirectory()) {
			throw new RuntimeException("Illegal subdirectory structure, subdirectory \"train\" expected in folder " + splitFolder + " !");
		}
		this.trainList = trainFolder.listFiles();
		this.trainFoldersLength = trainList.length;
		
		File testFolder = new File(splitFolder.getAbsolutePath()+"/test");
		if (!testFolder.isDirectory()) {
			throw new RuntimeException("Illegal subdirectory structure, subdirectory \"test\" expected in folder " + splitFolder + " !");
		}
		this.testList = testFolder.listFiles();
		this.testFoldersLength = testList.length;
		
		File validationFolder = new File(splitFolder.getAbsolutePath()+"/validation");
		if (!validationFolder.isDirectory()) {
			throw new RuntimeException("Illegal subdirectory structure, subdirectory \"validation\" expected in folder " + splitFolder + " !");
		}
		this.validationList = validationFolder.listFiles();
		this.validationFoldersLength = validationList.length;
	}
		
	@Override
	public String toString() {
		return "ReadSplit-Object at " + getSplitFolder().getAbsolutePath() + " (@" + hashCode()+ ")";
	}

	public void setSplitFolder(File splitFolder) {
		this.splitFolder = splitFolder;
	}

}
