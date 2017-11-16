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
   echo    restart_site [-grid %GRIDS_OPT%] [-site %SITES_OPT%] [-locators] [-parallel] [-kill] [-?]
   echo.
   echo    Restarts servers by first stopping or killing all servers in the
   echo    specified site.
   echo.
   echo       -grid      Restarts the specified grid. Default: %GRID_DEFAULT%
   echo       -site      Restarts the specified site. Default: %SITE_DEFAULT%
   echo       -locators  Restarts locators in addition to servers.
   echo       -parallel  Starts servers in parallel
   echo       -kill      Kills servers/locators before restarting them.
   echo                  The kill command is faster than the stop command
   echo                  but at the expense of possible loss or corruption
   echo                  of data. This command removes all server persistent
   echo                  files if any.
   echo.
   echo    Default: restart_site -grid %GRID_DEFAULT% -site %SITE_DEFAULT%
   echo.
   goto stop
)

if "%KILL%" == "true" (
   @call kill_site %*
   @call clean_site %* -persist
   @call start_site %*
) else (
   @call stop_site %*
   @call clean_site %*
   @call start_site %*
)

:stop
