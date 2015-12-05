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
@setlocal enabledelayedexpansion

@call setenv.bat
@call all_env.bat
@call argenv.bat %*

if "%1" == "-?" (
   echo Usage:
   echo    ./start_grid [-grid %GRIDS_OPT%] [-site %SITES_OPT%] [-locators] [-agents] [-parallel] [-?]
   echo.
   echo    Starts all servers in all sites of the specified grid.
   echo.
   echo       -grid      Starts the specified grid. Default: %GRID_DEFAULT%
   echo       -site      Starts the specified site. Default: %SITE_DEFAULT%
   echo       -locators  Starts locators in addition to servers.
   echo       -agents    Starts agents in addition to servers.
   echo       -parallel  Starts servers in parallel.
   echo.
   echo    Default: ./start_grid -grid %GRID_DEFAULT%
   echo.
   goto stop
)

::
:: start all sites
::
if "%SITE_SPECIFIED%" == "true" (
   echo *****************************************************
   echo Starting grid: %GRID%, site: %SITE%
   echo *****************************************************
   @call start_site -grid %GRID% %*
   echo Started grid: %GRID%, site: %SITE%
) else (
   echo *****************************************************
   echo Starting grid: %GRID%, site: all - %SITES%
   echo *****************************************************
   for %%i in (%SITES%) do (
      @call start_site -grid %GRID% -site %%i %*
   )
   echo Started grid: %GRID%, site: all - %SITES%
)

:stop
