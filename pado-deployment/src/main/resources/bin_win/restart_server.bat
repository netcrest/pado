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
   echo    restart_server [-num server-number] [-grid %GRIDS_OPT%] [-site %SITES_OPT%] [-gateway [xml-file-path]] [-rebalance] [-kill] [-?]
   echo.
   echo    Restarts a server by first stopping or killing the running server in the
   echo    specified grid and site.
   echo. 
   echo       server-number server number 1-99
   echo       -grid      Restarts the server in the specified grid. Default: %GRID_DEFAULT%
   echo       -site      Restarts the server in the specified site. Default: %SITE_DEFAULT%
   echo       -kill      Kills servers/locators before restarting them.
   echo                  The kill command is faster than the stop command
   echo                  but at the expense of possible loss or corruption
   echo                  of data. This command removes all server persistent
   echo                  files if any.
   echo.
   echo    Default: restart_server -num 1 -grid %GRID_DEFAULT% -site %SITE_DEFAULT%
   echo.
   goto stop
)

if "%KILL%" == "true" (
   @call kill_server %*
   @call clean_server %* -persist
   @call start_server %*
) else (
   @call stop_server %*
   @call clean_server %*
   @call start_server %*
)

:stop
