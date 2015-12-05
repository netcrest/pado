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
   echo    check_all [-grid %GRIDS_OPT% [-site %SITES_OPT%] [-prompt] [-?]
   echo.
   echo        Displays running status of all locators, agents and servers of
   echo        all sites and all grids.
   echo.
   echo       -grid      Displays servers in the specified grid only. Default: %GRID_DEFAULT%
   echo       -site      Displays servers in the specified site only. Default: %SITE_DEFAULT%
   echo       -prompt    Displays user prompt to exit.
   echo.
   echo    Default: check_all
   echo.
   goto stop
)

if "%GRID_SPECIFIED%" == "true" (
   check_grid %*
) else (
   for %%i in (%GRIDS%) do (
      check_grid -grid %%i %*
   )
)

if "%PROMPT%" == "true" (
   @set /p USER_INPUT=Type Enter to exit . . .
)

:stop
