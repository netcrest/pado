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

if "%1" == "-?" (
   echo Usage:
   echo    update_keytype [-dbdir dbDirPath] [-keyType className ^| -all ^| -reset]
   echo                   [-grid %GRIDS_OPT%] [-site %SITES_OPT%] [-?]
   echo.
   echo   IMPORTANT:
   echo       This command executes ISysBiz.registerKeyTypeQueryReferences. As such,
   echo       its execution may span one or more grids defined by the 'sys' app ID.
   echo       That is, if this command is issued to a parent grid, then all of
   echo       the child grids will be affected as well.
   echo.
   echo   Updates the KeyType query reference definitions found in the specified Pado DB directory.
   echo.
   echo       -dbdir   DB directory path. If not specified, defaults to %PADO_DB_DIR%.
   echo       -keyType Updates the grid^(s^) to the specified KeyType definitions found
   echo                in this node's DB.
   echo       -all     Updates the grid^(s^) to the persistent state of all of the KeyType
   echo                definitions found in this node's DB. This node's DB is applied
   echo                to all participating grids.
   echo       -reset   Resets the grid^(s^) to the their respective persistent state of all
   echo                of the KeyType definitions found in their node's DB.
   echo.
   echo    Default: KeyTypeUpdater -dbdir %PADO_DB_DIR% ...
   echo.
   goto stop
)

@call %GRIDS_DIR%\%GRID%\site_%SITE%.bat

:: locators
for %%i in (%LOCATOR_HOSTS%) do (
   @set LOCATORS=%%i:!LOCATOR_PORT!,%LOCATORS%
)

@set GEMFIRE_PROPERTIES=-DgemfirePropertyFile=%ETC_DIR%/client/client.properties
@set PADO_PROPERTIES=-Dpado.home.dir=%PADO_HOME% -Dpado.locators=%LOCATORS% -Dpado.server=false -Dpado.properties=%ETC_DIR%/client/pado.properties -Dpado.csv.properties=%ETC_DIR%/client/csv.properties -Dpado.db.dir=%PADO_DB_DIR%

echo BASE_DIR=%BASE_DIR%
@pushd %BASE_DIR%
"%GF_JAVA%" -Xms64m -Xmx64m -Djava.awt.headless=true %GEMFIRE_PROPERTIES% %SECURITY_PROPERTIES% %PADO_PROPERTIES% com.netcrest.pado.tools.KeyTypeUpdater %*
@popd

:stop
