package de.ismll.secondversion;

public enum Proportion {
	TRAIN("train"), VALIDATION("validation"), TEST("test");
	
	private String label;

	Proportion(String label){
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
}
