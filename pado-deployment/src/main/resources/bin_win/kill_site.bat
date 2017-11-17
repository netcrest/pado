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
   echo    kill_site [-grid %GRIDS_OPT%] [-site %SITES_OPT%] [-locators] [-?]
   echo.
   echo    Kills servers in the specified site in the specified grid.
   echo    The kill command is faster than the stop command
   echo    but at the expense of possible loss or corruption
   echo    of data.
   echo.
   echo       -grid      Kills the specified grid. Default: %GRID_DEFAULT
   echo       -site      Kills the specified site. Default: %SITE_DEFAULT
   echo       -locators  Kills locators in addition to servers.
   echo.
   echo    Default: kill_site -grid %GRID_DEFAULT% -site %SITE_DEFAULT%
   echo.
   goto stop
)

@call %GRIDS_DIR%\%GRID%\site_%SITE%.bat

@set LOCATOR_ID_PREFIX=locator-%GRID$-%SITE%
@set SERVER_ID_PREFIX=server-%GRID%-%SITE%

@set SERVER_HOSTS=localhost
@set NUM_HOSTS=0
for %%i in (%SERVER_HOSTS%) do (
   @set /a NUM_HOSTS="%NUM_HOSTS%+1"
)

@set /a NUM_SERVERS_PER_HOST="%NUM_SERVERS%/%NUM_HOSTS%"
@set /a NUM_SERVERS="%NUM_SERVERS_PER_HOST%*%NUM_HOSTS%"

@set /a BEGIN_NUM=1
for %%i in (%SERVER_HOSTS%) do (
   echo -----------------------------------------------------
   echo Kill host: %%i, grid: %GRID%, site: %SITE%
   echo -----------------------------------------------------
   @set /a END_NUM="!BEGIN_NUM!+!NUM_SERVERS_PER_HOST!-1"
   for /l %%g in (!BEGIN_NUM!, 1, !END_NUM!) do (
      @call kill_server -num %%g -grid %GRID% -site %SITE%
   )
   @set /a BEGIN_NUM="!END_NUM!+1"
)

if "%LOCATORS%" == "true" (
   @set /a NUM=1
   for %%i in (%LOCATOR_HOSTS%) do (
      echo -----------------------------------------------------
      echo Kill host: %%i, grid: %GRID%, site: %SITE%
      echo -----------------------------------------------------
      @call kill_locator -num !NUM! -grid %GRID% -site %SITE%
      @set /a NUM="!NUM!+1"
   )
)

:stop
