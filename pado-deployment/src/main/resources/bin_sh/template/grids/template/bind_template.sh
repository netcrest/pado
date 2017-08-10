# List all of the hosts that will be part of the grid cluster separated by spaces.  #
# [0] ServerHost is the address used by the management scripts. It is
#     typically same as ServerBindAddress.
# [1] ServerBindAddress is the address for the grid peers to connect.
# [2] ClientBindAddress is the address for the the clients to connect.
# [3] GatewayBindAddress is the address for the gateway clients to connect.
#
# The bind addresses may be required for multi-homed hosts that have more
# than one IP address. For ServerBindAddress, set the address that belongs
# to the subnet with the highest bandwidth to provide fast replication.
# For single-homed hosts, all of the addresses are the same IP address.
# Note that even for a grid that runs entirely in a single host, you
# must list the IP address (or localhost). 
#
# BindAddressArray elements:
#    array[0] ServerHost
#    array[1] ServerBindAddress
#    array[2] ClientBindAddress
#    array[3] GatewayBindAddress
#
# ServerHost         | ServerBindAddress  | ClientBindAddress  | GatewayBindAddress
# ------------------ | ------------------ | ------------------ | ------------------
  %%ServerHost%%   %%ServerBindAddress%%   %%ClientBindAddress%%  %%GatewayBindAddress%%
