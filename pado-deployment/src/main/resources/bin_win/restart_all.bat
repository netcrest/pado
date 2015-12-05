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
   echo    restart_all [-grid %GRIDS_OPT%] [-site %SITES_OPT%] [-locators] [-agents] [-parallel] [-kill] [-?]
   echo. 
   echo    Restarts all grids and sites by first stopping all servers in
   echo    all grids and all sites.
   echo. 
   echo       -grid      Restarts the specified grid only. Default: all grids
   echo       -site      Restarts the specified site only. Default: all sites
   echo       -locators  Restarts locators in addition to servers.
   echo       -agents    Restarts agents in addition to servers.
   echo       -parallel  Restarts servers in parallel
   echo       -kill      Kills servers/locators before restarting them.
   echo                  The kill command is faster than the stop command
   echo                  but at the expense of possible loss or corruption
   echo                  of data. This command removes all server persistent
   echo                  files if any.
   echo. 
   echo    Default: restart_all
   echo. 
   goto stop
)

if "%KILL%" == "true" (
   @call kill_all %*
   @call clean_all %* -all
   @call start_all %*
) else (
   @call stop_all %*
   @call clean_all %*
   @call start_all %*
)

:stop
