package de.ismll.lossFunctions;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import gnu.trove.map.hash.TIntFloatHashMap;
import de.ismll.modelFunctions.ModelFunctions;
import de.ismll.table.Vector;

public class LapSquaredLoss extends SquaredLoss {
	
	private Logger log = LogManager.getLogger(getClass());
	
	@Override
	public void iterate(ModelFunctions function, Vector instance, float label) {
		log.fatal("Laplacian Loss does not work solely on one instance!");
	}

	@Override
	public void iterate(ModelFunctions function, TIntFloatHashMap instance, float label) {
		log.fatal("Laplacian Loss does not work solely on one instance!");
	}

}
