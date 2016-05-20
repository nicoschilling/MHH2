package de.ismll.secondversion;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.ismll.table.Vector;
import de.ismll.table.impl.DefaultVector;

public class ApplyMHHModelImplTest {

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
	public void testCountChanges0() {
		Vector input = DefaultVector.wrap(new int[] {
				1,
				1
		});
		
		int countChanges = ApplyMHHModelImpl.countChanges(input);
		assertEquals(0, countChanges);
	}

	@Test
	public void testCountChanges1() {
		Vector input = DefaultVector.wrap(new int[] {
				1,
				-1
		});
		
		int countChanges = ApplyMHHModelImpl.countChanges(input);
		assertEquals(1, countChanges);
	}

	@Test
	public void testCountChanges3() {
		Vector input = DefaultVector.wrap(new int[] {
				1,
				-1,
				5,
				-909
		});
		
		int countChanges = ApplyMHHModelImpl.countChanges(input);
		assertEquals(3, countChanges);
	}

}
