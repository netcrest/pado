:: ========================================================================
:: Copyright (c) 2013-2017 Netcrest Technologies, LLC. All rights reserved.
::
:: Licensed under the Apache License, Version 2.0 (the "License");
:: you may not use this file except in compliance with the License.
:: You may obtain a copy of the License at
::
::     http://www.apache.org/licenses/LICENSE-2.0
::
:: Unless required by applicable law or agreed to in writing, software
:: distributed under the License is distributed on an "AS IS" BASIS,
:: WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
:: See the License for the specific language governing permissions and
:: limitations under the License.
:: ========================================================================

@echo off
@setlocal enabledelayedexpansion

@call setenv.bat
@call all_env.bat
@call argenv.bat %*

if "%1" == "-?" (
   echo Usage:
   echo    start_server [-num server-number] [-grid %GRIDS_OPT%] [-site %SITES_OPT%] [-gateway [xml-file-path]] [-rebalance] [-?]
   echo.
   echo   Starts a server in the specified grid and site. This command executes
   echo   on the local host. As such, it starts a server on the host where it
   echo   is executed.
   echo.
   echo       server-number server number 1-%NUM_SERVERS%
   echo       -grid      Starts the server in the specified grid. Default: %GRID_DEFAULT%
   echo       -site      Starts the server in the specified site. Default: %SITE_DEFAULT%
   echo.
   echo    Default: start_server -num 1 -grid %GRID_DEFAULT% -site %SITE_DEFAULT%
   echo.
   goto stop
)

@call %GRIDS_DIR%\%GRID%\site_%SITE%.bat

@set MY_ADDRESS=localhost
@set SERVER_HOST=localhost
@set CLIENT_BIND_ADDRESS=localhost
@set SERVER_BIND_ADDRESS=localhost

:: locators
for %%i in (%LOCATOR_HOSTS%) do (
   @set LOCATORS=%%i[!LOCATOR_PORT!],%LOCATORS%
)

::
:: server.xml and gateway.xml
::
if "%GATEWAY%" == "" (
   @set CACHE_XML_FILE=%SERVER_XML_FILE%
) else (
   if "%GATEWAY_XML_FILE%" == "" (
      @set CACHE_XML_FILE=%SERVER_GATEWAY_XML_FILE%
   ) else (
      @set CACHE_XML_FILE=%ETC_GRID_DIR%/%GATEWAY_XML_FILE%
   )
)

:: the parent directory of all servers 
if "%RUN_DIR%" == "" (
   @set RUN_DIR=%BASE_DIR%\run
)
:: directory in which the server is to be run
@set DIR=%RUN_DIR%\%SERVER_ID%
@if not exist "%DIR%" (
   @mkdir %DIR%
)

:: log directory
@if "%LOG_DIR%" == "" (
   @set LOG_DIR=%BASE_DIR%\log
)
@if not exist "%LOG_DIR%" (
   @mkdir %LOG_DIR%
)

:: stats directory
@if "%STATS_DIR%" == "" (
   @set STATS_DIR=%BASE_DIR%\stats
)
@if not exist "%STATS_DIR%" (
  @mkdir %STATS_DIR%
)

:: plugins directory
@if "%PADO_PLUGINS_DIR%" == "" (
   @set PADO_PLUGINS_DIR=%BASE_DIR%\plugins
)
@if not exist "%PADO_PLUGINS_DIR%" (
   @mkdir %PADO_PLUGINS_DIR%
)

:: etc directorires env passed in to Pado
@set PADO_ETC_DIR=%ETC_DIR%
@set PADO_ETC_GRID_DIR=%ETC_GRID_DIR%

@set CACHE_SERVER_PORT=%CACHE_SERVER_PORT_PREFIX%%SERVER_NUM%
@set SERVER_NAME=%GRID%-%SITE%-%SERVER_NUM%
@set LOG_FILE=%LOG_DIR%/%SERVER_ID%.log
@set STATS_FILE=%STATS_DIR%/%SERVER_ID%.gfs

::
:: SERVER_PROPERTIES - Cache server properties required by the plugin
@set SERVER_PROPERTIES=--disable-default-server --J=-Dgfinit.cacheserver.1.port=%CACHE_SERVER_PORT% --J=-Dgfinit.cacheserver.1.notify-by-subscription=true --J=-Dgfinit.cacheserver.1.socket-buffer-size=131072

:: The following environment variables are defined in the scripts 
:: and may be used in server.xml.
::    GRID - Grid ID.
::    SITE - Site name. This name should be used to configure gateways.
::    SERVER_PORT - Cache server port number for cache-server port=%SERVER_PORT%
::    SERVER_BIND_ADDRESS - Cache server bind address
::    CLIENT_BIND_ADDRESS - Hostname for clients
::    DISK_STORE_DIR - Disk store directory path
::
:: Example:
::    <cache-server port="%SERVER_PORT%"
::                  bind-address="%SERVER_BIND_ADDRESS%"
::                  hostname-for-clients="%CLIENT_BIND_ADDRESS%" />
::    <disk-store name="disk-store">
::       <disk-dirs>
::           <disk-dir>%DISK_STORE_DIR%</disk-dir>
::       </disk-dirs>
::    </disk-store>

@set GEMFIRE_PROPERTIES=--name=%SERVER_NAME% --J=-Dgemfire.log-file=%LOG_FILE% --statistic-archive-file=%STATS_FILE% --cache-xml-file=%SERVER_XML_FILE% --locators=%LOCATORS% --J=-Dgemfire.distributed-system-id=%SYSTEM_ID% --J=-DSITE=%SITE% --J=-DDISK_STORE_DIR=%DISK_STORE_DIR% --J=-DREMOTE_SYSTEM_ID_1=%REMOTE_SYSTEM_ID_1% --J=-DREMOTE_SYSTEM_ID_2=%REMOTE_SYSTEM_ID_2% --J=-Dgemfire.PREFER_SERIALIZED=false --J=-Dgemfire.BucketRegion.alwaysFireLocalListeners=false

REM @set GEMFIRE_PROPERTIES=name=%SERVER_NAME% log-file=%LOG_FILE% statistic-archive-file=%STATS_FILE% cache-xml-file=%CACHE_XML_FILE% locators=%LOCATORS% -J-Dgemfire.PREFER_DESERIALIZED=false -J-Dgemfire.BucketRegion.alwaysFireLocalListeners=false -J-Dgemfire.start-dev-rest-api=true -J-Dgemfire.http-service-bindaddress=localhost -J-Dgemfire.jmx-manager=true -J-Dgemfire.jmx-manager-start=true

REM @set GEMFIRE_PROPERTIES=name=%SERVER_NAME% log-file=%LOG_FILE% statistic-archive-file=%STATS_FILE% cache-xml-file=%CACHE_XML_FILE% locators=%LOCATORS% -J-Dgemfire.PREFER_DESERIALIZED=false -J-Dgemfire.BucketRegion.alwaysFireLocalListeners=false -J-Dgemfire.start-dev-rest-api=true

if "%GEMFIRE_PROPERTY_FILE%" == "" (
   @set GEMFIRE_PROPERTY_FILE=%ETC_GRID_DIR%\server.properties
)
if "%GEMFIRE_SECURITY_PROPERTY_FILE%" == "" (
   @set GEMFIRE_SECURITY_PROPERTY_FILE=%ETC_GRID_DIR%\gfsecurity.properties
)

::
:: Check if security is enabled
::
@set GEMFIRE_SECURITY_PROPERTY_SYSTEM=
if exist "%GEMFIRE_SECURITY_PROPERTY_FILE%" (
   @if "%SECURITY_ENABLED%" == "true" (
      @set GEMFIRE_SECURITY_PROPERTY_SYSTEM=-J-DgemfireSecurityPropertyFile=%GEMFIRE_SECURITY_PROPERTY_FILE%
   )
) else (
   @if "%SECURITY_ENABLED%" == "true" (
      echo.
      echo Security is enabled but the following security file does not exist:
      echo    %GEMFIRE_SECURITY_PROPERTY_FILE%
      echo start_server Aborted.
      echo.
      goto stop
   )
)

if "%GRID_ID%" == "" (
   @set GRID_ID=%GRID%
)
if "%GRID_NAME%" == "" (
   @set GRID_NAME=%GRID%
)
if "%SITE_ID%" == "" (
   @set SITE_ID=%SITE%
)
if "%SITE_NAME%" == "" (
   @set SITE_NAME=%SITE%
)

::
:: Application specifics
::
:: List all application specific properties here. Make sure to
:: use the prefix "--J=-D", e.g., APP_PROPERTIES=--J=-Dfoo.test=true
:: APP_PROPERTIES should be set in setenv.sh.
::
@set APP_PROPERTIES=%APP_PROPERTIES%

::
:: PADO_PROPERTIES - Pado specific properties
::
@set PADO_PROPERTIES=--J=-Dpado.grid.id=%GRID_ID% --J=-Dpado.grid.name=%GRID_NAME% --J=-Dpado.site.id=%SITE_ID% --J=-Dpado.site.name=%SITE_NAME% --J=-Dpado.home.dir=%PADO_HOME% --J=-Dpado.plugins.dir=%PADO_PLUGINS_DIR% --J=-Dpado.etc.dir=%ETC_DIR% --J=-Dpado.etc.grid.dir=%ETC_GRID_DIR% --J=-Dpado.padoPropertyFile=%PADO_PROPERTY_FILE% --J=-Dpado.db.dir=%PADO_DB_DIR% --J=-Dpado.properties=%PADO_PROPERTY_FILE% --J=-Dpado.appConfigDir=%PADO_APP_CONFIG_DIR% --J=-Dpado.security.encryption.enabled=true --J=-Dpado.server=true --J=-Dpado.config-file=%PADO_XML_FILE% --J=-Dpado.log.gridInfo=false --J=-Djavax.xml.accessExternalDTD=all

:: 
:: HEAPSIZE for min and max
::
@set HEAPSIZE=--initial-heap=%HEAP_MAX% --max-heap=%HEAP_MAX%

::
:: GC_PARAMETERS - GC specifics
@set GC_PARAMETERS=--J=-XX:+UseParNewGC --J=-XX:+UseConcMarkSweepGC --J=-XX:+DisableExplicitGC --J=-XX:CMSInitiatingOccupancyFraction=50
:: @set GC_PARAMETERS=--J=-XX:+UseParNewGC --J=-XX:+UseConcMarkSweepGC --J=-XX:+DisableExplicitGC --J=-XX:NewSize=256m --J=-XX:CMSInitiatingOccupancyFraction=50

::
:: JMX_PARAMETERS - JMX specifics
::
if "%JMX_PREFIX%" neq "" (
   @set JMX_PARAMETERS=--J=-Dcom.sun.management.jmxremote.port=%JMX_PREFIX%%SERVER_NUM% --J=-Dcom.sun.management.jmxremote.ssl=false --J=-Dcom.sun.management.jmxremote.authenticate=false
)

::
:: DEBUG_ENABLED - If true open debug port
::
if "%DEBUG_ENABLED%" == "true" (
   if not "%DEBUG_PREFIX%" == "" (
      @set DEBUG=--J=-Xdebug --J='-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=%DEBUG_PREFIX%%SERVER_NUM%'
   )
) else (
   @set DEBUG=
)

echo *****************************************************
if "%GATEWAY%" == "" (
   echo Starting %SERVER_ID% on host %MY_ADDRESS%
) else (
   echo Starting gateway %SERVER_ID% on host %MY_ADDRESS%
)
echo *****************************************************

REM echo cacheserver start -dir=%DIR% -J-Dpado.vm.id=%SERVER_ID% -J-Djava.awt.headless=true %HEAPSIZE% %GC_PARAMETERS% %DEBUG% -J-DgemfirePropertyFile=%GEMFIRE_PROPERTY_FILE% %GEMFIRE_SECURITY_PROPERTY_SYSTEM% %SERVER_PROPERTIES% %GEMFIRE_PROPERTIES% %PADO_PROPERTIES% %APP_PROPERTIES% %REBALANCE%
REM cacheserver start -dir=%DIR% -J-Dpado.vm.id=%SERVER_ID% -J-Djava.awt.headless=true %HEAPSIZE% %GC_PARAMETERS% %DEBUG% -J-DgemfirePropertyFile=%GEMFIRE_PROPERTY_FILE% %GEMFIRE_SECURITY_PROPERTY_SYSTEM% %SERVER_PROPERTIES% %GEMFIRE_PROPERTIES% %PADO_PROPERTIES% %APP_PROPERTIES% %REBALANCE%


:: ------------------
:: gfsh specifics
:: ------------------

echo gfsh start server --dir=%DIR% --J=-Dpado.vm.id=%SERVER_ID% --J=-Djava.awt.headless=true %HEAPSIZE% %GC_PARAMETERS% %JMX_PARAMETERS% %DEBUG% --J=-DgemfirePropertyFile=%GEMFIRE_PROPERTY_FILE% %GEMFIRE_SECURITY_PROPERTY_SYSTEM% %SERVER_PROPERTIES% %GEMFIRE_PROPERTIES% %PADO_PROPERTIES% %APP_PROPERTIES% %REBALANCE% --classpath=%CLASSPATH%
gfsh start server --dir=%DIR% --J=-Dpado.vm.id=%SERVER_ID% --J=-Djava.awt.headless=true %HEAPSIZE% %GC_PARAMETERS% %JMX_PARAMETERS% %DEBUG% --J=-DgemfirePropertyFile=%GEMFIRE_PROPERTY_FILE% %GEMFIRE_SECURITY_PROPERTY_SYSTEM% %SERVER_PROPERTIES% %GEMFIRE_PROPERTIES% %PADO_PROPERTIES% %APP_PROPERTIES% %REBALANCE% --classpath=%CLASSPATH%

:stop
