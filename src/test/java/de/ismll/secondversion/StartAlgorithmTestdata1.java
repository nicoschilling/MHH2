package de.ismll.secondversion;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class StartAlgorithmTestdata1 {

	private static final String SPLITDIR = "testdata1/";
	private static final String ANNOTATOR = "sm";
	private static final String ANNOTATIONDIR = "testdata1/";
	private static File resourcesDirectory;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	    resourcesDirectory = new File("src/test/resources");
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
	public void test() {
		StartAlgorithm.main(new String[] {
				 "splitFolder=" +  resourcesDirectory + File.separator  + SPLITDIR
				,"annotator=" + ANNOTATOR
				,"maxIterations=1"
				,"stepSize=0.001"
				,"lambda=0.1"
				,"windowExtent=75"
				,"columnSelector=33,166"
				,"laplacian=true"
				,"useValidation=true"
				,"descentDirection=logistic"
				,"modelFunction=lm"
				,"annotationBaseDir=" + resourcesDirectory + File.separator + ANNOTATIONDIR
				,"includeRD=true"
				//,"runLapTable=run_${u_experimentidentifier}"
				//,"iterTable=iter_${u_experimentidentifier}"
				,"smoothReg=0.001"
				,"smoothWindow=0.001"
		});
	}
	
	@Test
	public void testNegativeIterations() {
		StartAlgorithm.main(new String[] {
				 "splitFolder=" +  resourcesDirectory + File.separator  + SPLITDIR
				,"annotator=" + ANNOTATOR
				,"maxIterations=-1"
				,"stepSize=0.001"
				,"lambda=0.1"
				,"windowExtent=75"
				,"columnSelector=33,166"
				,"laplacian=true"
				,"useValidation=true"
				,"descentDirection=logistic"
				,"modelFunction=lm"
				,"annotationBaseDir=" + resourcesDirectory + File.separator + ANNOTATIONDIR
				,"includeRD=true"
				//,"runLapTable=run_${u_experimentidentifier}"
				//,"iterTable=iter_${u_experimentidentifier}"
				,"smoothReg=0.001"
				,"smoothWindow=0.001"
		});
	}
	
	@Test
	public void testNonLaplacian() {
		StartAlgorithm.main(new String[] {
				 "splitFolder=" +  resourcesDirectory + File.separator  + SPLITDIR
				,"annotator=" + ANNOTATOR
				,"maxIterations=-1"
				,"stepSize=0.001"
				,"lambda=0.1"
				,"windowExtent=75"
				,"columnSelector=33,166"
				,"laplacian=false"
				,"useValidation=true"
				,"descentDirection=logistic"
				,"modelFunction=lm"
				,"annotationBaseDir=" + resourcesDirectory + File.separator + ANNOTATIONDIR
				,"includeRD=true"
				//,"runLapTable=run_${u_experimentidentifier}"
				//,"iterTable=iter_${u_experimentidentifier}"
				,"smoothReg=0.001"
				,"smoothWindow=0.001"
		});
	}
	@Test
	public void testWithoutValidation() {
		StartAlgorithm.main(new String[] {
				 "splitFolder=" +  resourcesDirectory + File.separator  + SPLITDIR
				,"annotator=" + ANNOTATOR
				,"maxIterations=1"
				,"stepSize=0.001"
				,"lambda=0.1"
				,"windowExtent=75"
				,"columnSelector=33,166"
				,"laplacian=true"
				,"useValidation=false"
				,"descentDirection=logistic"
				,"modelFunction=lm"
				,"annotationBaseDir=" + resourcesDirectory + File.separator + ANNOTATIONDIR
				,"includeRD=true"
				//,"runLapTable=run_${u_experimentidentifier}"
				//,"iterTable=iter_${u_experimentidentifier}"
				,"smoothReg=0.001"
				,"smoothWindow=0.001"
		});
	}
	@Test
	public void testWithoutRd() {
		StartAlgorithm.main(new String[] {
				 "splitFolder=" +  resourcesDirectory + File.separator  + SPLITDIR
				,"annotator=" + ANNOTATOR
				,"maxIterations=1"
				,"stepSize=0.001"
				,"lambda=0.1"
				,"windowExtent=75"
				,"columnSelector=33,166"
				,"laplacian=true"
				,"useValidation=true"
				,"descentDirection=logistic"
				,"modelFunction=lm"
				,"annotationBaseDir=" + resourcesDirectory + File.separator + ANNOTATIONDIR
				,"includeRD=false"
				//,"runLapTable=run_${u_experimentidentifier}"
				//,"iterTable=iter_${u_experimentidentifier}"
				,"smoothReg=0.001"
				,"smoothWindow=0.001"
		});
	}
}
