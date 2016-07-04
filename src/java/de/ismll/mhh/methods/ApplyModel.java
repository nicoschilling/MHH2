package de.ismll.mhh.methods;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import de.ismll.bootstrap.Parameter;
import de.ismll.exceptions.ModelApplicationException;
import de.ismll.mhh.io.DataInterpretation;
import de.ismll.secondversion.AnalysisResult;
import de.ismll.storage.StorageException;
import de.ismll.storage.StorageTargetFactory;

public class ApplyModel implements Runnable {

	@Parameter(cmdline = "inputfolder")
	private DataInterpretation di;

	@Parameter(cmdline = "model")
	private MHHModelFactory modelFactory;

	protected Logger log = LogManager.getLogger(getClass());

	private AnalysisResult predict;

	@Parameter(cmdline="target")
	private	StorageTargetFactory storageTargetFactory;
	
	@Override
	public void run() {
		
	
		try {
			predict = modelFactory.getTarget().predict(di);
		} catch (ModelApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		try {
			getStorageTargetFactory().getTarget().store(predict);
		} catch (StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// store predict

	}

	public MHHModelFactory getModelFactory() {
		return modelFactory;
	}

	public void setModelFactory(MHHModelFactory modelFactory) {
		this.modelFactory = modelFactory;
	}

	public DataInterpretation getDi() {
		return di;
	}

	public void setDi(DataInterpretation di) {
		this.di = di;
	}

	public AnalysisResult getPredict() {
		return predict;
	}

	public void setPredict(AnalysisResult predict) {
		this.predict = predict;
	}

	public StorageTargetFactory getStorageTargetFactory() {
		return storageTargetFactory;
	}

	public void setStorageTargetFactory(StorageTargetFactory storageTargetFactory) {
		this.storageTargetFactory = storageTargetFactory;
	}

}
