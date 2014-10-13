#!/bin/bash

queues="fast.q,acogpr.q,all.q"

# enqueue hyperparameter search for normal swallows.
# job names will start with NormalOnNormal
#./hyperNormalOnNormal.sh

# submit script to determine best model for normal on normal. 
# wait for all jobs from above to complete before execute this.
qsub -l mem=1G -q ${queues} -N bestModelNormalOnNormal -hold_jid NormalOnNormal\*  ./bestModelNormal-on-Normal.sh

# predict on all normal test swallows
qsub -l mem=6G -q ${queues} -N predictNormalOnNormal -hold_jid bestModelNormalOnNormal  ./predictNormalOnNormal.sh

# create an excel table.
# the configuration in the script results in an output to file logs/evaluateNormalOnNormal.txt
qsub -l mem=1G -q ${queues} -N createExcelTable -hold_jid predictNormalOnNormal ./evaluateNormalOnNormalExcel.sh


