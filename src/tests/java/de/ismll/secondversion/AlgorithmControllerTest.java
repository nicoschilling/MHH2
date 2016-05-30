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

public class AlgorithmControllerTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private AlgorithmController sut;
	
	@Before
	public void setUp() throws Exception {
		sut = new AlgorithmController();
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
	public void testGetTheMaxCurve() {
		Matrix inputData = DefaultMatrix.wrap(new float[][] {
			{0,1,3},
			{0,2,5},
			{0,6,3}
		});
		float[] theMaxCurve = AlgorithmController.getTheMaxCurve(inputData);
		assertArrayEquals(new float[] {
				3,
				5,
				6
				}, 
				theMaxCurve, 
				0.001f);
		
	}


	@Test
	public void testConcatenateLoggerDataInterpretationIntBooleanIntFail1() {
		Logger inputLogger = Logger.getLogger(getClass());
		DataInterpretation inputDataInterpretation = new DataInterpretation();
		inputDataInterpretation.setChannelstart("P1");
		inputDataInterpretation.setChannelend("P3");
		inputDataInterpretation.setFirstSample(2);
		inputDataInterpretation.setLastSample(3);
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
		
		int inputRestitutionszeitSample=0;
		boolean inputNormalize = false;
		int inputPmaxSample = 1;
		
		try {
			Matrix result = sut.concatenate(inputLogger,inputDataInterpretation, inputRestitutionszeitSample, inputNormalize, inputPmaxSample);
			fail("Shall throw an exception, because 1 (inputPmaxSample) is smaller than the first sample");
		} catch (ModelApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	
		
	}

	@Test
	public void testConcatenateLoggerDataInterpretationIntBooleanInt() throws ModelApplicationException {
		Logger inputLogger = Logger.getLogger(getClass());
		DataInterpretation inputDataInterpretation = new DataInterpretation();
		inputDataInterpretation.setChannelstart("2");
		inputDataInterpretation.setChannelend("3");
		inputDataInterpretation.setFirstSample(0);
		inputDataInterpretation.setLastSample(2);
		inputDataInterpretation.setAcid_level("");
		inputDataInterpretation.setSwallowId(101);
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
		
		int inputRestitutionszeitSample=10;
		boolean inputNormalize = true;
		/*
		 * means: compute the sample; idx of max(P1:P3) from druckData
		 * 
		 * calculation will lead to inputPmaxSample==2, because 2 is the value of the first column in the data, whose idx is maxidx(4,8,6) (max of columns 2 and 3 above)  
		 */
		int inputPmaxSample = -1; 
		int expectedPmaxAbsoluteSampleIdx = 2; 
		
		Matrix result = sut.concatenate(inputLogger,inputDataInterpretation, inputRestitutionszeitSample, inputNormalize, inputPmaxSample);
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

	

	@Test
	public void testCreateSample2Labels() {
		Matrix input = new DefaultMatrix(2, META_COLUMNS.length+3);
		input.set(0, COL_SWALLOW_IDX			, 0);
		input.set(0, COL_ABS_SAMPLE_IDX			, 100);
		input.set(0, COL_REL_SAMPLE_IDX			, 0);
		input.set(0, COL_ANNOTATION_SAMPLE_IDX	, 0);
		input.set(0, COL_PMAX_SAMPLE_IDX		, 0);
		input.set(1, COL_SWALLOW_IDX			, 0);
		input.set(1, COL_ABS_SAMPLE_IDX			, 100);
		input.set(1, COL_REL_SAMPLE_IDX			, 0);
		input.set(1, COL_ANNOTATION_SAMPLE_IDX	, 0);
		input.set(1, COL_PMAX_SAMPLE_IDX		, 0);
		
		Matrix[] createSample2Labels = AlgorithmController.createSample2Labels(input);
		assertEquals(2, createSample2Labels.length);
		
		Matrix predictedLabels	= createSample2Labels[0];
		assertEquals(2, predictedLabels.getNumRows());
		assertEquals(SAMPLE_2_LABELS_META_COLUMNS.length, predictedLabels.getNumColumns());
		
		Matrix averageLabels	= createSample2Labels[1];
		assertEquals(2, averageLabels.getNumRows());
		assertEquals(SAMPLE_2_LABELS_META_COLUMNS.length, averageLabels.getNumColumns());
		
		
	}


}
