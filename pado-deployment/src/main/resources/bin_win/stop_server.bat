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
   echo    stop_server [-num server-number] [-grid %GRIDS_OPT%] [-site %SITES_OPT%] [-?]
   echo.
   echo    Stops a server in the specified grid and site." 
   echo.
   echo       server-number server number 1-99
   echo       -grid      Stops the server in the specified grid. Default: %GRID_DEFAULT%
   echo       -site      Stops the server in the specified site. Default: %SITE_DEFAULT%
   echo.
   echo    Default: stop_server -num 1 -grid %GRID_DEFAULT% -site %SITE_DEFAULT%
   echo.
   goto stop
)

@call %GRIDS_DIR%\%GRID%\site_%SITE%.bat

:: the parent directory of all servers 
if  "%RUN_DIR%" == "" (
   @set RUN_DIR=%BASE_DIR%\run
)
:: directory in which the server is to be stopped
@set DIR=%RUN_DIR%\%SERVER_ID%
@if not exist "%DIR%" (
  @mkdir %DIR%
)

echo stop_server -num %SERVER_NUM% -grid %GRID% -site %SITE%
echo gfsh stop server --dir=%DIR%
gfsh stop server --dir=%DIR%

:stop
