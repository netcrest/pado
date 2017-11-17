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
   echo    kill_grid [-grid %GRIDS_OPT%] [-site %SITES_OPT%] [-locators] [-?]
   echo.
   echo    Kill all servers in all sites in the specified grid.
   echo    The kill command is faster than the stop command
   echo    but at the expense of possible loss or corruption
   echo    of data.
   echo.
   echo       -grid      Kills the specified grid. Default: %GRID_DEFAULT%
   echo       -site      Kills the specified site. Default: all sites
   echo       -locators  Kills locators in addition to servers.
   echo.
   echo    Default: kill_grid -grid %GRID_DEFAULT%
   echo.
   goto stop
)

if "%SITE_SPECIFIED%" == "true" (
   echo *****************************************************
   echo Killing grid: %GRID%, site: %SITE%
   echo *****************************************************
   @call kill_site -grid %GRID% %*
   echo Killed grid: %GRID%, site: %SITE%
) else (
   echo *****************************************************
   echo Killing grid: %GRID%, site: all - %SITES%
   echo *****************************************************
   for %%i in (%SITES%) do (
      @call kill_site -grid %GRID% -site %%i %*
   )
   echo Killed grid: %GRID%, site: all - %SITES%
)

:stop
