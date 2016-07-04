#!/bin/bash -e

echo "test"

. readProplib.sh 2>/dev/null
if [ ! $? == 0 ]; then
        echo2 "ERROR: Failed to source readProplib2.sh. Does it exist? Aborting execution of this script"
        exit 1
fi

if [ 'x'$1 = 'x' ]; then
        echo2 "ERROR: no source properties file specified. Need at least one parameter (the properties file to be used) ..."
        exit 2
fi

sourcein 'main' $1

echo target_dir ${target_dir}
echo specific_dir ${specific_dir}