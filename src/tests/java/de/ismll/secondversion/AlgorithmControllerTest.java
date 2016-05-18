package de.ismll.secondversion;

import static org.junit.Assert.*;
import static de.ismll.secondversion.DatasetFormat.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.ismll.table.Matrix;
import de.ismll.table.impl.DefaultMatrix;

public class AlgorithmControllerTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRun() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetPmax() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAnnotationDataInterpretation() {
		fail("Not yet implemented");
	}

	@Test
	public void testPreprocess() {
		fail("Not yet implemented");
	}

	@Test
	public void testPreprocessSwallow() {
		fail("Not yet implemented");
	}

	@Test
	public void testPreprocessTestSwallow() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetMax() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetTheMaxCurve() {
		fail("Not yet implemented");
	}

	@Test
	public void testConcatenateLoggerDataInterpretationIntBoolean() {
		fail("Not yet implemented");
	}

	@Test
	public void testConcatenateLoggerDataInterpretationIntBooleanInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testConcatenateForPmax() {
		fail("Not yet implemented");
	}

	@Test
	public void testNormalize() {
		fail("Not yet implemented");
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

	@Test
	public void testComputeSample2avgLabelIntMatrixArrayMatrixArray() {
		fail("Not yet implemented");
	}

	@Test
	public void testComputeSample2avgLabelIntMatrixMatrix() {
		fail("Not yet implemented");
	}

	@Test
	public void testNewComputeSample2avgLabel() {
		fail("Not yet implemented");
	}

	@Test
	public void testPredictAnnotationMatrixLogger() {
		fail("Not yet implemented");
	}

	@Test
	public void testPredictAnnotationMatrixLoggerInt() {
		fail("Not yet implemented");
	}

}
