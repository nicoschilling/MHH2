#
# common.sh
#
# common functionalitites of evere which are used in all component scripts.
#
# functions defined here:
#  -fn_exists


#
# utility function for checking function existence
#
# taken from http://stackoverflow.com/questions/85880/determine-if-a-function-exists-in-bash
#
function fn_exists()
{
	type $1 2>/dev/null | grep -q 'function'
}


# logging methods
fn_exists trace || function trace () {
	echo `date +%Y%m%d-%R:%S`" [TRACE] "$*
}
fn_exists info2 || function info2() {
	echo `date +%Y%m%d-%R:%S`" [INFO ] "$*
}
fn_exists warn || function warn() {
	echo `date +%Y%m%d-%R:%S`" [WARN ] "$*
}
fn_exists error || function error() {
	echo `date +%Y%m%d-%R:%S`" [ERROR] "$*
}





						
