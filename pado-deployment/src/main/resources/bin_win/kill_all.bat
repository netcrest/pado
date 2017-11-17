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
   echo    kill_all [-grid %GRIDS_OPT%] [-site %SITES_OPT%] [-locators] [-?]
   echo.
   echo    Kills all servers in all sites and all grids.
   echo    The kill command is faster than the stop command
   echo    but at the expense of possible loss or corruption
   echo    of data.
   echo.
   echo       -grid      Kills the specified grid only. Default: all grids
   echo       -site      Kills the specified site only. Default: all sites
   echo       -locators  Kills locators in addition to servers.
   echo.
   echo    Default: kill_all -grid %GRID_DEFAULT%
   echo.
   goto stop
)

if "%GRID_SPECIFIED%" == "true" (
   @call kill_grid -grid %GRID% %*
) else (
   for %%i in (%GRIDS%) do (
      @call kill_grid -grid %%i %*
   )
)

if "%GRID_SPECIFIED%" == "true" (
   echo Killed grid: %GRID%
) else (
   echo Killed all grids
)
echo.

:stop
