package de.ismll.lossFunctions;

import de.ismll.table.IntVector;
import de.ismll.table.Matrix;
import de.ismll.table.Vector;
import de.ismll.table.impl.DefaultMatrix;
import de.ismll.table.impl.DefaultVector;
import de.ismll.table.projections.IntVectorView;
import de.ismll.table.projections.RowSubsetMatrixView;

public class Testing {

	public static void main(String[] args) {
		
		Matrix test = new DefaultMatrix(3, 3);
		
		test.set(0, 0, 1);
		test.set(0, 1, 2);
		
		test.set(0,2,1000);
		
		
		test.set(1,0,3);
		test.set(1, 1, 4);
		
		test.set(1, 2, 2000);
		
		test.set(2,0,5);
		test.set(2,1,6);
		
		test.set(2, 2, 3000);
		
		
		Vector pointers =  new DefaultVector(2);
		
		pointers.set(0, 0);
		pointers.set(1, 2);
		
		
		IntVector pointers1	 = new IntVectorView(pointers);
		
		Matrix current = new RowSubsetMatrixView(test, pointers1);
		
		
		System.out.println("bluuuh");

	}

}
