package de.ismll.modelFunctions;

import de.ismll.table.Matrix;
import de.ismll.table.Vector;

public class ModelParameters {
	
	private float bias;
	private Vector parameters;
	
	private Vector fm_W;
	private Matrix fm_V;
	
	public ModelParameters() {
		
	}
	
	

	public float getBias() {
		return bias;
	}

	public void setBias(float bias) {
		this.bias = bias;
	}

	public Vector getFm_W() {
		return fm_W;
	}

	public void setFm_W(Vector fm_W) {
		this.fm_W = fm_W;
	}

	public Matrix getFm_V() {
		return fm_V;
	}

	public void setFm_V(Matrix fm_V) {
		this.fm_V = fm_V;
	}

}
