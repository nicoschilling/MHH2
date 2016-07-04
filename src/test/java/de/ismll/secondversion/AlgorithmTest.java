package de.ismll.secondversion;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.ismll.table.impl.DefaultMatrix;
import de.ismll.modelFunctions.LinearRegressionPrediction;
import de.ismll.table.Matrix;

public class AlgorithmTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private Algorithm sut;
	
	@Before
	public void setUp() throws Exception {
		sut = new Algorithm();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRun() {
		LinearRegressionPrediction model = new LinearRegressionPrediction();

		sut.setUseValidation(true);
		sut.setRawData(fakeRawData());
		sut.setColumnSelector(IntRange.convert("1,2"));
		sut.setModelFunction(model);
//		sut.run();
	}

	private MhhRawData fakeRawData() {
		MhhRawData ret = new MhhRawData();
		ret.trainData = new Matrix[] {
				DefaultMatrix.wrap(new float[][] {
					new float[] {1,2,2,1},
					new float[] {2,4,4,2},
				})
		};
		ret.trainDataLabels = new Matrix[] {
				DefaultMatrix.wrap(new float[][] {
					new float[] {DatasetFormat.LABEL_NICHT_SCHLUCK},
					new float[] {DatasetFormat.LABEL_SCHLUCK},
				})
		};
		ret.trainDataRelativeAnnotations = new int[] {
				1,0
		};
		
		ret.validationData = new Matrix[] {
				DefaultMatrix.wrap(new float[][] {
					new float[] {1,2,2,1},
					new float[] {2,4,4,2},
				})
		};
		ret.validationDataLabels = new Matrix[] {
				DefaultMatrix.wrap(new float[][] {
					new float[] {DatasetFormat.LABEL_NICHT_SCHLUCK},
					new float[] {DatasetFormat.LABEL_SCHLUCK},
				})
		};
		ret.validationDataRelativeAnnotations = new int[] {
				1,0
		};
		
		return ret;
	}
}
