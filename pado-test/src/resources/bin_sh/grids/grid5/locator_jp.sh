# List locator specifics.
#
# [0] LocatorNumber must be a unique number that identifies the
#     locator. It should be sequentially incremented starting
#     from 1. Allowed values are [1-99], inclusive. Always
#     start at least two locators on different hosts.
# [2] LocatorHost is the host where the locator is launched.
# [3] LocatorPort is the locator port number that the underlying
#     data grid product opens for peers and clients to connect.
#     LocatorPort should be same for all locators.
# [4] JmxRmiPort is the RMI port that the locator opens for the
#     JMX tools to connect. If supported, a locator serves
#     as a JMX aggrator that collects JMX metrics from all
#     servers in the grid.
# [5] JmxHttp is the HTTP port that the locator opens for HTTP
#     browsers to connect.
#
# LocatorArray elements:
#    array[0] LocatorNumber
#    array[1] Host
#    array[2] LocatorPort
#    array[3] JmxRmiPort
#    array[4] JmxHttpPort
# Locator|                  | Locator | JmxRmi | JmxHttp
# Number | LocatorHost      | Port    | Port   | Port
# ------ | ---------------- | ------- | ------ | -------
   1       localhost          25200    25250    25251
