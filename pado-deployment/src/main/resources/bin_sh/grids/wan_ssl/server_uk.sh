# List server specifics.
#
# [0] ServerNumber must be a unique number that identifies the
#     server. It should be sequentially incremented starting
#     from 1. Allowed values are [1-99], inclusive.
# [1] IsGateway 'true' to run the server as a gateway server.
# [2] ServerHost must match the ServerHost set in bind_<site>.sh.
# [3] ServerPort is the cache server port number that the underlying
#     data grid product opens for clients to connect.
# [4] DebugPort is the Java debugging port to which a debugger
#     such as Eclipse can remotely conntect.
#     to the grid.
# [5] JmxPort is the JMX port that the server opens for JMX tools
#     to connect.
#
# ServerArray elements:
#    array[0] ServerNumber
#    array[1] ServerHost
#    array[2] ServerPort
#    array[3] DebugPort
#    array[4] JmxPort
#    array[5] IsGateWay
#
# Server |                    | Server| Debug | Jmx   | Is
# Number | ServerHost         | Port  | Port  | Port  | Gateway
# ------ | ------------------ | ----- | ----- | ----- | -------
   1       localhost            20101   10101   30101   true
   2       localhost            20102   10102   30102   false
