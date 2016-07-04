#!/bin/bash
# taken from http://it.toolbox.com/blogs/shell-functions/property-files-with-with-shell-functions-57410
# and adapted for the following
# * support for non-properties-lines and normal bash functions (lines without equals = sign). WARNING: Disambiguation is a crude heuristic!!! Everything without a equals sign is simply echoed. E.g., bash functions will not work!!!
# * added function sourcein() to include a source file (similar to . semantic): expands a given file (and its properties), sources it, and thereafter cleans up resources.
# * added echorpl function

readProp ()
{
    v_prop_name=$1;shift
    cat ${*:--} |
         grep "\${$v_prop_name}=" |
         sed "s/\${$v_prop_name}=\(.*\)/\1/"
}
readProp ()
{
    cat ${*:--} | awk -F= '
        function absorb( val, pattn, valv) {

             begnp = substr(val,1,index(val, pattn)-1)
             nextp = substr(val, 1+length(pattn))
             gsub( / /, "", nextp)
             return begnp valv nextp
        }
        function replace (val) {
            for( v in value)
                if (index(val, v)) {
                    gsub( / /, "", value[v])
                    return absorb( val, "${" v "}", value[v])
                }
             return val
        }
                                { if ( $2 == "" ) { print $0 } else {value[$1] = $2} }
        END {
              for( v in value) {
                printf "%s=%s\n", v, replace(value[v])
                }
             }
     '
}
#
# helper method echo read prop lib; appending a date to the log message
#
echorpl () {
        echo `date` $@
}


#
# function to include a properties file
#
# copies the given source properties file into a temporary file while expanding the properties variables
# sources this file, and deletes it.
#
# requires 2 arguments
# arg 1 -> unique identifier (used as a temporary file name)
# arg 2 -> properties file to include
#
#
sourcein() {
        local TMP_PROPERTIES=`whoami``date '+%s'`$1 # local required s.t. it does not get overridden in sourced files
        echorpl "Using temporary properties file ${TMP_PROPERTIES}"

        # define function to clean up temporary resouces ...

        echorpl "Expanding properties in file $2 to temporary file ${TMP_PROPERTIES} ..."
        readProp $2 > ${TMP_PROPERTIES}
        # re-enable this for debugging ...
        #cat ${TMP_PROPERTIES}

        echorpl "Expanded properties file has "`wc -l ${TMP_PROPERTIES} | awk ' {  print $1 } ' `" lines."

        echorpl "Including expanded properties file from ${TMP_PROPERTIES} ..."

        #source the tmp file
        . ${TMP_PROPERTIES}

        if [ ! $? == 0 ]; then
                echo2 "ERROR: Failed to source ${TMP_PROPERTIES}. Aborting execution of this script"
		exit 3
		fi

        echorpl "deleting ${TMP_PROPERTIES} ..."
        rm -rf ${TMP_PROPERTIES}
}
