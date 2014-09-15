#!/bin/bash
#$ -cwd
#$ -S /bin/bash
#$ -j n
#$ -o logs/output.$JOB_NAME.$TASK_ID.txt
#$ -e logs/output.$JOB_NAME.$TASK_ID.err
#$ -p -100
#$ -m a
#$ -M busche@ismll.de
#$ -R y

database=buschemhh
dbuser=busche

for p in 1 ; do # probanden
for s in `seq 1 1`; do #split numbers 1 ... 5

baseDir="/acogpr/mhh/models-battery/AllSwallows/Proband${p}/split-${s}"
modelfileSampleDiff=${baseDir}/"bestSampleDiff/" # or any absolute path...
modelfileAccuracy=${baseDir}/"bestAcc/" # or any absolute path...
mkdir -p ${modelfileAccuracy}
mkdir -p ${modelfileSampleDiff}
splitfolder="/acogpr/mhh/Splits/AllSwallows/Proband${p}/split-${s}"

tablename="normal_on_normal_test"

echo "determining best model for Proband ${p} on Split ${s} (table identifier: ${tablename}) ..."
echo "    ... in folder ${splitfolder} ..."
echo "    ... writing files (based on best accuracy)    to ${modelfileAccuracy}"
echo "    ... writing files (based on best sample diff) to ${modelfileSampleDiff}"

for c in model_parameters window_extent; do
queryAccuracy="select
/*r.split, 
i.accuracy,*/
${c}
from
run_${tablename} r
join
iter_${tablename} i
on (r._id=i.run_id)
where r.split = '${splitfolder}'
order by 
accuracy desc
limit 1"

querySampleDiff="select
/*r.split,
i.accuracy,*/
${c}
from
run_${tablename} r
join
iter_${tablename} i
on (r._id=i.run_id)
where r.split = '${splitfolder}'
order by
sample_difference asc
limit 1"


#echo $queryAccuracy
# uggh! we do not have psql on the nodes nor the head node.
ssh acogpr -C 'psql -A -t -d '${database}' -U '${dbuser}' -c "'${queryAccuracy}'" -o "'${modelfileAccuracy}/${c}'"'
ssh acogpr -C 'psql -A -t -d '${database}' -U '${dbuser}' -c "'${querySampleDiff}'" -o "'${modelfileSampleDiff}/${c}'"'
#echo $parameters >p${p}

done # of c column
done # of splits
done # of probanden



