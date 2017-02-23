:: ========================================================================
:: Copyright (c) 2013-2016 Netcrest Technologies, LLC. All rights reserved.
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
   echo    import_csv [-verbose] [-grid %GRIDS_OPT%] [-site %SITES_OPT%] [-?]
   echo.
   echo   Imports all CSV files found in the data/import directory. The name of each
   echo   CSV file in data/import must begin with the name of the machtching schema
   echo   file in the data/schema directory. For example, 'data/import/foo_v3.csv'
   echo   must be paired with 'data/schema/foo.schema'.
   echo.
   echo   IMPORTANT: If login is required then you must include appId, user name, and password
   echo              %ETC_DIR%/client/csv.properties.
   echo              Note that locators are automatically picked up by import_csv and not required
   echo              in csv.properties.
   echo.
   echo       -temporal If specified then temporal indexing is not disabled while importing data.
   echo                 Default: The importer first disables temporal indexing if it is enabled
   echo                 in order to speed up the load time. Upon completion, it re-enables temporal
   echo                 which in turn rebuilds temporal indexes.
   echo       -verbose  If specified then prints additional import status information.
   echo       -grid     Imports the data files in the specified grid. Default: %GRID_DEFAULT%
   echo       -site     Imports the data files in the specified site. Default: %SITE_DEFAULT%
   echo.
   echo    Default: import_csv -grid %GRID_DEFAULT% -site %SITE_DEFAULT%
   echo.
   goto stop
)

@call %GRIDS_DIR%\%GRID%\site_%SITE%.bat

:: locators
for %%i in (%LOCATOR_HOSTS%) do (
   @set LOCATORS=%%i:!LOCATOR_PORT!,%LOCATORS%
)

@set GEMFIRE_PROPERTIES=-DgemfirePropertyFile=%ETC_DIR%/client/client.properties -Dgemfire.log-file=%LOG_DIR%/%0.log
@set PADO_PROPERTIES=-Dpado.home.dir=%PADO_HOME% -Dpado.locators=%LOCATORS% -Dpado.server=false -Dpado.properties=%ETC_DIR%\client\pado.properties -Dpado.csv.properties=%ETC_DIR%\client\csv.properties

@pushd %BASE_DIR%
"%GF_JAVA%" -Xms512m -Xmx512m -Djava.awt.headless=true %GEMFIRE_PROPERTIES% %SECURITY_PROPERTIES% %PADO_PROPERTIES% com.netcrest.pado.tools.CsvFileImporter %*
@popd

:stop
