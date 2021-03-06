#!/bin/bash

#
#$ -M schilling@ismll.de
#$ -q all.q,acogpr.q
#$ -cwd
#$ -S /bin/bash
#$ -j n
#$ -o logs/output.$JOB_NAME.txt
#$ -e logs/output.$JOB_NAME.err
#$ -p 0
#$ -R y
#$ -l mem=4000M
### $ -l h_rt=05:00:00


#export JAVA_HOME=/usr/java/latest
#export JAVA_HOME=$JAVA_DIR
export JAVA_OPTS=$JAVA_OPTS"  -Xmx2500M -Dlog4j.configuration=log4j.properties"
#export JAVA_OPTS=$JAVA_OPTS" -Xms2000M -Xss8M -Xmn1500M -Xmx2500M -Dlog4j.configuration=log4j.properties"


if [ $JAVA_HOME'w' = 'w' ]; then
	export JAVA_HOME=/usr/java/latest
fi

cp="."
for f in `ls *.jar -1`; do
        cp=${cp}":"${f}
done

slots=1
 

echo meta.cp=${cp}
echo meta.date.start=`date`
java -XX:ParallelGCThreads=${slots} -XX:ConcGCThreads=${slots} -classpath $cp $JAVA_OPTS de.ismll.console.Generic "$@"
##### -XX:ParallelGCThreads=${slots} -XX:+UseG1GC -XX:+ScavengeBeforeFullGC -XX:ConcGCThreads=${slots}
echo $?
echo meta.date.end=`date`
