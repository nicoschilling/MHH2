#
# traputils.sh
#
# defines traps and handlers for capturing various events on bash
#

#
# TODO: consider adding SIGTRAP SIGUSR1 SIGUSR2
#

# include definitions from common.sh (logging, fn_exists)
. common.sh

#
# default implementation of SIGHUP signal
#
function sighandler_trap_SIGHUP(){
	warn "SIGHUP"
}

#
# default implementation of SIGINT signal
#
function sighandler_trap_SIGINT(){
	warn "SIGINT"
}

#
# default implementation of SIGQUIT signal
#
function sighandler_trap_SIGQUIT(){
	warn "SIGQUIT"
}

#
# default implementation of SIGABRT signal
#
function sighandler_trap_SIGABRT(){
	warn "SIGABRT"
}

#
# default implementation of SIGKILL signal
#
function sighandler_trap_SIGKILL(){
	warn "SIGKILL"
}

#
# default implementation of SIGALRM signal
#
function sighandler_trap_SIGALRM(){
	warn "SIGALRM"
}

#
# default implementation of SIGTERM signal
#
function sighandler_trap_SIGTERM(){
	warn "SIGTERM"
}

# register trap handler
trap 'sighandler_trap_SIGHUP' SIGHUP
trap 'sighandler_trap_SIGINT' SIGINT
trap 'sighandler_trap_SIGQUIT' SIGQUIT
trap 'sighandler_trap_SIGABRT' SIGABRT
trap 'sighandler_trap_SIGKILL' SIGKILL
trap 'sighandler_trap_SIGALRM' SIGALRM
trap 'sighandler_trap_SIGTERM' SIGTERM

