package de.ismll.mhh.io;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;

import de.ismll.bootstrap.BootstrapException;
import de.ismll.bootstrap.CommandLineParser;
import de.ismll.bootstrap.Parameter;
import de.ismll.table.Matrix;

public class DataInterpretation extends SwallowData implements Runnable{

	static final String FILENAME_RDEND = "rdend";

	static final String FILENAME_RDSTART = "rdstart";

	static final String FILENAME_CHANNELEND = "channelend";

	static final String FILENAME_CHANNELSTART = "channelstart";

	static final String FILENAME_FFT_CSV = "fft.csv";

	static final String FILENAME_PMAX_MANUAL = "pmax_manuell";
	
	static final String FILENAME_ACID_LEVEL = "acid_level";

	public static final int PMAX_MANUAL_DEFAULT = -1;

	public static final int DEFAULT_ANNOTATED_RESTITUTION_SAMPLE_WITHOUT_FILE = (int) Double.NaN;

	private Matrix fft;
	
	@Parameter(cmdline="interpretation")
	private File dataInterpretation;
	
	private String channelstart;
	private String channelend;
	private String rdend;
	private String rdstart;
	private int rdStartSample;
	
	private String acid_level;

	private int pmaxManuell = PMAX_MANUAL_DEFAULT;
	
	private int rdEndSample;

	private int pmaxFromFile = -1;

	private int annotatedRestitutionTimeSample= -1;
	
	@Override
	public void run() {
		
		if(new File(dataInterpretation.getParentFile(), FILENAME_DATA_CSV).exists()) {
			super.setSchluckverzeichnis(dataInterpretation.getParentFile());
		}
		else {
			super.setSchluckverzeichnis(dataInterpretation);
		}
		
		super.run();
		
		File acidFile = new File(dataInterpretation, FILENAME_ACID_LEVEL);
		acid_level="unknown";
		if (acidFile.exists()) {
			try {
				acid_level = Parser.readFileCompletely(
						acidFile)
						.trim();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			fft = Parser.readCSVFile(new File(dataInterpretation,
					FILENAME_FFT_CSV));
		} catch (IOException e) {
			throw new RuntimeException(
					"Failed to load fft file "
							+ FILENAME_FFT_CSV);
		}
		try {
			channelstart = Parser.readFileCompletely(
					new File(dataInterpretation, FILENAME_CHANNELSTART))
					.trim();
		} catch (IOException e) {
			throw new RuntimeException(
					"Failed to load channel start file "
							+ FILENAME_CHANNELSTART);
		}
		try {
			channelend = Parser.readFileCompletely(
					new File(dataInterpretation, FILENAME_CHANNELEND))
					.trim();
		} catch (IOException e) {
			throw new RuntimeException(
					"Failed to load channel end file "
							+ FILENAME_CHANNELEND);
		}
		try {
			rdstart = Parser.readFileCompletely(
					new File(dataInterpretation, FILENAME_RDSTART)).trim();
		} catch (IOException e) {
			throw new RuntimeException(
					"Failed to load rd start file "
							+ FILENAME_RDSTART);
		}
		try {
			rdend = Parser.readFileCompletely(                                                                      
				new File(dataInterpretation, FILENAME_RDEND)).trim();                                      
		} catch (IOException e) {
			throw new RuntimeException(
					"Failed to load rd end file " + FILENAME_RDEND);
		}
		int samplerateI = Integer.parseInt(samplerate);
		int rdStartTime = Parser.time2Sample(rdstart.trim());
		rdStartSample = rdStartTime * samplerateI; 
		log.info("Ruhedruck faengt an bei Zeit " + rdStartTime + " (Sample: " + rdStartSample + ", relativ: " + (rdStartSample-firstSample) + ")");
		int rdEndTime = Parser.time2Sample(rdend.trim());
		rdEndSample = rdEndTime * samplerateI; 
		log.info("Ruhedruck hoert auf bei Zeit " + rdEndTime + " (Sample: " + rdEndSample + "), relativ: " + (rdEndSample-firstSample) + ")");
		
		File pmax_manuell = new File(dataInterpretation, FILENAME_PMAX_MANUAL);
		if (!pmax_manuell.exists()) {
			log.debug("No manual pmax file exists / detected. Using default (" + PMAX_MANUAL_DEFAULT + ")");
		} else {
			try {
				
				String pmax_manualS = Parser.readFileCompletely(pmax_manuell).trim();
				this.pmaxManuell = Parser.time2Sample(pmax_manualS, samplerateI);
				log.info("Using manual pmax sample at index " + pmaxManuell);
			} catch (IOException e) {				
				throw new RuntimeException(
						"Failed to load data interpretation file" + FILENAME_RDEND);
			}
		}
		
		
	}

	public static DataInterpretation convert(Object in) {
		URI source = (URI) CommandLineParser.convert(in, URI.class);
		switch (source.getScheme()) {
		case "file":
			DataInterpretation di = new DataInterpretation();
			di.setDataInterpretation(new File(source));
			di.run();
			return di;			
		}
	
		throw new BootstrapException("Could not convert " + in + " into a valid DataInterpretation object.");
	}
	
	public void toMathematicaOutput(PrintStream where) {
//		o(where,"dataDruck", druck);
//		o(where,"data", fft);
		oln(where,"startIdx=" + channelstart.trim().substring(1) + ";");
		oln(where,"endIdx=" + channelend.trim().substring(1) + ";");
		oln(where,"startTraining=" + (int)(Parser.time2Sample(rdstart.trim(),Integer.parseInt(samplerate))-druck.get(0, 0)) + ";");
		oln(where,"endTraining=" + (int)(Parser.time2Sample(rdend.trim(),Integer.parseInt(samplerate))-druck.get(0, 0)) + ";");
	}

	public Matrix getFft() {
		return fft;
	}

	public String getChannelstart() {
		return channelstart;
	}

	public int getChannelstartAsInt() {
		String myChannelstart= channelstart;
		if (myChannelstart.startsWith("P"))
			myChannelstart=myChannelstart.substring(1);
		return Integer.valueOf(myChannelstart);
	}

	public String getChannelend() {
		return channelend;
	}
	
	public int getChannelendAsInt() {
		String myChannelend= channelend;
		if (myChannelend.startsWith("P"))
			myChannelend=myChannelend.substring(1);
		return Integer.valueOf(myChannelend);
	}


	public String getRdend() {
		return rdend;
	}


	public String getRdstart() {
		return rdstart;
	}

	public int getRdStartSample() {
		return rdStartSample;
	}

	public void setRdStartSample(int rdStartSample) {
		this.rdStartSample = rdStartSample;
	}

	public int getRdEndSample() {
		return rdEndSample;
	}

	public void setRdEndSample(int rdEndSample) {
		this.rdEndSample = rdEndSample;
	}

	public final void setFft(Matrix fft) {
		this.fft = fft;
	}

	public final void setChannelstart(String channelstart) {
		this.channelstart = channelstart;
	}

	public final void setChannelend(String channelend) {
		this.channelend = channelend;
	}

	public final void setRdend(String rdend) {
		this.rdend = rdend;
	}

	public final void setRdstart(String rdstart) {
		this.rdstart = rdstart;
	}

	public File getDataInterpretation() {
		return dataInterpretation;
	}

	public void setDataInterpretation(File interpretationDirectory) {
		expect(FILENAME_FFT_CSV);
		expect(FILENAME_CHANNELSTART);
		expect(FILENAME_CHANNELEND);
		expect(FILENAME_RDSTART);
		expect(FILENAME_RDEND);
		
		this.dataInterpretation = interpretationDirectory;
	}

	public int getPmaxManuell() {
		return pmaxManuell;
	}

	public void setPmaxManuell(int pmaxManuell) {
		this.pmaxManuell = pmaxManuell;
	}

	public String getAcid_level() {
		return acid_level;
	}

	public void setAcid_level(String acid_level) {
		this.acid_level = acid_level;
	}

	public int getAnnotatedPmaxSample(String annotationBaseDir, String annotator) {
		if (pmaxFromFile >= 0)
			return pmaxFromFile;
		
		pmaxFromFile = readAnnotationFile (annotationBaseDir, annotator, Parser.ANNOTATION_COL_PMAX_SAMPLE, -1);

		return pmaxFromFile;
	}

	/**
	 * Returns the absolute Annotation for a given Swallow in a Read Folder Object. If there is no annotation, "NaN" will be returned
	 */
	public int getAnnotatedRestitutionTimeSample(String annotationBaseDir, String annotator) {
		if (annotatedRestitutionTimeSample >= 0 )
			return annotatedRestitutionTimeSample;
		
		annotatedRestitutionTimeSample = readAnnotationFile (annotationBaseDir, annotator, Parser.ANNOTATION_COL_RESTITUTIONSZEIT_SAMPLE, DEFAULT_ANNOTATED_RESTITUTION_SAMPLE_WITHOUT_FILE);
		
		return annotatedRestitutionTimeSample;
	}
	
	private int readAnnotationFile(String annotationBaseDir, String annotator, int column, int defaultReturn) {
		String annotationPath = annotationBaseDir + File.separator + proband + "-" + annotator + ".tsv";
		log.debug("The annotation path is: " + annotationPath);

		int ret = defaultReturn;
		try {
			Matrix annotations = Parser.readAnnotations(new File(annotationPath), getSamplerateAsInt());
			ret = (int) annotations.get(swallowId-1, column);
			log.info("Loaded value for column " + column + " from the file " + annotationPath + ". It is " + ret);
		} catch (IOException e) {
			
		} catch (ArrayIndexOutOfBoundsException t){
			log.info("There was an array out of bounds when reading annotations.... did you access the right file?");
		}
		return ret;

	}

}
