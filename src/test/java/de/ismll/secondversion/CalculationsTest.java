package de.ismll.secondversion;

import static org.junit.Assert.*;

import org.junit.Test;

public class CalculationsTest {

	@Test
	public void testComputeSigmoid___1() {
		assertEquals(0.268, Calculations.computeSigmoid(-1), 0.001);
	}

	@Test
	public void testComputeSigmoid_0() {
		assertEquals(0.5, Calculations.computeSigmoid(0), 0.001);
	}

	@Test
	public void testComputeSigmoid_1() {
		assertEquals(0.731, Calculations.computeSigmoid(1), 0.001);
	}

}
