package de.ismll.mhh.methods;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import de.ismll.bootstrap.BootstrapException;
import de.ismll.bootstrap.CommandLineParser;
import de.ismll.secondversion.ApplyMHHModel;
import de.ismll.secondversion.ApplyMHHModelImpl;
import de.ismll.stub.AbstractProxy;

public class MHHModelFactory extends AbstractProxy<ApplyMHHModel> {

	
	public static MHHModelFactory convert(Object in){
		MHHModelFactory ret = new MHHModelFactory();
		ApplyMHHModel mdl = null;
		
		if (in instanceof ApplyMHHModel)
			mdl = (ApplyMHHModel) in;
		
		if (in instanceof MHHModelFactory)
			mdl = ((MHHModelFactory) in).getTarget();
		
		URI source = (URI) CommandLineParser.convert(in, URI.class);
		
		switch (source.getScheme()) {
		case "file":
			File directory = new File(source);
			try {
				mdl = ApplyMHHModelImpl.fromDirectory(directory);
			} catch (IOException e) {
				throw new BootstrapException("Could not load model from directory " + directory, e);
			}
			
			break;		
		}
		
		ret.setTarget(mdl);
		return ret;
	}
	
}
