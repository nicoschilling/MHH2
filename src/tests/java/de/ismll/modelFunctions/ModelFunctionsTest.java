package de.ismll.modelFunctions;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.ismll.secondversion.AlgorithmController;
import de.ismll.secondversion.DatasetFormat;
import de.ismll.secondversion.IntRange;
import de.ismll.secondversion.Quality;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.impl.DefaultVector;

public class ModelFunctionsTest {

	private final class MockModelFunction extends ModelFunctions {
		
		int useColumnAsLabel = -1;
		
		@Override
		public void initialize(AlgorithmController algcon) {
			
		}

		@Override
		public float evaluate(Vector instance) {
			if (useColumnAsLabel>=0)
				return instance.get(useColumnAsLabel);
			return super.evaluate(instance);
		}
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		mockModelFunction = new MockModelFunction();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testInitialize() {
		fail("Not yet implemented");
	}

	@Test
	public void testSaveBestParameters() {
		fail("Not yet implemented");
	}

	@Test
	public void testComputeMajorityClassAccuracyHigh() {
		assertEquals(
				0.75,  
				ModelFunctions.computeMajorityClassAccuracy(DefaultVector.wrap(new int[] {
							1,
							1,
							1,
							0
						}))
				, 0.01);
	}

	@Test
	public void testComputeMajorityClassAccuracyLow() {
		assertEquals(
				0.75,  
				ModelFunctions.computeMajorityClassAccuracy(DefaultVector.wrap(new int[] {
							1,
							0,
							0,
							0
						}))
				, 0.01);
	}

	@Test
	public void testEvaluateModel() {
		/*
		 * define that the 6.th column in the data is actually the label, see below for the column selector! 
		 */
		mockModelFunction.useColumnAsLabel=5;
		int pmaxSampleIdx = 101;
		
		Matrix[] swallowData = new Matrix[] {
				DefaultMatrix.wrap(new float[][] {
					{0,100,0,0,pmaxSampleIdx,/*data: */ 5, /*predicted ! label, see above: */ DatasetFormat.LABEL_NICHT_SCHLUCK },
					{1,101,1,0,pmaxSampleIdx,/*data: */ 2, /*predicted ! label, see above: */ DatasetFormat.LABEL_SCHLUCK },
					{2,102,2,0,pmaxSampleIdx,/*data: */ 3, /*predicted ! label, see above: */ DatasetFormat.LABEL_SCHLUCK },
					{3,103,3,0,pmaxSampleIdx,/*data: */ 4, /*predicted ! label, see above: */ DatasetFormat.LABEL_NICHT_SCHLUCK }
				})
		};
		Matrix[] samplewiseTrueLabels = new Matrix[] {
				DefaultMatrix.wrap(new float[][] {
					{0,/*label: */ DatasetFormat.LABEL_NICHT_SCHLUCK},
					{1,/*label: */ DatasetFormat.LABEL_SCHLUCK},
					{2,/*label: */ DatasetFormat.LABEL_NICHT_SCHLUCK},
					{3,/*label: */ DatasetFormat.LABEL_NICHT_SCHLUCK}
				})
		};
		int[] timebasedAnnotation = new int[] {
				2 /*at sample 1 is the human annotation; label transition between indizes 0 and 1 */ ,
		};
		
		int windowExtent=0;
		IntRange columnSelector = IntRange.convert("0,4;6,6");

		Quality evaluateModel = mockModelFunction.evaluateModel(
				swallowData, 
				samplewiseTrueLabels, 
				timebasedAnnotation, 
				windowExtent, 
				columnSelector);
		
		assertEquals("Accuracy is 75%", 0.75, evaluateModel.getAccuracy(), 0.0001f);
		/*
		 * derived sample diff is 1, as the prediction (see the data) shall derive the annotation at index 2.
		 */
		assertEquals("Sampledifference is 1", 1, evaluateModel.getSampleDifference(), 0.0001f);
		
	}
	
	@Test
	public void testEvaluateModelAllWrong() {
		/*
		 * define that the 6.th column in the data is actually the label, see below for the column selector! 
		 */
		mockModelFunction.useColumnAsLabel=5;
		final int pmaxSampleIdx = 102;
		
		Matrix[] data = new Matrix[] {
				DefaultMatrix.wrap(new float[][] {
					{0,100,0,0,pmaxSampleIdx,/*data: */ 5, /*predicted ! label, see above: */ DatasetFormat.LABEL_NICHT_SCHLUCK },
					{1,101,1,0,pmaxSampleIdx,/*data: */ 2, /*predicted ! label, see above: */ DatasetFormat.LABEL_NICHT_SCHLUCK },
					{2,102,2,0,pmaxSampleIdx,/*data: */ 3, /*predicted ! label, see above: */ DatasetFormat.LABEL_SCHLUCK },
					{3,103,3,0,pmaxSampleIdx,/*data: */ 4, /*predicted ! label, see above: */ DatasetFormat.LABEL_SCHLUCK }
				})
		};
		Matrix[] samplewiseTrueLabels = new Matrix[] {
				DefaultMatrix.wrap(new float[][] {
					{0,/*label: */ DatasetFormat.LABEL_SCHLUCK},
					{1,/*label: */ DatasetFormat.LABEL_SCHLUCK},
					{2,/*label: */ DatasetFormat.LABEL_NICHT_SCHLUCK},
					{3,/*label: */ DatasetFormat.LABEL_NICHT_SCHLUCK}
				})
		};
		int[] timebasedAnnotation = new int[] {
				2 /*at sample 2 is the human annotation; label transition between indizes 1 and 2 */ ,
		};
		
		int windowExtent=0;
		IntRange columnSelector = IntRange.convert("0,4;6,6");

		Quality evaluateModel = mockModelFunction.evaluateModel(
				data, 
				samplewiseTrueLabels, 
				timebasedAnnotation, 
				windowExtent, 
				columnSelector);
		
		assertEquals(0.0, evaluateModel.getAccuracy(), 0.0001f);
		/*
		 * derived sample diff is 1, as the prediction (see the data) shall derive the annotation at index 2.
		 */
		assertEquals("Sampledifference is 2 (at the end of the swallow)", 2, evaluateModel.getSampleDifference(), 0.0001f);
		
	}

	@Test
	public void testConvertLm() {
		ModelFunctions convert = ModelFunctions.convert("lm");
		assertTrue(convert instanceof LinearRegressionPrediction);
	}

	@Test
	public void testConvertFm() {
		ModelFunctions convert = ModelFunctions.convert("fm");
		assertTrue(convert instanceof FmModel);
	}
	
	@Test
	public void testConvertNonsense() {
		ModelFunctions convert = ModelFunctions.convert("nonsene");
		assertNull(convert);
	}

	@Test
	public void testPredictAsClassification() {
		assertArrayEquals(new float[] {
				1,
				1,
				-1,
				-1
		}, mockModelFunction.predictAsClassification(new float[] {
				0.001f,
				10,
				-0.001f,
				-10
		}), 0.01f);
		
	}

	private MockModelFunction mockModelFunction;

}
