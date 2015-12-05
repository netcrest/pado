:: ========================================================================
:: Copyright (c) 2013-2015 Netcrest Technologies, LLC. All rights reserved.
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

::
:: argenv.bat parses input arguments of individual scripts
:: and assign appropriate parameters.
::

::
:: Determine arguments
::
@set SERVER_NUM=1
@set REBALANCE=
@set GATEWAY=
@set GATEWAY_XML_FILE=
@set GRID_SPECIFIED=
@set SITE_SPECIFIED=
@set PARALLEL=
@set ALL=
@set LOCATORS=
@set AGENTS=
@set CLIENTS=
@set LOCATORS_OPT=
@set PERSIST=
@set PERSIST_OPT=
@set JAR_OPT=
@set BEGIN_NUM=1
@set END_NUM=
@set KILL=
@set PROMPT=
@set PREV=

:LOOP
IF "X%~1" == "X" GOTO DONE
   if "%PREV%"=="-num" (
      @set SERVER_NUM=%~1
   ) else if "%PREV%" == "-grid" (
      @set GRID=%~1
      @set GRID_SPECIFIED=true
   ) else if "%PREV%" == "-site" (
      @set SITE=%~1
      @set SITE_SPECIFIED=true
   ) else if "%PREV%" == "-jar" (
      @set JAR_OPT=%~1
   ) else if "%PREV%" == "-begin" (
      @set BEGIN_NUM=%~1
   ) else if "%PREV%" == "-end" (
      @set END_NUM=%~1
   ) else if "%~1" == "-gateway" (
      @set GATEWAY=gateway
   ) else if "%~1" == "-rebalance" (
      @set REBALANCE=-rebalance
   ) else if "%~1" == "-parallel" (
      @set PARALLEL=true
   ) else if "%~1" == "-all" (
      @set ALL=true
   ) else if "%~1" == "-locators" (
      @set LOCATORS=true
      @set LOCATORS_OPT=%~1
   ) else if "%~1" == "-agents" (
      @set AGENTS=true
   ) else if "%~1" == "-clients" (
      @set CLIENTS=true
   ) else if "%~1" == "-kill" (
      @set KILL=true
   ) else if "%~1" == "-prompt" (
      @set PROMPT=true
   ) else if "%~1" == "-persist" (
      @set PERSIST=true
      @set PERSIST_OPT=%~1

   ! this must be the last check
   ) else if "%PREV%" == "-gateway" (
      @set GATEWAY_XML_FILE=%~1
   )
   @set PREV=%~1

SHIFT
GOTO LOOP
:DONE

:: Determine the server number
if %SERVER_NUM% LSS 10 (
   @set SERVER_NUM=0%SERVER_NUM%
)

:: If the end server number is not defined then
:: assign it to the beginning server number.
if "%END_NUM%" == "" (
   @set END_NUM=%BEGIN_NUM%
)

:: Set the grid options to display in the command usage.
@set GRID_DEFAULT=
@set GRIDS_OPT=
@set ALL_SITES=
@for %%X in (%GRIDS%) do (
   @if not defined GRIDS_OPT (
      @set GRIDS_OPT=%%X
      @set GRID_DEFAULT=%%X
   ) else (
      @set GRIDS_OPT=!GRIDS_OPT! OR %%X
   )
   @call grids\%%X\grid_env.bat
   @set ALL_SITES=%ALL_SITES% %SITES%
)

:: Set all sites found in all grids
::unique_words "%ALL_SITES%" SITES

:: Set the grid to the default grid ID if undefined.
if "%GRID%" == "" (
   @set GRID=%GRID_DEFAULT%
)

:: Load the grid specifics from the grid_env.bat file which
:: must be supplied in the grid directory that has the
:: same name as the grid ID.
@call grids\%GRID%\grid_env.bat

:: Set the etc directory for the grid if undefined.
if "%GRIDS_DIR%" == "" (
   @set GRIDS_DIR=%BASE_DIR%\bin_win\grids
)

:: Set the etc directory
if "%ETC_DIR%" == "" (
   @set ETC_DIR=%BASE_DIR%\etc
)

:: Set the etc directory for the grid if undefined.
if "%ETC_GRID_DIR%" == "" (
   @set ETC_GRID_DIR=%ETC_DIR%\%GRID%
)

:: Set RUN_DIR if not defined
if "%RUN_DIR%" == "" (
   @set RUN_DIR=%BASE_DIR%\run
)

:: Set SECURITY_DIR if not defined
if "%SECURITY_DIR%" == "" (
   @set SECURITY_DIR=%BASE_DIR%\security
)

:: Set LOG_DIR if not defined
if "%LOG_DIR%" == "" (
   @set LOG_DIR=%BASE_DIR%\log
)

:: Set STATS_DIR if not defined
if "%STATS_DIR%" == "" (
   @set STATS_DIR=%BASE_DIR%\stats
)

:: Set PADO_PLUGINS_DIR if not defined
if "%PADO_PLUGINS_DIR%" == "" (
   @set PADO_PLUGINS_DIR=%BASE_DIR%\plugins
)

:: Set PADO_APP_CONFIG_DIR if not defined
if "%PADO_APP_CONFIG_DIR%" == "" (
  @set PADO_APP_CONFIG_DIR=%ETC_GRID_DIR%\app
)

:: Set PADO_PROPERTY_FILE if not defined
if "%PADO_PROPERTY_FILE%" == "" (
  @set PADO_PROPERTY_FILE=%ETC_GRID_DIR%\pado.properties
)

:: Set GEMFIRE_PROPERTY_FILE if not defined
if "%GEMFIRE_PROPERTY_FILE%" == "" (
  @set GEMFIRE_PROPERTY_FILE=%ETC_GRID_DIR%\server.properties
)

:: Set SERVER_XML_FILE if not defined
if "%SERVER_XML_FILE%" == "" (
  @set SERVER_XML_FILE=%ETC_GRID_DIR%\server.xml
)

:: Set SERVER_GATEWAY_XML_FILE if not defined
if "%SERVER_GATEWAY_XML_FILE%" == "" (
   @set SERVER_GATEWAY_XML_FILE=%ETC_GRID_DIR%\%SITE%_%GATEWAY%.xml
)

:: Set the site options to display in the command usage.
@set SITE_DEFAULT=
@set SITES_OPT=
@for %%X in (%SITES%) do (
   @if not defined SITES_OPT (
      @set SITES_OPT=%%X
      @set SITE_DEFAULT=%%X
   ) else (
      @set SITES_OPT=!SITES_OPT! OR %%X
   )
)

:: Set the site to the default site if undefined.
if "%SITE%" == "" (
   @set SITE=%SITE_DEFAULT%
)

@set LOCATOR_ID=locator-%GRID%-%SITE%%SERVER_NUM%
@set AGENT_ID=agent-%GRID%-%SITE%
@set SERVER_ID=server-%GRID%-%SITE%%SERVER_NUM%
