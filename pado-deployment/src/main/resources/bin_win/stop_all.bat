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
   echo    ./stop_all [-grid %GRIDS_OPT% [-site %SITES_OPT% [-locators] [-agents] [-parallel] [-?]
   echo.
   echo    Stops all servers in all sites and all grids. If no options are
   echo    specified then it stops all servers in all grids and sites.
   echo.
   echo       -grid      Stops the specified grid only. Default: all grids
   echo       -site      Stops the specified site only. Default: all sites
   echo       -locators  Stops locators in addition to servers.
   echo       -agents    Stops agents in addition to servers.
   echo       -parallel  Stops servers in parallel
   echo.
   echo    Default: ./stop_all
   echo.
   goto stop
)

::
:: stop all sites
::
for %%i in (%GRIDS%) do (
   stop_grid -grid %%i %*
)
echo Stopped all grids
echo.

:stop
