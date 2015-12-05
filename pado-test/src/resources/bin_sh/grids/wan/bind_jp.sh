# List all of the hosts that will be part of the grid cluster separated by spaces.
#
# 1. ServerHost must match the hosts set by $SERVER_HOSTS in ${SITE}_env.sh.
# 2. ServerBindAddress must be the address that the grid peers use to communicate.
# 3. ClientBindAddress must be the address the the clients would use to connect
#    to the grid.
#
# The bind addresses may be required for multi-homed hosts that have more
# than one IP address. For ServerBindAddress, set the address that belongs
# to the subnet with the highest bandwidth to provide fast replication.
# For single-homed hosts, all of the addresses are the same IP address.
# Note that even for a grid that runs entirely in a single host, you
# must list the IP address (or localhost). 
#
# ServerHost must start at each line without leading white characters, i.e., 
# spaces, tabs, etc.
#
# ServerHost | ServerBindAddress | ClientBindAddress
# --------------------------------------------------
localhost localhost localhost
