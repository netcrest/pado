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
   echo    restart_agent [-grid %GRIDS_OPT%] [-site %SITES_OPT%] [-kill] [-?]
   echo. 
   echo    Restarts a agent by first stopping or killing the running agent in the
   echo    specified grid and site.
   echo.
   echo       -grid      Restarts the agent in the specified grid. Default: %GRID_DEFAULT%
   echo       -site      Restarts the agent in the specified site. Default: %SITE_DEFAULT%
   echo       -kill      Kill the agent before restarting it.
   echo                  The kill command is faster than the stop command
   echo                  but at the expense of possible loss or corruption
   echo                  of data.
   echo. 
   echo    Default: restart_agent -grid %GRID_DEFAULT% -site %SITE_DEFAULT%
   echo. 
   goto stop
)

@if "%KILL%" == "true" (
   @call kill_agent %*
   @call clean_agent %* -persist
   @call start_agent %*
) else (
   @call stop_agent %*
   @call clean_agent %* -persist
   @call start_agent %*
)

goto stop

:stop
