@echo off

:: grid_env.bat defines grid-wide parameters to configure a
:: single grid. Parameters defined in this file become the
:: default parameters for all sites. Any of the parameters
:: can be overwritten in individual site files. A site file
:: must begin with a site name followed by the "_env.sh" postfix.

:: SITES: List the site names separated by space. These sites belong
:: to a single Pado grid. The first site in the list is always
:: always the default site.
@set SITES=us uk jp

:: Enable or disable security. If security is disabled
:: then the $ETC_GRID/gfsecurity.properties file is ignored.
:: Note that if this parameter is set to true and the security
:: file does not exist then the server will not start.
::@set SECURITY_ENABLED=true

:: GRID_ID: Grid ID is a unique ID representing this grid. 
:: If GRID_ID is not defined then the grid ID in the
:: %GRIDS% list defined in all_env.sh is assigned. It must 
:: not include spaces.
@set GRID_ID=%GRID%

:: GRID_NAME: Grid name is a legible non-unique name display
:: purposes. Any characters including spaces are allowed.
@set GRID_NAME=%GRID%

:: ETC_GRID_DIR: etc directory that contains the config files 
:: (*.properties, *.xml). 
:: Default: @set ETC_GRID_DIR=%BASE_DIR%\etc\%GRID%
@set ETC_GRID_DIR=%BASE_DIR%\etc\%GRID%

:: RUN_DIR: Run directory - Pado server working directory
:: Default: @set RUN_DIR=%BASE_DIR%\run
::@set RUN_DIR=%BASE_DIR%\run

:: LOG_DIR: Log directory
:: Default: @set LOG_DIR=%BASE_DIR%\log
::@set LOG_DIR=%BASE_DIR%\log

:: STATS_DIR: Stats directory
:: Default: @set STATS_DIR=%BASE_DIR%\stats
::@set STATS_DIR=%BASE_DIR%\stats

:: PADO_PLUGINS_DIR: Plugins directory. All of Pado plug-in jar
:: files must be deployed into this directory. Pado also 
:: hot-deploys IBiz and versioned data class jar files to this
:: this directory.
:: Default: @set PADO_PLUGINS_DIR=%BASE_DIR%\plugins
::@set PADO_PLUGINS_DIR=%BASE_DIR%\plugins

:: Pado initialization script. If specified, the script
:: file is executed once the server has fully been initialized.
:: Default: Undefined
@set PADO_INIT_SCRIPT=%BASE_DIR%\script\wan_init.pado

:: Pado app directory that contains client app config files
::@set PADO_APP_CONFIG_DIR=%ETC_GRID_DIR%\app
:: Pado properties file path
::@set PADO_PROPERTY_FILE=%ETC_GRID_DIR%\pado.properties
:: Gemfire properties file path.
::@set GEMFIRE_PROPERTY_FILE=%ETC_GRID_DIR\server.properties
:: Server configuration XML file path.
::@set SERVER_XML_FILE=%ETC_GRID_DIR%\server.xml
:: Server gateway configuraiton XML file path.
::@set SERVER_GATEWAY_XML_FILE=%ETC_GRID_DIR%\%SITE%_%GATEWAY%.xml
