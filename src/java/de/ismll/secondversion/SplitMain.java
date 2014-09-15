package de.ismll.secondversion;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import weka.core.PropertyPath.Path;

public class SplitMain {

	public static void main(String[] args) {
		IntraSplits is = new IntraSplits();
		is.run();
		
	}

}
