package de.ismll.mhh;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

public enum Probanden {
	Proband1("00506534Schluck"), Proband2("00511910Schluck"), Proband3("00518402Schluck");
	
	String folder;
	
	Probanden(String folder){
		this.folder = folder;
	}
	
	public File expand(File baseDir, int schluck) {
		return new File(baseDir, this.name() + "/" + folder + schluck + ".ASC.data");
	}

	public File[] allDirs(File baseDir) {
		File schluckdir = new File(baseDir, this.name());
	 
		FilenameFilter ff = new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				boolean ret= name.startsWith(folder) && name.endsWith(".ASC.data");
				return ret;
			}
		};
		
		return schluckdir.listFiles(ff);
		
	}

}
