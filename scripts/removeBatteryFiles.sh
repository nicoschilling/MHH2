#!/bin/bash

basefolder=/acogpr/mhh
database=buschemhh

if [ $# -lt 1 ]; then
	echo READ THE SCRIPT before executing it!!!
	exit 1
fi

echo "deleting ${basefolder}/Results-battery/ ..."
rm -rf  ${basefolder}/Results-battery/

echo "deleting ${basefolder}/models-battery/ ..."
rm -rf ${basefolder}/models-battery/

echo "deleting all database tables in database ${database} ..."
for table in `ssh acogpr -C 'psql -t -d '${database}' -c "SELECT tablename FROM pg_catalog.pg_tables where schemaname='\'public\''"'`; do ssh acogpr -C 'psql -d '${database}' -c "DROP TABLE '$table'"' ;done


