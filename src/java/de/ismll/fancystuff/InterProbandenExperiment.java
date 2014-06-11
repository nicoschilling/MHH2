package de.ismll.fancystuff;

import java.io.File;
import java.util.Arrays;

import de.ismll.bootstrap.BootstrapAssertions;
import de.ismll.bootstrap.Parameter;
import de.ismll.mhh.Probanden;
import de.ismll.mhh.io.DataInterpretation;
import de.ismll.mhh.io.SwallowData;
import de.ismll.utilities.Assert;

public class InterProbandenExperiment implements Runnable{

	@Parameter(cmdline="training", description="an enumeration of training Probands ;-)")
	private
	String[] trainingProband;
	
	@Parameter(cmdline="basedir")
	private File baseDir = new File("M:/");
	
	
	@Override
	public void run() {
		BootstrapAssertions.notNull(this, trainingProband, "training");
		
		// read
		for (String s : trainingProband) {
			System.out.println("Lese Proband " + s);
			
			Probanden proband = Probanden.valueOf(s);
			
			
			File[] allDirs = proband.allDirs(baseDir);
			
			for (File schluckDir : allDirs) {
				
				
	//			File schluckDir = proband.expand(baseDir, 1);
	//			
				DataInterpretation rf = new DataInterpretation();
				rf.setDataInterpretation(schluckDir);
				rf.run();
				
				
				System.out.println(rf.getDruck().getNumRows());
					
	//			System.out.println(schluckDir);
			}
			
//			System.out.println(Arrays.toString(allDirs));
			
			
		}
		
		
		// execute
		
		
		
		// write

		
	}

	public String[] getTrainingProband() {
		return trainingProband;
	}

	public void setTrainingProband(String[] trainingProband) {
		this.trainingProband = trainingProband;
	}

	public File getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}
}
