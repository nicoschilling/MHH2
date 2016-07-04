#
# lifecycle.sh
#
# defines functions for handling the lifecycle of an object; see docs/lifecycle.txt
#

# include common functions (rn_exists, logging)
. common.sh

# define stubs if it is not (yet) defined
fn_exists on_success || function on_success() { 
	echo "stubbed on_succes" 
}
fn_exists on_init || function on_init() { 
	echo "stubbed on_init" 
}
fn_exists on_abort || function on_abort() { 
	echo "stubbed on_abort" 
}
fn_exists on_exit || function on_exit() { 
	echo "stubbed on_exit" 
}

#
# main function, guarding the on_run method
#
# currently,  this mostly creates a simple lifecycle by calling callback methods.
#
function guarded_run() {
	local call_success=f
	# initialize calling on_init
	trace "calling on_init"
	on_init "$@"
	trace "on_init finished"
	on_init_ret=$?
#	echo "ret:" $on_init_ret
	if [ 'x'$on_init_ret = 'x' ]; then
		error "guarded_run:: invalid return value (null) from do_init method. Please return an appropriate return value (e.g., 0 on success)"
		return 1;
	else
		if [ $on_init_ret = 0 ]; then
			# call on_run
			trace "calling on_run"
			echo "starting at `date`"
			on_run "$@"
			retval=$?
			trace "on_run finished, returnvalue=$retval"

			echo "stopping at `date` with errorlevel $retval"
			if [ 'x'$retval = 'x' ]; then
				error "guarded_run:: invalid return value (null) from do_run method. Please return an appropriate return value (e.g., 0 on success)"
			else
				if [ $retval = 0 ]; then
					call_success=t		
                		fi
			fi
		fi
	fi

	if [ $call_success = 't' ]; then
		trace "calling on_success"
		on_success "$@"
		trace "on_success finished"
	else
		trace "calling on_abort"
		on_abort "$@"
		trace "on_abort finished"
	fi
	trace "calling on_exit"
	on_exit	 "$@"
	trace "on_exit finished"
}

