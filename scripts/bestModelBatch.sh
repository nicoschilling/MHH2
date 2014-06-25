#!/bin/bash
#$ -cwd
#$ -S /bin/bash
#$ -j n
#$ -o logs/output.$JOB_NAME.$TASK_ID.txt
#$ -e logs/output.$JOB_NAME.$TASK_ID.err
#$ -p -100
#$ -m a
#$ -M schilling@ismll.de
#$ -R y

# source the properties in
. $1

u_experimentidentifier=${experimentidentifier?Error: no experimentidentifier defined!}

database=schilling
dbuser=schilling

baseDir=${usemodelsdir}
modelfileSampleDiff=${baseDir}/"bestSampleDiff/" # or any absolute path...
modelfileAccuracy=${baseDir}/"bestAcc/" # or any absolute path...
mkdir -p ${modelfileAccuracy}
mkdir -p ${modelfileSampleDiff}
u_splitfolder=${usesplitsdir}

tablename=${u_experimentidentifier}

echo "determining best model (table identifier: ${tablename}) ..."
echo "    ... in folder ${u_splitfolder} ..."
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
where r.split = '${u_splitfolder}'
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
where r.split = '${u_splitfolder}'
order by
sample_difference asc
limit 1"


#echo $queryAccuracy
# uggh! we do not have psql on the nodes nor the head node.
ssh acogpr -C 'psql -A -t -d '${database}' -U '${dbuser}' -c "'${queryAccuracy}'" -o "'${modelfileAccuracy}/${c}'"'
ssh acogpr -C 'psql -A -t -d '${database}' -U '${dbuser}' -c "'${querySampleDiff}'" -o "'${modelfileSampleDiff}/${c}'"'

done # of c column



