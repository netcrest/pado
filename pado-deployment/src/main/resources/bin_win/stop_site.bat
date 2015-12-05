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
   echo    stop_site [-grid %GRIDS_OPT%] [-site %SITES_OPT%] [-locators] [-agents] [-parallel] [-?]
   echo.
   echo   Stops servers in the specified site of the specified grid.
   echo.
   echo       -grid      Stops the specified grid. Default: %GRID_DEFAULT%
   echo       -site      Stops the specified site. Default: %SITE_DEFAULT%
   echo       -locators  Stops locators in addition to servers.
   echo       -agents    Stops agents in addition to servers.
   echo       -parallel  Stops servers in parallel
   echo.
   echo    Default: stop_site -grid %GRID_DEFAULT% -site %SITE_DEFAULT%
   echo.
   goto stop
)

@call %GRIDS_DIR%\%GRID%\site_%SITE%.bat

@set SERVER_HOSTS=localhost
@set NUM_HOSTS=0
for %%i in (%SERVER_HOSTS%) do (
   @set /a NUM_HOSTS="%NUM_HOSTS%+1"
)

@set /a NUM_SERVERS_PER_HOST="%NUM_SERVERS%/%NUM_HOSTS%"
@set /a NUM_SERVERS="%NUM_SERVERS_PER_HOST%*%NUM_HOSTS%"

:: locators
for %%i in (%LOCATOR_HOSTS%) do (
   @set ALL_LOCATORS=%%i[%LOCATOR_PORT%],%ALL_LOCATORS%
)

::echo
::echo stopping %NUM_SERVERS% servers on %SERVER_HOSTS%
::echo %NUM_SERVERS_PER_HOST% on each server
::echo

::echo Shutting down all servers. Please wait.
::echo gemfire -J-Dgemfire.locators=%ALL_LOCATORS shut-down-all
::gemfire -J-Dgemfire.locators=%ALL_LOCATORS shut-down-all
::echo Shutdown complete.

@set /a BEGIN_NUM=1
for %%i in (%SERVER_HOSTS%) do (
   echo -----------------------------------------------------
   echo Stop host: %%i, grid: %GRID%, site: %SITE%
   echo -----------------------------------------------------
   @set /a END_NUM="!BEGIN_NUM!+!NUM_SERVERS_PER_HOST!-1"
   for /l %%g in (!BEGIN_NUM!, 1, !END_NUM!) do (
      @call stop_server -num %%g -grid %GRID% -site %SITE%
   )
   @set /a BEGIN_NUM="!END_NUM!+1"
)

if "%LOCATORS%" == "true" (
   @set /a NUM=1
   for %%i in (%LOCATOR_HOSTS%) do (
      echo -----------------------------------------------------
      echo Stop host: %%i, grid: %GRID%, site: %SITE%
      echo -----------------------------------------------------
      @call stop_locator -num !NUM! -grid %GRID% -site %SITE%
      @set /a NUM="!NUM!+1"
   )
)

if "%AGENTS%" == "true" (
   @set /a NUM=1
   for %%i in (%AGENT_SERVERS%) do (
      echo -----------------------------------------------------
      echo Stop host: %%i, grid: %GRID%, site: %SITE%
      echo -----------------------------------------------------
      @call stop_agent -grid %GRID% -site %SITE%
      @set /a NUM="!NUM!+1"
   )
)

:stop
