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
   echo    start_all [-grid %GRIDS_OPT%] [-site %SITES_OPT%] [-locators] [-parallel] [-?]
   echo    Starts all servers in all sites and all grids. Note that
   echo    locators must be running before starting servers.
   echo.
   echo       -grid      Starts the specified grid only. Default: all grids
   echo       -site      Starts the specified site only. Default: all sites
   echo       -locators  Starts locators in addition to servers.
   echo       -parallel  Starts servers in parallel
   echo.
   echo    Default: start_all
   echo.
   goto stop
)

::
:: start all sites
::
echo *****************************************************
if "%GRID_SPECIFIED%" == "true" (
   if "%SITE_SPECIFIED%" == "true" (
      echo Starting grid: %GRID%, site: %SITE%
   ) else (
      echo Starting grid: %GRID%, site: all - %SITES%
   )
   echo *****************************************************
   @call start_grid %*
) else (
   if "%SITE_SPECIFIED%" == "true" (
      echo Starting grid: all - %GRIDS%, site: %SITE%
   ) else (
      echo Starting grid: all - %GRIDS%, site: all - %SITES%
   )
   echo *****************************************************
   for %%i in (%GRIDS%) do (
      @call start_grid -grid %%i %*
   )
)

echo.
if "%GRID_SPECIFIED%" == "true" (
   if "%SITE_SPECIFIED%" == "true" (
      echo Started grid: %GRID%, site: %SITE%
   ) else (
      echo Started grid: %GRID%, site: all - %SITES%
   )
) else (
   if "%SITE_SPECIFIED%" == "true" (
      echo Started grid: all - %GRIDS%, site: %SITE%
   ) else (
      echo Started grid: all - %GRIDS%, site: all - %SITES%
   )
)
echo.

:stop
