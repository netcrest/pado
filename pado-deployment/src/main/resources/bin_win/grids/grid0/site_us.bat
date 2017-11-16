@echo off

:: site_<%SITE%>.bat defines site-wid parameters to configure
:: a single site. Parameters defined in this file become the
:: default parameters for servers running in the site.

:: SITE: Site ID. It must be unique and must not include spaces.
:: If SITE_ID is not defined then the site ID in the
:: %SITES list defined in grid_env.sh is assigned. It must 
:: not include spaces.
@set SITE_ID=%SITE%

:: SITE_NAME: Site name is a legible non-unique name for
:: display purposes. Any characters including spaces are allowed.
@set SITE_NAME=%SITE%

:: SYSTEM_ID is a number that uniquely identifies this cluster for
:: site-to-site (WAN) replication. GemFire specific.
@set SYSTEM_ID=1

:: List of locator hosts separated by space - only 1 locator per host is allowed
@set LOCATOR_HOSTS=localhost

:: Cache server port prefix. Limit the number of digits to 2 or 3.
:: Each cache is assigned a trailing number from 1 to 99.
:: For example, if it is set to 200, './start_server -num 1' assigns 20001
:: and this script sets locator=20000, agent rmi=20050, agent http=20051
@set CACHE_SERVER_PORT_PREFIX=200

:: JVM port prefix. Server number is appended to this prefix.
:: For example, if it is set to 300, './start_server -num 1' assigns 30001.
:: To disable JMX, set JMX_PREFIX with no value.
@set JMX_PREFIX=300

:: JVM debug mode. Set DEBUG_ENABLED to true to enable the debug mode.
@set DEBUG_ENABLED=true

:: JVM debug port prefix. Server number is appended to this prefix.
:: For example, if it is set to 100, './start_server -num 1' assigns 10001.
@set DEBUG_PREFIX=100

:: Loator port. Locator on each host uses the same port defined here.
@set LOCATOR_PORT=%CACHE_SERVER_PORT_PREFIX%00

:: Number of cache servers. This number is equally divided
:: amongst the list of cache sever hosts defined by %SERVER_HOSTS%
:: %SERVER_HOSTS% is determined by the servers listed in bind_%SITE%.bat.
@set NUM_SERVERS=2

:: Gateway servers
:: Starts one of the cache servers on each of the servers listed
:: as a gateway swerver. Comment it out to disable gateways.
:: The gateway server hosts must match the first hosts listed
:: in SERVER_HOSTS and must run on different hosts.
@set GATEWAY_SERVERS=

:: min/max cache server heap size.
@set HEAP_MAX=128m

:: Agent port number.
@set AGENT_SERVERS=localhost
@set AGENT_PORT=%CACHE_SERVER_PORT_PREFIX%50
@set AGENT_HTTP_PORT=%CACHE_SERVER_PORT_PREFIX%51

:: Pado app directory that contains client app config files
@set PADO_APP_CONFIG_DIR=%ETC_GRID_DIR%\app
:: Pado properties file path
@set PADO_PROPERTY_FILE=%ETC_GRID_DIR%\pado.properties
:: Gemfire properties file path.
@set GEMFIRE_PROPERTY_FILE=%ETC_GRID_DIR%\server.properties
:: Server configuration XML file path.
@set SERVER_XML_FILE=%ETC_GRID_DIR%\server.xml
:: Server gateway configuraiton XML file path.
@set SERVER_GATEWAY_XML_FILE=%ETC_GRID_DIR%\%SITE%_%GATEWAY%.xml
