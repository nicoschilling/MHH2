
#for p in 1 ; do # probandena
for s in `seq 1 8`; do #split numbers 1 ... 5

baseDir="/data/mhh/models/Normal-on-Acid-Inter-Experiment/split-${s}"
modelfileSampleDiff=${baseDir}/"bestSampleDiff/" # or any absolute path...
modelfileAccuracy=${baseDir}/"bestAcc/" # or any absolute path...
mkdir -p ${modelfileAccuracy}
mkdir -p ${modelfileSampleDiff}
splitfolder="/acogpr/mhh/Splits/Normal-on-Acid-Inter-Experiment/split-${s}"

tablename="normal_on_acid"

echo "determining best model on Split ${s} (table identifier: ${tablename}) ..."
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


psql -A -t -d schilling -U busche -c "${queryAccuracy}" -o "${modelfileAccuracy}/${c}"
psql -A -t -d schilling -U busche -c "${querySampleDiff}" -o "${modelfileSampleDiff}/${c}"

done # of c column
done # of splits
#done # of probanden



