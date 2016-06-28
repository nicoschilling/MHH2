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
	private final File splitFolder;
	
	/**
	 * Array von Train ReadFolder Objekten
	 * @author nico
	 *	
	 */
	public final DataInterpretation[] trainFolders;
	/**
	 * Array von Test ReadFolder Objekten
	 * @author nico
	 *	
	 */
	public final DataInterpretation[] testFolders;
	/**
	 * Array von Validation ReadFolder Objekten
	 * @author nico
	 *	
	 */
	public final DataInterpretation[] validationFolders;
	
	/**
	 * Liste der Files in /train
	 * @author nico
	 *
	 */
	private final File[] trainList;
	
	/**
	 * Liste der Files in /test
	 * @author nico
	 *
	 */
	private final File[] testList;
	
	/**
	 * Liste der Files in /validation
	 * @author nico
	 *
	 */
	private final File[] validationList;
	
	
	public ReadSplit(File newSplitFolder) {
		this.splitFolder = newSplitFolder;

		File trainFolder = new File(splitFolder.getAbsolutePath()+"/" + Proportion.TRAIN.getLabel());
		if (!trainFolder.isDirectory()) {
			throw new RuntimeException("Illegal subdirectory structure, subdirectory \"train\" expected in folder " + splitFolder + " !");
		}
		this.trainList = trainFolder.listFiles();
		
		File testFolder = new File(splitFolder.getAbsolutePath()+"/"+ Proportion.TEST.getLabel());
		if (!testFolder.isDirectory()) {
			throw new RuntimeException("Illegal subdirectory structure, subdirectory \"test\" expected in folder " + splitFolder + " !");
		}
		this.testList = testFolder.listFiles();
		
		File validationFolder = new File(splitFolder.getAbsolutePath()+"/" + Proportion.VALIDATION.getLabel());
		if (!validationFolder.isDirectory()) {
			throw new RuntimeException("Illegal subdirectory structure, subdirectory \"validation\" expected in folder " + splitFolder + " !");
		}
		this.validationList = validationFolder.listFiles();

		testFolders = new DataInterpretation[testList.length];
		trainFolders = new DataInterpretation[trainList.length];
		validationFolders = new DataInterpretation[validationList.length];
	}

	@Override
	public void run() {
		
		
		for (int i = 0; i < testFolders.length ; i++) {
			testFolders[i] = new DataInterpretation();
		}
		
		for (int i = 0; i < trainFolders.length ; i++) {
			trainFolders[i] = new DataInterpretation();
		}
		
		for (int i = 0; i < validationFolders.length; i++) {
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
		// wait 360 seconds to read all files (or interrupt otherwise)
		try {
			slaves.awaitTermination(360, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new BootstrapException("Unable to read all data in 360 seconds");
		}
		
	}

	public final File getSplitFolder() {
		return splitFolder;
	}
		
	@Override
	public String toString() {
		return "ReadSplit-Object at " + getSplitFolder().getAbsolutePath() + " (@" + hashCode()+ ")";
	}

	public final File[] getTrainList() {
		return trainList;
	}

}
