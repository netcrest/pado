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
   echo Do NOT run this command while locators are running.
   echo It will remove the log and stats files.
   echo.
   echo    clean_locator [-num <locatorNum>] [-grid %GRIDS_OPT%] [-site %SITES_OPT%] [-?]
   echo.
   echo        Cleans up locator directories by removing log, stats, other temporary files
   echo        from the specified grid and site.
   echo.
   echo    Default: clean_locator -num 1 -grid %GRID_DEFAULT% -site %SITE_DEFAULT%
   echo.
   goto stop
)

@call %GRIDS_DIR%\%GRID%\site_%SITE%.bat

rmdir /s /q %RUN_DIR%\%LOCATOR_ID%

:stop
