package de.ismll.secondversion;

import static org.junit.Assert.*;

import java.io.File;

import static de.ismll.secondversion.DatasetFormat.*;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.ismll.exceptions.ModelApplicationException;
import de.ismll.mhh.io.DataInterpretation;
import de.ismll.table.Matrix;
import de.ismll.table.impl.DefaultMatrix;

public class PreprocessDataTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private class PreprocessDataExtension extends PreprocessData {

		@Override
		public Matrix concatenate(DataInterpretation folder) throws DataValidationException {
			return super.concatenate(folder);
		}
		
	}
	
	private PreprocessDataExtension sut;
	
	@Before
	public void setUp() throws Exception {
		sut = new PreprocessDataExtension();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPreprocessSwallow() {
		DataInterpretation inputData;
		int inputAnnotation;
		int inputPmax;
		boolean inputSkipBetween;
		boolean inputSkipLeading;
		
//		SwallowDS result = sut.preprocessSwallow(inputData, inputAnnotation, inputPmax, inputSkipLeading, inputSkipBetween);
		
		
		//fail("Not yet implemented");
	}

	@Test
	public void testConcatenateLoggerDataInterpretationIntBooleanIntFail1() {
		final int inputPmaxSample = 1;

		DataInterpretation inputDataInterpretation = new DataInterpretation() {
			@Override
			public int getAnnotatedPmaxSample(String annotationBaseDir, String annotator) {
				return inputPmaxSample;
			}
		};
		inputDataInterpretation.setChannelstart("P1");
		inputDataInterpretation.setChannelend("P3");
		inputDataInterpretation.setFirstSample(2);
		inputDataInterpretation.setLastSample(3);
		inputDataInterpretation.setSamplerate("50");
		inputDataInterpretation.setDruck(DefaultMatrix.wrap(new float[][] {
			{1,2,3,4,5},
			{2,4,6,8,10},
			{9,8,7,6,5}
		}));
		inputDataInterpretation.setFft(DefaultMatrix.wrap(new float[][] {
			{4,5,6},
			{6,7,8},
			{1,2,3}
		}));
		
		try {
			Matrix result = sut.concatenate(inputDataInterpretation);
			fail("Shall throw an exception, because 1 (inputPmaxSample) is smaller than the first sample");
		} catch (DataValidationException e) {
		}
	
	
	}

	@Test
	public void testConcatenateLoggerDataInterpretationIntBooleanInt() throws DataValidationException {
		DataInterpretation inputDataInterpretation = new DataInterpretation();
		inputDataInterpretation.setChannelstart("2");
		inputDataInterpretation.setChannelend("3");
		inputDataInterpretation.setFirstSample(0);
		inputDataInterpretation.setLastSample(2);
		inputDataInterpretation.setAcid_level("");
		inputDataInterpretation.setSwallowId(101);
		inputDataInterpretation.setSamplerate("50");

		inputDataInterpretation.setDruck(DefaultMatrix.wrap(new float[][] {
			{1,2,3,4,5},
			{2,4,6,8,10},
			{9,8,7,6,5}
		}));
		inputDataInterpretation.setFft(DefaultMatrix.wrap(new float[][] {
			{4,5,6},
			{6,7,8},
			{1,2,3}
		}));
		
		// no annotation given in the DataInterpretation object is not backed with a file.
		int inputRestitutionszeitSample=DataInterpretation.DEFAULT_ANNOTATED_RESTITUTION_SAMPLE_WITHOUT_FILE;
		/*
		 * means: compute the sample; idx of max(P1:P3) from druckData
		 * 
		 * calculation will lead to inputPmaxSample==2, because 2 is the value 
		 * of the first column in the data, whose idx is maxidx(4,8,6) (max of columns 2 and 3 above)  
		 */
		int expectedPmaxAbsoluteSampleIdx = 2; 
		
		Matrix result = sut.concatenate(inputDataInterpretation);
		// assert the swallow ID at pos 0
		assertEquals(101, result.get(0, 0), 0.001);
		assertEquals(101, result.get(1, 0), 0.001);
		assertEquals(101, result.get(2, 0), 0.001);
		// sample id (first column in input data)
		assertEquals(1, result.get(0, 1), 0.001);
		assertEquals(2, result.get(1, 1), 0.001);
		assertEquals(9, result.get(2, 1), 0.001);
		// relative sample ID
		assertEquals(0, result.get(0, 2), 0.001);
		assertEquals(1, result.get(1, 2), 0.001);
		assertEquals(8, result.get(2, 2), 0.001);
		// restitution time sample at column 3 (const)
		assertEquals(inputRestitutionszeitSample, result.get(0, 3), 0.001);
		assertEquals(inputRestitutionszeitSample, result.get(1, 3), 0.001);
		assertEquals(inputRestitutionszeitSample, result.get(2, 3), 0.001);
		// pmax information at column 4 (const)
		assertEquals(expectedPmaxAbsoluteSampleIdx, result.get(0, 4), 0.001);
		assertEquals(expectedPmaxAbsoluteSampleIdx, result.get(1, 4), 0.001);
		assertEquals(expectedPmaxAbsoluteSampleIdx, result.get(2, 4), 0.001);
		
	}

}
