package de.ismll.secondversion;

@SuppressWarnings("unused")
public class Quality {
	
	private float accuracy;
	private float sampleDifference;
	private float overshootPercentage;
	private float rss;
	
	
	public float getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}
	public float getSampleDifference() {
		return sampleDifference;
	}
	public void setSampleDifference(float sampleDifference) {
		this.sampleDifference = sampleDifference;
	}
	public float getOvershootPercentage() {
		return overshootPercentage;
	}
	public void setOvershootPercentage(float overshootPercentage) {
		this.overshootPercentage = overshootPercentage;
	}
	public float getRss() {
		return rss;
	}
	public void setRss(float rss) {
		this.rss = rss;
	}
	
	

}
