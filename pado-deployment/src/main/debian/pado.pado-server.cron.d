# Cron jobs required by pado

SHELL=/bin/sh
PATH=/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin
PADO_HOME=/apps/adf/pado/prod-iot

# m h dom mon dow user  command
* * * * *   root    (cd $PADO_HOME/bin_sh; ./start_server_local -num auto -clean) >/dev/null 2>&1

# An empty line is required. Do not remove the empty line below.

