#!/bin/bash
#
#$ -cwd
#$ -S /bin/bash
#$ -j n
#$ -o logs/output.$JOB_NAME.$TASK_ID.txt
#$ -e logs/output.$JOB_NAME.$TASK_ID.err
#$ -p 0
#$ -m a
#$ -M schilling@ismll.de
#$ -R y

# defines loogging messages,  etc.
echo "including common function definitions"
. common.sh

if [ -f lifecycleutils.sh ]; then
	info2 "including lifecycleutils.sh component ..."
	. lifecycleutils.sh
else
	# make a stub of the guarded_run function which would be defined in the traputils component.
	function guarded_run() {
		on_run "$@"
	}
fi

# include java defintions
info2 "including java target definitions"
. target_java.sh

# this function runs the java program.
function on_run() {

	info2 meta.hostname=$HOSTNAME
	trace meta.date.start=`date`
	trace meta.numslots=$NSLOTS

	# use the defined function to call the java executable with these parameters
	run_java de.ismll.console.Generic "$@"

	trace meta.date.end=`date`

} #of function run()

guarded_run "$@"
