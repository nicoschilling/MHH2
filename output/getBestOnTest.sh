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
                    
#mkdir -p ${modelfileAccuracy}
#mkdir -p ${modelfileSampleDiff}


tablename=${u_experimentidentifier}_${modelFunction}_test

#echo ${tablename}

#exit 0

#echo "determining best model (table identifier: ${tablename}) ..."
#echo "    ... in folder ${u_splitfolder} ..."
#echo "    ... writing hyperparameters (based on best accuracy)    to ${modelfileAccuracy}"
#echo "    ... writing hyperparameters (based on best sample diff) to ${modelfileSampleDiff}"


queryAccuracy="select
accuracy
FROM
iter_${tablename} i
JOIN
run_${tablename} r
ON
(r._id=i.run_id)
WHERE
r.split ='${u_splitfolder}'
AND
i.iteration_nr=1999
ORDER BY
accuracy
DESC
limit 1
"


querySampleDiff="select                                                                                                                                                           
sample_difference
FROM
iter_${tablename} i
JOIN
run_${tablename} r
ON
(r._id=i.run_id)
WHERE
r.split ='${u_splitfolder}'
AND
i.iteration_nr=1999
ORDER BY
sample_difference
ASC
limit 1
" 




#echo "${queryAccuracy}"
#queryAccuracy= `echo "${queryAccuracy2}" `
#echo "$querySampleDiff"

# uggh! we do not have psql on the nodes nor the head node.
accuracy=`ssh acogpr -C 'psql -A -t -d '${database}' -U '${dbuser}' -c "'${queryAccuracy}'"'`  #-o "'${modelfileAccuracy}/parameters'"'
sample_difference=`ssh acogpr -C 'psql -A -t -d '${database}' -U '${dbuser}' -c "'${querySampleDiff}'" '` # -o "'${modelfileSampleDiff}/parameters'"'

echo ${accuracy} >> Proband${patient}_accuracy_${modelFunction}
echo ${sample_difference} >> Proband${patient}_sd_${modelFunction}


#done # of c column

done
done

