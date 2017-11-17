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
   echo Do NOT run this command while servers are running.
   echo Remove all server log and stats files. Use options to remove other
   echo files, also.
   echo. 
   echo    clean_all [-grid %GRIDS_OPT%] [-site %SITES_OPT%] [-all] [-locators] [-persist] [-?]
   echo. 
   echo       -grid      Removes log and stats files generated in the specified grid
   echo                  Default: %GRID_DEFAULT%
   echo       -site      Removes log and stats files generated in the specified site
   echo                  Default: %SITE_DEFAULT%
   echo       -all       Remove all including server, locator and persistent files
   echo       -locators  Removes locator files in addition to server files
   echo       -persist   Removes persistent files generated by servers
   echo. 
   echo    Default: clean_all
   echo. 
   goto stop
)

::
:: clean all sites
::

if "%GRID_SPECIFIED%" == "true" (
   @call clean_grid %*
) else (
   for %%i in (%GRIDS%) do (
      @call clean_grid -grid %%i %*
   )
)

echo All grids cleaned.
echo.

:stop
