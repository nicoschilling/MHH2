package de.ismll.exceptions;

public class ModelApplicationException extends Exception{
	
	private int manual;
	private int max;
	private int min;
	
	public ModelApplicationException(String msg, int manual, int max, int min){
		super(msg);
		this.manual = manual;
		this.max = max;
		this.min = min;
		
	}

	public int getManual() {
		return manual;
	}

	public void setManual(int manual) {
		this.manual = manual;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

}
