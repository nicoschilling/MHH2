package de.ismll.mhh.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import de.ismll.table.Matrices;
import de.ismll.table.Matrix;
import de.ismll.table.MatrixCallback;
import de.ismll.table.ReaderConfig;
import de.ismll.table.Vector;
import de.ismll.table.Vectors;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.impl.DefaultVector;
import de.ismll.utilities.Buffer;

public class Parser {

	private static final class AnnotationFileFormatParser implements
			MatrixCallback {
		public Matrix m;
		private int samplerate;

		public AnnotationFileFormatParser(int samplerate) {
			this.samplerate = samplerate;
		}

		@Override
		public void setField(int row, int col, String string) {
			if (string==null || string.length()==0) return;				
			switch (col) {
			case 0:
			case 1:
			case 2:
			case 8:
				m.set(row, col, Float.parseFloat(string));
				break;
			case 3: // sensor
			case 6: // sensor
				m.set(row, col, Float.parseFloat(string.substring(1)));
				break;
			case 4: // pmax Zeit
			case 5: // Restitutionszeit
			case 7: // absoluter Endzeitpunkt
				m.set(row, col, time2Sample(string, samplerate));
				break;
			default:
				System.err.println("Unknown column number " + col + " - discarding.");
			}
		}

		@Override
		public void meta(int numRows, int numColumns) {
			m = new DefaultMatrix(numRows, numColumns);
		}
	}
	
	public static final int ANNOTATION_COL_PROBAND_ID = 0;
	/**
	 * Durchschnittlicher Ruhedruck des annotierten Bereichs
	 */
	public static final int ANNOTATION_COL_RUHEDRUCK = 1;
	/**
	 * Der absolute Druck zum Zeitpunkt des maximalen drucks
	 */
	public static final int ANNOTATION_COL_PMAX_PRESSURE = 2;
	
	/**
	 * sample, an dem die Maximaldruckkurve das maximum hat 
	 */
	public static final int ANNOTATION_COL_PMAX_SAMPLE = 4;
	
	/**
	 * Die Zeitspanne zwischen Pmax und dem Wiedererreichen des Ruhedrucks 
	 */
	public static final int ANNOTATION_COL_RESTITUTIONSZEIT_DELTA = 5;
	/**
	 * Sample, an dem der Ruhedruck wieder erreicht ist.
	 */
	public static final int ANNOTATION_COL_RESTITUTIONSZEIT_SAMPLE = 7;

	static ReaderConfig annotationReaderConfig;
	static {
		annotationReaderConfig = new ReaderConfig();
		annotationReaderConfig.autodetectFormat=true;
		annotationReaderConfig.fieldSeparator='\t';
	}	
	
	public static Matrix readCSVFile(File in) throws IOException{
		String readFileCompletely = readFileCompletely(in);
		readFileCompletely = readFileCompletely.replaceAll("-Inf", "0");
		return Matrices.readDense(new ByteArrayInputStream(readFileCompletely.getBytes()), Matrices.headerlessCsvReaderConfig);		
	}
	
	@Deprecated
	public static int computeStartSample(Matrix matrix) {
		int startSample = 0;
		Vector[] columns;
		DefaultVector maxIndexes;
		
		
		// Ab hier wird HART kodiert!! cool;-)
		if (true) throw new RuntimeException("Ab hier wird HART kodiert!! uncool;-(");
		
		int firstChannel = 15;
		int lastChannel = 20;
		
		int channelDiff = lastChannel - firstChannel;
		
		columns = new Vector[channelDiff];
		maxIndexes = new DefaultVector(channelDiff);
		
		
		
		
		for (int i = 0 ; i < channelDiff; i++)
		{
			int col = i + firstChannel;
			columns[i] = Matrices.col(matrix, col);	
			float maxIdx = Vectors.maxIdx(columns[i]);
			maxIndexes.set(i, maxIdx);
		}
		
		startSample = (int) Vectors.min(maxIndexes);

		return startSample;
	}

	public static String readFileCompletely(File file) throws IOException {
		byte[] buffer =new byte[0];
		
		try (InputStream fis = Buffer.newInputStream(file)){
			
			buffer = new byte[(int) file.length()];
			fis.read(buffer);
		}
		return new String(buffer);
	}
	
	public static Matrix readAnnotations(File f, int samplerate) throws IOException {
		return readAnnotations(Buffer.newInputStream(f), samplerate);
	}
	
	public static Matrix readAnnotations(File f, int samplerate, int normalizeIndex) throws IOException {
		return readAnnotations(Buffer.newInputStream(f), samplerate, normalizeIndex);
	}
	
	public static Matrix readAnnotations(InputStream newInputStream, int samplerate) throws IOException {
		return readAnnotations(newInputStream, samplerate, 0);
	}
	
	public static Matrix readAnnotations(InputStream newInputStream, int samplerate, int normalizeIndex) throws IOException {
		AnnotationFileFormatParser annotationFileFormatParser = new AnnotationFileFormatParser(samplerate);
		Matrices.readDense(newInputStream, annotationReaderConfig, annotationFileFormatParser);
		
		Matrix ret = annotationFileFormatParser.m;
		
		if (normalizeIndex>0) {
			Matrices.addColumn(ret, ANNOTATION_COL_RESTITUTIONSZEIT_SAMPLE, -1*normalizeIndex);
			Matrices.addColumn(ret, ANNOTATION_COL_PMAX_SAMPLE, -1*normalizeIndex);
		}
		
		return ret;
	}

	@Deprecated
	// no use of samplerate!!!
	public static int time2Sample(String time) {
		String[] split = time.split(":");
		int first = Integer.parseInt(split[0]);
		int second = Integer.parseInt(split[1]);
		return first * 60 + second;
	}

	public static int time2Sample(String time, int samplerate) {
		int add = 0;
		if (time.contains(",")) {
			int indexOf = time.indexOf(',');
			String fraction = time.substring(indexOf+1);
			add = (int) (Float.parseFloat("0." + fraction) * samplerate); 
			time = time.substring(0, indexOf);
		}
		String[] split = time.split(":");
		int first = Integer.parseInt(split[0]);
		int second = Integer.parseInt(split[1]);
		return (first * 60 + second) * samplerate + add;
		
	}
	
	
	public static String sample2Time(int sample, int samplerate) {
		
		
		String ret = "";
		float totalSeconds =  ( (float) sample) / ( (float) samplerate);
		
		int minutes = (int) Math.floor(totalSeconds/60);
		
		double seconds = totalSeconds - 60*minutes;
		
		double brokenSeconds = (seconds - Math.floor(seconds))*100;
		
		String minutesString = "";
		String secondsString = "";
		String brokenSecondsString = "";
		
		if (minutes < 10) { minutesString = "0" + minutes; }
		else { minutesString = "" + minutes; }
		
		if ((int) seconds < 10 ) { secondsString = "0" + (int) seconds;}
		else { secondsString = "" + (int) seconds;}
		
		if ( (int) brokenSeconds < 10 ) { brokenSecondsString = "0" + (int) brokenSeconds; }
		else { brokenSecondsString = "" + (int) brokenSeconds;}
		
		
		
		
		ret = minutesString + ":" + secondsString + "," +  brokenSecondsString;
		
		
		return ret;
		
	}
	
}
