package de.ismll.mhh.io;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.bootstrap.BootstrapAssertions;
import de.ismll.bootstrap.Parameter;
import de.ismll.table.Matrix;

public class SwallowData implements Runnable{

	public static final String FILENAME_ID = "id";
	public static final String FILENAME_SAMPLERATE = "samplerate";
	public static final String FILENAME_DATA_CSV = "data.csv";
	public static final String FILENAME_PROBAND = "proband";
	@Parameter(cmdline = "input")
	private File schluckverzeichnis;
	protected Matrix druck;
	protected String samplerate;
	protected Logger log = LogManager.getLogger(getClass());
	protected int firstSample;
	private int lastSample;
	private int swallowId;
	private int proband;

	public SwallowData() {
		super();
	}

	@Override
	public void run() {
		BootstrapAssertions.notNull(this, schluckverzeichnis, "schluckverzeichnis");
		String id = null;
		try {
			druck = Parser.readCSVFile(new File(schluckverzeichnis, FILENAME_DATA_CSV));
			
		
			samplerate= Parser.readFileCompletely(new File(schluckverzeichnis, FILENAME_SAMPLERATE)).trim();
			File idFile = new File(schluckverzeichnis, FILENAME_ID);
			if (idFile.exists()) {
				id = Parser.readFileCompletely(idFile).trim();
			}
			File probandFile = new File(schluckverzeichnis, FILENAME_PROBAND);
			if (probandFile.exists())
				proband = Integer.parseInt(Parser.readFileCompletely(probandFile).trim());
	
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		if (id != null) {
			this.setSwallowId(Integer.parseInt(id));
		} else {
			log.warn("id for swallow " + schluckverzeichnis + " is not existing. This may result in a future failure / exception ...");
		}
		
		firstSample = (int)druck.get(0, 0);
		lastSample = (int)druck.get(druck.getNumRows()-1, 0);
		log.info("Erstes Sample in der Datei ist " + firstSample);
		log.info("Letztes Sample in der Datei ist " + lastSample);
		
		log.info("Samplerate ist " + samplerate);
		
		
	}

//	private void o(PrintStream where, String string, Matrix druck) {
//		o(where,string);
//		o(where,"={");
//		
//		for (int r = 0; r < druck.getNumRows(); r++) {
//			o(where,"{");
//			for (int c = 0; c < druck.getNumColumns(); c++) {
//				o(where,druck.get(r, c) + "");
//				if (c <druck.getNumColumns()-1) o(where,",");
//			}
//			o(where,"}");
//			if (r<druck.getNumRows()-1) oln(where,",");
//		}
//		
//		oln(where,"};");
//		
//			
//	}

//	private static void o(PrintStream where, String string) {
//		where.print(string);
//	}

	protected static void oln(PrintStream where, String string) {
		where.println(string);
	}

	public File getSchluckverzeichnis() {
		return schluckverzeichnis;
	}

	public void setSchluckverzeichnis(File schluckverzeichnis) {
		this.schluckverzeichnis = schluckverzeichnis;
		expect(FILENAME_DATA_CSV);
		expect(FILENAME_SAMPLERATE);
		if (!expect(FILENAME_ID, false))
			swallowId=-1;
		if (!expect(FILENAME_PROBAND, false)) {
			proband = -1;  
		}
	}

	protected void expect(String filename) {
		expect(filename, false);
	}

	private boolean expect(String filename, boolean throwException) {
		if (!new File(schluckverzeichnis, filename).isFile()) {
			if (throwException)
				throw new RuntimeException("File \"" + filename + "\" in directory \"" + schluckverzeichnis + " \"expected!");
		}
		return false;
	}

	public Matrix getDruck() {
		return druck;
	}

	public String getSamplerate() {
		return samplerate;
	}


	public int getFirstSample() {
		return firstSample;
	}

	public void setFirstSample(int firstSample) {
		this.firstSample = firstSample;
	}

	public int getLastSample() {
		return lastSample;
	}

	public void setLastSample(int lastSample) {
		this.lastSample = lastSample;
	}


	public int getProband() {
		return proband;
	}

	public void setProband(int proband) {
		this.proband = proband;
	}

	public int getSamplerateAsInt() {
		return Integer.parseInt(samplerate);
	}

	public final void setDruck(Matrix druck) {
		this.druck = druck;
	}

	public final void setSamplerate(String samplerate) {
		this.samplerate = samplerate;
	}

	public boolean initialized() {
		return druck!=null; // Uggh - better check required to check whether all required fields are set here...
	}

	public int getSwallowId() {
		return swallowId;
	}

	public void setSwallowId(int swallowId) {
		this.swallowId = swallowId;
	}

}