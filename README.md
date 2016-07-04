

Dependency Note
===============

This project requires the native library MyFM-1.0.jar from ISMLL for some parts of the implementation. This library is not yet contained in any public repository.
During build, it is assumed to be located at /tmp.


Usage
=====

The project uses the Gradle build system

Call 

	gradle run -Pexec.args="de.ismll.console.Generic de.ismll.console.Generic --help"

to call the implementation directly. Alternatively, cd into src/dist/scripts/ and use the run.sh / any other script therein,  e.g.

	./run.sh de.ismll.Test
	./gridSearch.sh ../configurations/bcs.properties ../configurations/test.regression.properties

In order to create a distribution package, call

	gradle assembleDist

and copy the file build/distributions/mhh2-X.Y.zip to any distribution location


