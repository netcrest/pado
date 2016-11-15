# List server specifics.
#
# [0]  ServerNumber must be a unique number that identifies the
#      server. It should be sequentially incremented starting
#      from 1. Allowed values are [1-99], inclusive.
# [1]  ServerHost must match the ServerHost set in bind_<site>.sh.
# [2]  ServerPort is the cache server port number that the underlying
#      data grid product opens for clients to connect.
# [3]  DebugPort is the Java debugging port to which a debugger
#      such as Eclipse can remotely conntect.
#      to the grid.
# [4]  JmxPort is the JMX port that the server opens for JMX tools
#      to connect.
# [5]  ServerProperties specifies the server properties file name. Must be
#      in $ETC_GRID_DIR. Default: server.xml
# [6]  ServerXml specifies the server XML file name. Must be in $ETC_GRID_DIR.
#      Default: server.properties
# [7]  PadoProperties specifies the Pado properties file name. Must be in
#      $ETC_GRID_DIR. Default: pado.properties
# [8]  PadoXml specifies the Pado XML file name. Must be in $ETC_GRID_DIR.
#      Default: pado.xml
# [9]  IsStart 'true' to start via start_site, 'false' to start via start_server.
#      Note that stop_site stops all servers regardless of this setting.
#      Default: true
#
# ServerArray elements:
#    array[0] ServerNumber      Required
#    array[1] ServerHost        Required
#    array[2] ServerPort        Required
#    array[3] DebugPort         Required
#    array[4] JmxPort           Required
#    array[5] ServerProperties  Optional Default: server.properties
#    array[6] ServerXml         Optional Default: server.xml
#    array[7] PadoProperties    Optional Default: pado.properties
#    array[8] PadoXml           Optional Default: pado.xml
#    array[9] IsServerStart     Optional Default: true
#
# Server |                    | Server| Debug | Jmx   | Server            | Server            | Pado               | Pado        | Is
# Number | ServerHost         | Port  | Port  | Port  | Properties        | Xml               | Properties         | Xml         | ServerStart
# ------ | ------------------ | ----- | ----- | ----- | ----------------- | ----------------- | ------------------ | ----------- | -----------
   1       localhost            20101   10101   30101   server.properties   server_gateway.xml  pado.properties      pado.xml      true
   2       localhost            20102   10102   30102   server.properties   server.xml          pado.properties      pado.xml      true
