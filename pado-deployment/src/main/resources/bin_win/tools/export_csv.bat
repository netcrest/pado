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
   echo    export_csv [-gridpath gridpath] [-all] [-grid %GRIDS_OPT%] [-site %SITES_OPT%] [-?]
   echo.
   echo   Exports all or a single grid path to schema and CSV files.
   echo.
   echo       -gridpath  grid path to export. gridpath must not begin with '/'
   echo       -all       exports all grid paths
   echo       -grid      Starts the server in the specified grid. Default: %GRID_DEFAULT%
   echo       -site      Starts the server in the specified site. Default: %SITE_DEFAULT%
   echo.
   echo    Default: export_csv -gridpath temporal -grid %GRID_DEFAULT% -site %SITE_DEFAULT%
   echo.
   goto stop
)

@call %GRIDS_DIR%\%GRID%\site_%SITE%.bat

:: locators
for %%i in (%LOCATOR_HOSTS%) do (
   @set LOCATORS=%%i:!LOCATOR_PORT!,%LOCATORS%
)

@set GEMFIRE_PROPERTIES=-DgemfirePropertyFile=%ETC_DIR%/client/client.properties -Dgemfire.log-file=%LOG_DIR%/%0.log
@set PADO_PROPERTIES=-Dpado.home.dir=%PADO_HOME% -Dpado.locators=%LOCATORS% -Dpado.server=false -Dpado.properties=%ETC_DIR%/client/pado.properties -Dpado.csv.properties=%ETC_DIR%/client/csv.properties

@pushd %BASE_DIR%
"%GF_JAVA%" -Xms512m -Xmx512m -Djava.awt.headless=true %GEMFIRE_PROPERTIES% %SECURITY_PROPERTIES% %PADO_PROPERTIES% com.netcrest.pado.tools.CsvFileExporter %*
@popd

:stop
