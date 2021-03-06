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



for patient in ${patients[@]}; do
for split in ${splits[@]}; do

splitIdentifier=Proband${patient}/split${split}

modelfileSampleDiff=${baseDir}/${splitIdentifier}/"bestSampleDiff" # or any absolute path...                                                                                     
modelfileAccuracy=${baseDir}/${splitIdentifier}/"bestAcc" # or any absolute path...                                                                                              
u_splitfolder=${usesplitsdir}/${splitIdentifier}
                    
mkdir -p ${modelfileAccuracy}
mkdir -p ${modelfileSampleDiff}


tablename=${u_experimentidentifier}_${modelFunction}

#echo ${tablename}

#exit 0

echo "determining best model (table identifier: ${tablename}) ..."
echo "    ... in folder ${u_splitfolder} ..."
echo "    ... writing hyperparameters (based on best accuracy)    to ${modelfileAccuracy}"
echo "    ... writing hyperparameters (based on best sample diff) to ${modelfileSampleDiff}"


queryAccuracy="select
r.step_size,
r.reg0,
r.regw,
r.regv,
r.nrlatentfeatures,
r.window_extent,
r.smoothreg,
r.smoothwindow 
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
r.step_size,
r.reg0,
r.regw,
r.regv,
r.nrlatentfeatures,
r.window_extent,
r.smoothreg,
r.smoothwindow
from
run_${tablename} r
join
iter_${tablename} i
on (r._id=i.run_id)
where r.split = '${u_splitfolder}'
order by
sample_difference asc
limit 1"

#echo "${queryAccuracy2}"
#queryAccuracy= `echo "${queryAccuracy2}" `
#echo "$querySampleDiff"

# uggh! we do not have psql on the nodes nor the head node.
ssh acogpr -C 'psql -A -t -d '${database}' -U '${dbuser}' -c "'${queryAccuracy}'" -o "'${modelfileAccuracy}/parameters'"'
ssh acogpr -C 'psql -A -t -d '${database}' -U '${dbuser}' -c "'${querySampleDiff}'" -o "'${modelfileSampleDiff}/parameters'"'



#done # of c column

done
done

